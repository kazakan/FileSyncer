package filesyncer.client

import filesyncer.common.FSEventConnWorker
import filesyncer.common.FSEventMessageHandler
import java.io.DataOutputStream
import java.io.File
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import message.FMEVENT_TYPE
import message.FSEventMessage

enum class FSCLIENT_STATE {
    DISCONNECTED,
    CONNECTED,
    LOGGEDIN
}

enum class REQUEST_STATE {
    NOT_WAITING,
    WAITING,
    GRANTED,
    REJECTED
}

class Client(var localRepoDir: File) : FSEventMessageHandler, FSClientFrontInterface {
    var state = FSCLIENT_STATE.DISCONNECTED
    var runner: FSEventConnWorker? = null
    var front: FSClientFront? = null

    var address = ""
    var port = 500

    private var waitingLoginRequest = REQUEST_STATE.NOT_WAITING
    private var waitingRegisterRequest = REQUEST_STATE.NOT_WAITING
    private var waitingListFolderRequest = REQUEST_STATE.NOT_WAITING

    private var _id = ""
    private var _passwd = ""
    private var _cloudFileLists: List<String> = mutableListOf()

    private val _reportMsgQueue = LinkedBlockingQueue<String>()

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
        runner = FSEventConnWorker(socket, this)
        runner!!.run()
    }

    override fun handleMessage(msg: FSEventMessage) {
        println("client code = ${msg.mEventcode}, id: ${msg.userIdField.str}")
        when (msg.mEventcode) {
            FMEVENT_TYPE.NONE -> {
                // do nothing
            }
            FMEVENT_TYPE.ASK_ALIVE -> {}
            FMEVENT_TYPE.ANSWER_ALIVE -> {}
            FMEVENT_TYPE.LOGIN_REQUEST -> {}
            FMEVENT_TYPE.LOGIN_GRANTED -> {
                waitingLoginRequest = REQUEST_STATE.GRANTED
                state = FSCLIENT_STATE.LOGGEDIN
            }
            FMEVENT_TYPE.LOGIN_REJECTED -> {
                waitingLoginRequest = REQUEST_STATE.REJECTED
            }
            FMEVENT_TYPE.LOGOUT -> {}
            FMEVENT_TYPE.BROADCAST_CONNECTED -> {
                _reportMsgQueue.put("${msg.userIdField.str} connected.")
            }
            FMEVENT_TYPE.BROADCAST_DISCONNECTED -> {
                _reportMsgQueue.put("${msg.userIdField.str} disconnected.")
            }
            FMEVENT_TYPE.UPLOAD_DONE -> {}
            FMEVENT_TYPE.DOWNLOAD_DONE -> {}
            FMEVENT_TYPE.UPLOAD_REQUEST -> {}
            FMEVENT_TYPE.DOWNLOAD_REQUEST -> {}
            FMEVENT_TYPE.REGISTER_REQUEST -> {}
            FMEVENT_TYPE.REGISTER_GRANTED -> {
                waitingRegisterRequest = REQUEST_STATE.GRANTED
            }
            FMEVENT_TYPE.REGISTER_REJECTED -> {
                waitingRegisterRequest = REQUEST_STATE.REJECTED
            }
            FMEVENT_TYPE.LISTFOLDER_RESPONSE -> {
                waitingListFolderRequest = REQUEST_STATE.GRANTED
                _cloudFileLists = msg.fileListField.strs.toList()
            }
            else -> {
                // TODO("Raise error or ..")
            }
        }
    }

    override fun checkConnection(address: String, port: Int): Boolean {
        if (
            (runner != null) &&
                runner!!.connection.isConencted() &&
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
            waitingLoginRequest = REQUEST_STATE.WAITING
            runner!!.putMsgToSendQueue(FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, id, password))
        } catch (e: Exception) {
            waitingLoginRequest = REQUEST_STATE.NOT_WAITING
            return false
        }

        while (waitingLoginRequest == REQUEST_STATE.WAITING) {
            Thread.sleep(200L)
        }

        if (waitingLoginRequest == REQUEST_STATE.GRANTED) {
            waitingLoginRequest = REQUEST_STATE.NOT_WAITING
            _id = id
            _passwd = password
            return true
        } else if (waitingLoginRequest == REQUEST_STATE.REJECTED) {

            waitingLoginRequest = REQUEST_STATE.NOT_WAITING
            return false
        }

        waitingLoginRequest = REQUEST_STATE.NOT_WAITING
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
            waitingRegisterRequest = REQUEST_STATE.WAITING
            runner!!.putMsgToSendQueue(FSEventMessage(FMEVENT_TYPE.REGISTER_REQUEST, id, password))
        } catch (e: Exception) {
            waitingRegisterRequest = REQUEST_STATE.NOT_WAITING
            return false
        }

        while (waitingRegisterRequest == REQUEST_STATE.WAITING) {
            Thread.sleep(200L)
        }

        if (waitingRegisterRequest == REQUEST_STATE.GRANTED) {

            waitingRegisterRequest = REQUEST_STATE.NOT_WAITING
            return true
        } else if (waitingRegisterRequest == REQUEST_STATE.REJECTED) {

            waitingRegisterRequest = REQUEST_STATE.NOT_WAITING
            return false
        }
        waitingRegisterRequest = REQUEST_STATE.NOT_WAITING
        return false
    }

    override fun showFolder(dir: String): List<Map<String, String>> {
        val localFiles =
            this.localRepoDir.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()

        try {
            waitingListFolderRequest = REQUEST_STATE.WAITING
            runner!!.putMsgToSendQueue(FSEventMessage(FMEVENT_TYPE.LISTFOLDER_REQUEST))
        } catch (e: Exception) {
            waitingListFolderRequest = REQUEST_STATE.NOT_WAITING
            return emptyList()
        }

        while (waitingListFolderRequest == REQUEST_STATE.WAITING) {
            Thread.sleep(200L)
        }
        waitingListFolderRequest = REQUEST_STATE.NOT_WAITING

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
        runner?.putMsgToSendQueue(FSEventMessage(FMEVENT_TYPE.LOGOUT, _id, _passwd))
        runner?.stop()
    }

    override fun takeReportMessage(): String {
        return _reportMsgQueue.take()
    }
}
