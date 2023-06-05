package filesyncer.client

import filesyncer.common.*
import java.io.DataOutputStream
import java.io.File
import java.net.Socket
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.util.concurrent.LinkedBlockingQueue
import message.FSEventMessage
import message.FSEventMessage.EventType

enum class FSCLIENT_STATE {
    DISCONNECTED,
    CONNECTED,
    LOGGEDIN
}

class Client(var localRepoDir: File) :
    FSEventMessageHandler, FSClientFrontInterface, FileWatcher.OnFileChangedListener {
    var state = FSCLIENT_STATE.DISCONNECTED
    var runner: FSEventConnWorker? = null
    var front: FSClientFront? = null

    var address = ""
    var port = 500

    private var waitingLoginRequest = FSRequestFsm()
    private var waitingRegisterRequest = FSRequestFsm()
    private var waitingListFolderRequest = FSRequestFsm()
    private var waitingSyncRequest = FSRequestFsm()

    private var _id = ""
    private var _passwd = ""
    private var _cloudFileLists: List<String> = mutableListOf()

    private val _reportMsgQueue = LinkedBlockingQueue<String>()

    private val _localRepoWatcher = FileWatcher(localRepoDir.toPath())

    private val logicalClock = FSLogicalClock()

    init {
        _localRepoWatcher.fileChangedListener = this
    }

    fun start() {

        if (!this.localRepoDir.exists()) {
            if (!this.localRepoDir.mkdir()) throw Error("Failed to mkdir() ${this.localRepoDir}.")
        } else {
            if (!this.localRepoDir.isDirectory)
                throw Error("Repo path(${this.localRepoDir}) is already occupied by other file")
        }

        front?.start()
    }

    fun startConnection(address: String, port: Int) {
        println("Creating connworker")
        val socket = Socket(address, port)
        runner = FSEventConnWorker(socket, this, true)
        runner!!.run()

        _localRepoWatcher.run()
    }

    override fun handleMessage(msg: FSEventMessage) {
        println("client code = ${msg.mEventcode}, id: ${msg.userIdField.str}")
        when (msg.mEventcode) {
            EventType.NONE -> {
                // do nothing
            }
            EventType.ASK_ALIVE -> {}
            EventType.ANSWER_ALIVE -> {}
            EventType.LOGIN_REQUEST -> {}
            EventType.LOGIN_GRANTED -> {
                waitingLoginRequest.grant()
                state = FSCLIENT_STATE.LOGGEDIN
            }
            EventType.LOGIN_REJECTED -> {
                waitingLoginRequest.reject()
            }
            EventType.LOGOUT -> {}
            EventType.BROADCAST_CONNECTED -> {
                _reportMsgQueue.put("${msg.userIdField.str} connected.")
            }
            EventType.BROADCAST_DISCONNECTED -> {
                _reportMsgQueue.put("${msg.userIdField.str} disconnected.")
            }
            EventType.UPLOAD_DONE -> {
                _reportMsgQueue.put("${msg.extraStrField.str} updated to server.")
            }
            EventType.DOWNLOAD_DONE -> {}
            EventType.UPLOAD_REQUEST -> {}
            EventType.DOWNLOAD_REQUEST -> {}
            EventType.REGISTER_REQUEST -> {}
            EventType.REGISTER_GRANTED -> {
                waitingRegisterRequest.grant()
            }
            EventType.REGISTER_REJECTED -> {
                waitingRegisterRequest.reject()
            }
            EventType.LISTFOLDER_RESPONSE -> {
                waitingListFolderRequest.grant()
                _cloudFileLists = msg.fileListField.strs.toList()
            }
            EventType.SYNC -> {
                val serverClockValue = msg.extraStrField.str.toLong()
                logicalClock.sync(serverClockValue)
                waitingSyncRequest.grant()
            }
            else -> {
                // TODO("Raise error or ..")
            }
        }
    }

    override fun checkConnection(address: String, port: Int): Boolean {
        if (
            (runner != null) &&
                runner!!.connection.isConnected() &&
                (this.address == address) &&
                (this.port == port)
        ) {
            return true
        }

        try {
            startConnection(address, port)
        } catch (e: Exception) {
            return false
        }
        this.address = address
        this.port = port
        return true
    }

    override fun requestLogin(id: String, password: String, address: String, port: Int): Boolean {
        if (!checkConnection(address, port)) {
            return false
        }

        try {

            runner!!.putMsgToSendQueue(FSEventMessage(EventType.LOGIN_REQUEST, id, password))
        } catch (e: Exception) {

            return false
        }

        waitingLoginRequest.wait()

        if (waitingLoginRequest.state == FSRequestFsm.STATE.GRANTED) {
            _id = id
            _passwd = password
            waitingLoginRequest.reset()
            syncLogicalClock()
            return true
        }

        waitingLoginRequest.reset()
        return false
    }

    override fun requestRegister(
        id: String,
        password: String,
        address: String,
        port: Int
    ): Boolean {
        if (!checkConnection(address, port)) {
            return false
        }

        try {
            runner!!.putMsgToSendQueue(FSEventMessage(EventType.REGISTER_REQUEST, id, password))
        } catch (e: Exception) {
            return false
        }

        waitingRegisterRequest.wait()

        if (waitingRegisterRequest.state == FSRequestFsm.STATE.GRANTED) {
            _id = id
            waitingRegisterRequest.reset()
            return true
        }

        waitingRegisterRequest.reset()
        return false
    }

    override fun showFolder(dir: String): List<Map<String, String>> {
        val localFiles =
            this.localRepoDir.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()

        try {
            runner!!.putMsgToSendQueue(FSEventMessage(EventType.LISTFOLDER_REQUEST))
        } catch (e: Exception) {
            return emptyList()
        }

        waitingListFolderRequest.wait()
        waitingListFolderRequest.reset()

        val cloudFiles = _cloudFileLists

        val lists = HashSet(localFiles + cloudFiles).toList().sorted()
        val localSet = HashSet(localFiles)
        val cloudSet = HashSet(cloudFiles)
        val data =
            lists.map {
                val inLocal = localSet.contains(it)
                val inCloud = cloudSet.contains(it)
                val status =
                    if (inLocal) if (inCloud) "Both" else "Local Only"
                    else if (inCloud) "Cloud Only" else "ERR"
                mapOf("name" to it, "status" to status, "type" to "file")
            }
        return data
    }

    override fun uploadFile(path: String): Boolean {
        val file = this.localRepoDir.resolve(File(path))
        var fileUploadThread = Thread {
            val fsocket = Socket(address, 7777)
            val fsize = file.length()
            val fname = file.name
            val fnameByteBuffer = fname.toByteArray()

            val ous = fsocket.getOutputStream()
            val dous = DataOutputStream(ous)
            dous.writeLong(fsize)
            dous.writeInt(fnameByteBuffer.size)
            dous.write(fnameByteBuffer)

            val fios = file.inputStream()

            ous.use { fios.copyTo(ous) }

            fsocket.close()
        }

        fileUploadThread.start()
        fileUploadThread.join()

        return true
    }

    override fun disconnect() {
        runner?.putMsgToSendQueue(FSEventMessage(EventType.LOGOUT, _id, _passwd))
        runner?.stop()

        _localRepoWatcher.stop()
    }

    override fun takeReportMessage(): String? {
        return _reportMsgQueue.poll()
    }

    override fun onFileChanged(file: File, kind: WatchEvent.Kind<out Any>) {
        // TODO : implement
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            // TODO : implement
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            // TODO : implement
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            // TODO : implement
        }
    }

    fun syncLogicalClock() {
        logicalClock.get()
        try {
            runner!!.putMsgToSendQueue(
                FSEventMessage(EventType.SYNC, "", "", "${logicalClock.time}")
            )
        } catch (e: Exception) {
            return
        }

        waitingSyncRequest.wait()
        waitingSyncRequest.reset()
    }
}
