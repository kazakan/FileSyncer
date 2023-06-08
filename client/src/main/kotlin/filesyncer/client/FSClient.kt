package filesyncer.client

import filesyncer.common.*
import filesyncer.common.file.FSFileMetaData
import filesyncer.common.file.FSFileTransfer
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

class Client(var localRepoDir: File) : FSEventMessageHandler, FileWatcher.OnFileChangedListener {
    var state = FSCLIENT_STATE.DISCONNECTED
    var runner: FSEventConnWorker? = null
    var front: FSClientFront? = null

    var address = ""
    var port = 500

    val uploadPort = 7777
    val downloadPort = 7778

    private var waitingLoginRequest = FSRequestFsm()
    private var waitingRegisterRequest = FSRequestFsm()
    private var waitingListFolderRequest = FSRequestFsm()
    private var waitingSyncRequest = FSRequestFsm()

    private var _id = ""
    private var _passwd = ""

    private val _reportMsgQueue = LinkedBlockingQueue<String>()

    private var _localRepoWatcher: FileWatcher? = null

    private val logicalClock = FSLogicalClock()
    private var fileTransfer: FSFileTransfer? = FSFileTransfer()

    // private var _localFileLists = mutableListOf<FSFileMetaData>()
    private var _cloudFileLists = mutableListOf<FSFileMetaData>()

    private var fileManager: FSClientFileManager? = null

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

        syncLogicalClock()
    }

    override fun handleMessage(msg: FSEventMessage) {
        println("client code = ${msg.mEventcode}, msg: ${msg.messageField.strs}")
        when (msg.mEventcode) {
            EventType.LOGIN_GRANTED -> {
                waitingLoginRequest.grant()
                state = FSCLIENT_STATE.LOGGEDIN
            }
            EventType.LOGIN_REJECTED -> {
                waitingLoginRequest.reject()
            }
            EventType.LOGOUT -> {}
            EventType.BROADCAST_CONNECTED -> {
                _reportMsgQueue.put("${msg.messageField.strs[0]} connected.")
            }
            EventType.BROADCAST_DISCONNECTED -> {
                _reportMsgQueue.put("${msg.messageField.strs[0]} disconnected.")
            }
            EventType.UPLOAD_DONE -> {
                _reportMsgQueue.put("${msg.messageField.strs[2]} updated to server.")
            }
            EventType.REGISTER_GRANTED -> {
                waitingRegisterRequest.grant()
            }
            EventType.REGISTER_REJECTED -> {
                waitingRegisterRequest.reject()
            }
            EventType.LISTFOLDER_RESPONSE -> {
                waitingListFolderRequest.grant()

                _cloudFileLists = mutableListOf()

                val nFiles = msg.messageField.strs.size / 6

                for (i in 0 until nFiles) {
                    val data = FSFileMetaData()
                    data.fromStringArray(
                        msg.messageField.strs.slice(IntRange(i * 6, i * 6 + 5)).toTypedArray()
                    )
                    _cloudFileLists = _cloudFileLists.plus(data).toMutableList()
                }
            }
            EventType.SYNC -> {
                val serverClockValue = msg.messageField.strs[0].toLong()
                logicalClock.sync(serverClockValue)
                waitingSyncRequest.grant()
            }
            EventType.FILE_CREATE -> {
                // TODO : download from server
                runner!!.putMsgToSendQueue(
                    FSEventMessage(EventType.DOWNLOAD_REQUEST, msg.messageField.strs[0])
                )
            }
            EventType.FILE_DELETE -> {
                // TODO : remove file in client
                val fileName = msg.messageField.strs[0]
                fileManager!!.localFolder.resolve(fileName).delete()
            }
            EventType.FILE_MODIFY -> {
                val fileName = msg.messageField.strs[0]
                val cloudMd5 = msg.messageField.strs[1]

                val localFilePath = fileManager!!.localFolder.resolve(fileName)
                val localMd5 = FSFileHash.md5(localFilePath)

                if (localMd5 != cloudMd5) {
                    // TODO : download from server
                    runner!!.putMsgToSendQueue(
                        FSEventMessage(EventType.DOWNLOAD_REQUEST, msg.messageField.strs[0])
                    )
                }
            }
            else -> {
                // TODO("Raise error or ..")
            }
        }
    }

    override fun onFileChanged(file: File, kind: WatchEvent.Kind<out Any>) {
        val name = file.name
        val file = fileManager!!.localFolder.resolve(name)
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            println("${name} created")
            // upload
            val metaData = FSFileMetaData()

            metaData.name = name
            metaData.owner = _id
            metaData.fileSize = file.length()
            metaData.md5 = FSFileHash.md5(file)
            metaData.timeStamp = logicalClock.get()

            fileManager!!.registerMetaData(metaData)
            fileManager!!.saveMetaData(metaData)

            upload(name)
            runner!!.putMsgToSendQueue(FSEventMessage(EventType.FILE_CREATE, name))
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            println("${name} removed")
            // tell delete
            runner!!.putMsgToSendQueue(FSEventMessage(EventType.FILE_DELETE, name))
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            println("${name} modified")

            // upload
            val metaData = fileManager!!.getMetaData(name)!!
            metaData.fileSize = file.length()
            metaData.md5 = FSFileHash.md5(file)
            metaData.timeStamp = logicalClock.get()

            fileManager!!.registerMetaData(metaData)
            fileManager!!.saveMetaData(metaData)

            upload(name)

            runner!!.putMsgToSendQueue(FSEventMessage(EventType.FILE_MODIFY, name))
        }
    }

    fun syncLogicalClock() {
        logicalClock.get()
        try {
            runner!!.putMsgToSendQueue(FSEventMessage(EventType.SYNC, "${logicalClock.time}"))
        } catch (e: Exception) {
            return
        }

        waitingSyncRequest.waitLock()
        waitingSyncRequest.reset()
    }

    private fun upload(fileName: String) {
        val file = fileManager!!.localFolder.resolve(fileName)
        val socket = Socket(address, uploadPort)
        val socketOutputStream = socket.getOutputStream()
        val fileInputStream = file.inputStream()
        val metaData = fileManager!!.getMetaData(file.name)!!

        fileTransfer!!.sendMetaData(_id, metaData, socketOutputStream)

        socketOutputStream.use { fileInputStream.copyTo(it) }
    }

    private fun download(metadata: FSFileMetaData, file: File) {
        val socket = Socket(address, downloadPort)
        val socketInputStream = socket.getInputStream()
        val socketOutputStream = socket.getOutputStream()

        // send metadata to download
        fileTransfer!!.sendMetaData(_id, metadata, socketOutputStream)

        // download metadata & file
        val fileOutputStream = file.outputStream()
        val metadata = fileTransfer!!.receiveMetaData(socketInputStream).toFileMetaData()
        fileOutputStream.use { socketInputStream.copyTo(it) }

        fileManager!!.registerMetaData(metadata)
        fileManager!!.saveMetaData(metadata)
    }

    private fun resolveDifference(cloudFileList: List<FSFileMetaData>) {

        val localList = fileManager!!.metaDataMap.values.toMutableList()
        val cloudMap = HashMap<String, FSFileMetaData>()

        for (metaData in cloudFileList) {
            cloudMap[metaData.name] = metaData
        }

        for (local in localList) {
            if (cloudMap.contains(local.name)) {
                val cloudMetaData = cloudMap[local.name]
                if (cloudMetaData!!.md5 != local.md5) {
                    if (local.timeStamp <= cloudMetaData.timeStamp) {} else {

                        TODO("download")
                    }
                }
            } else {
                // file is in local but not in cloud.
                TODO("upload")
            }
        }
    }

    val frontInterface =
        object : FSClientFrontInterface {

            override fun checkConnection(address: String, port: Int): Boolean {
                if (
                    (runner != null) &&
                        runner!!.connection.isConnected() &&
                        (this@Client.address == address) &&
                        (this@Client.port == port)
                ) {
                    return true
                }

                try {
                    startConnection(address, port)
                } catch (e: Exception) {
                    return false
                }
                this@Client.address = address
                this@Client.port = port
                return true
            }

            override fun requestLogin(
                id: String,
                password: String,
                address: String,
                port: Int
            ): Boolean {
                if (!checkConnection(address, port)) {
                    return false
                }

                try {

                    runner!!.putMsgToSendQueue(
                        FSEventMessage(EventType.LOGIN_REQUEST, id, password)
                    )
                } catch (e: Exception) {

                    return false
                }

                waitingLoginRequest.waitLock()

                if (waitingLoginRequest.state == FSRequestFsm.STATE.GRANTED) {
                    _id = id
                    _passwd = password
                    waitingLoginRequest.reset()

                    // login succeed
                    fileManager =
                        FSClientFileManager(
                            localRepoDir.resolve(_id),
                            localRepoDir.resolve(".${_id}_fsmetadata"),
                            _id
                        )

                    _localRepoWatcher = FileWatcher(fileManager!!.localFolder.toPath())
                    _localRepoWatcher!!.fileChangedListener = this@Client
                    _localRepoWatcher!!.run()

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
                    runner!!.putMsgToSendQueue(
                        FSEventMessage(EventType.REGISTER_REQUEST, id, password)
                    )
                } catch (e: Exception) {
                    return false
                }

                waitingRegisterRequest.waitLock()

                if (waitingRegisterRequest.state == FSRequestFsm.STATE.GRANTED) {
                    _id = id
                    waitingRegisterRequest.reset()
                    return true
                }

                waitingRegisterRequest.reset()
                return false
            }

            override fun showFolder(dir: String): List<Map<String, String>> {

                try {
                    runner!!.putMsgToSendQueue(FSEventMessage(EventType.LISTFOLDER_REQUEST, _id))
                } catch (e: Exception) {
                    return emptyList()
                }

                waitingListFolderRequest.waitLock()
                waitingListFolderRequest.reset()

                val cloudFiles = _cloudFileLists
                val _localFileLists = fileManager!!.metaDataMap.values

                val lists = HashSet(_localFileLists + cloudFiles).toList().sortedBy { it.name }
                val localSet = HashSet(_localFileLists)
                val cloudSet = HashSet(cloudFiles)
                val data =
                    lists.map {
                        val inLocal = localSet.contains(it)
                        val inCloud = cloudSet.contains(it)
                        val status =
                            if (inLocal) if (inCloud) "Both" else "Local Only"
                            else if (inCloud) "Cloud Only" else "ERR"
                        mapOf("name" to it.name, "status" to status, "type" to "file")
                    }
                return data
            }

            override fun uploadFile(path: String): Boolean {
                upload(path)
                return true
            }

            override fun disconnect() {
                runner?.putMsgToSendQueue(FSEventMessage(EventType.LOGOUT, _id, _passwd))
                runner?.stop()
                fileTransfer = null

                _localRepoWatcher!!.stop()
            }

            override fun takeReportMessage(): String? {
                return _reportMsgQueue.poll()
            }
        }
}
