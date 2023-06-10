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
    private var waitingListUserRequest = FSRequestFsm()

    private var _id = ""
    private var _passwd = ""

    private val _reportMsgQueue = LinkedBlockingQueue<String>()

    private var _localRepoWatcher: FileWatcher? = null

    private val logicalClock = FSLogicalClock()
    private var fileTransfer: FSFileTransfer? = FSFileTransfer()

    // private var _localFileLists = mutableListOf<FSFileMetaData>()
    private var _cloudFileLists = mutableListOf<FSFileMetaData>()

    private var fileManager: FSClientFileManager? = null

    private var _userList: List<String> = emptyList()

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
        println(
            "client code = ${msg.mEventcode}, timeStamp=${msg.mTimeStamp} msg: ${msg.messageField.strs}"
        )
        logicalClock.sync(msg.mTimeStamp)
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
                waitingSyncRequest.grant()
            }
            EventType.FILE_CREATE -> {
                // TODO : download from server
                runner!!.putMsgToSendQueue(
                    FSEventMessage(
                        EventType.DOWNLOAD_REQUEST,
                        logicalClock.get(),
                        msg.messageField.strs[0]
                    )
                )
            }
            EventType.FILE_DELETE -> {
                val fileName = msg.messageField.strs[0]
                fileManager!!.localFolder.resolve(fileName).delete()
            }
            EventType.FILE_MODIFY -> {
                val cloudMetaData = FSFileMetaData()
                cloudMetaData.fromStringArray(msg.messageField.strs.toTypedArray())

                val localFilePath = fileManager!!.localFolder.resolve(cloudMetaData.name)
                if (!localFilePath.exists()) {
                    download(cloudMetaData, localFilePath)
                    return
                }
                val localMd5 = FSFileHash.md5(localFilePath)

                if (localMd5 != cloudMetaData.md5) {
                    // TODO : check collision
                    runner!!.putMsgToSendQueue(
                        FSEventMessage(
                            EventType.DOWNLOAD_REQUEST,
                            logicalClock.get(),
                            msg.messageField.strs[0]
                        )
                    )
                    download(cloudMetaData, localFilePath)
                }
            }
            EventType.LIST_USER_RESPONSE -> {
                _userList = msg.messageField.strs
                waitingListUserRequest.grant()
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
            runner!!.putMsgToSendQueue(
                FSEventMessage(EventType.FILE_CREATE, logicalClock.get(), name)
            )
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            println("${name} removed")
            // tell delete
            val metaData = fileManager!!.getMetaData(name)!!
            runner!!.putMsgToSendQueue(
                FSEventMessage(EventType.FILE_DELETE, logicalClock.get(), *metaData.toStringArray())
            )
            fileManager!!.deleteMetaData(metaData)
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            println("${name} modified")

            // upload
            var metaData = fileManager!!.getMetaData(name)
            if (metaData == null) {
                metaData = FSFileMetaData(file.name)
                metaData.owner = _id
            }
            metaData.fileSize = file.length()
            metaData.md5 = FSFileHash.md5(file)
            metaData.timeStamp = logicalClock.get()

            fileManager!!.registerMetaData(metaData)
            fileManager!!.saveMetaData(metaData)

            upload(name)

            runner!!.putMsgToSendQueue(
                FSEventMessage(EventType.FILE_MODIFY, logicalClock.get(), name)
            )
        }
    }

    fun syncLogicalClock() {
        logicalClock.get()
        try {
            runner!!.putMsgToSendQueue(FSEventMessage(EventType.SYNC, logicalClock.get()))
        } catch (e: Exception) {
            return
        }

        waitingSyncRequest.waitLock()
        waitingSyncRequest.reset()
    }

    /** Send local metadata -> get cloud metadata -> decision -> upload or not */
    private fun upload(fileName: String) {
        val file = fileManager!!.localFolder.resolve(fileName)
        val socket = Socket(address, uploadPort)
        val socketOutputStream = socket.getOutputStream()
        val socketInputStream = socket.getInputStream()
        val fileInputStream = file.inputStream()
        val metaData = fileManager!!.getMetaData(file.name)!!

        // send local metadata
        fileTransfer!!.sendMetaData(_id, metaData, socketOutputStream)

        // download cloud metadata
        val metadata = fileTransfer!!.receiveMetaData(socketInputStream).toFileMetaData()
        // TODO("Check collision")

        // upload
        socketOutputStream.use { fileInputStream.copyTo(it) }

        socket.close()
        fileInputStream.close()
    }

    /** Send local metadata -> get cloud metadata -> decision -> download or not */
    private fun download(metadata: FSFileMetaData, file: File) {
        val socket = Socket(address, downloadPort)
        val socketInputStream = socket.getInputStream()
        val socketOutputStream = socket.getOutputStream()

        // send local metadata
        fileTransfer!!.sendMetaData(_id, metadata, socketOutputStream)

        // download cloud metadata
        val fileOutputStream = file.outputStream()
        val cloudMetadata = fileTransfer!!.receiveMetaData(socketInputStream).toFileMetaData()
        // TODO("Check collision")

        // download file
        fileOutputStream.use { socketInputStream.copyTo(it) }

        fileManager!!.registerMetaData(cloudMetadata)
        fileManager!!.saveMetaData(cloudMetadata)

        fileOutputStream.close()
        socket.close()
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

    private fun getUserList(): List<String> {
        try {
            runner!!.putMsgToSendQueue(
                FSEventMessage(EventType.LIST_USER_REQUEST, logicalClock.get())
            )
        } catch (e: Exception) {
            return emptyList()
        }

        waitingListUserRequest.waitLock()

        return _userList
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
                        FSEventMessage(EventType.LOGIN_REQUEST, logicalClock.get(), id, password)
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
                        FSEventMessage(EventType.REGISTER_REQUEST, logicalClock.get(), id, password)
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
                    runner!!.putMsgToSendQueue(
                        FSEventMessage(EventType.LISTFOLDER_REQUEST, logicalClock.get(), _id)
                    )
                } catch (e: Exception) {
                    return emptyList()
                }

                waitingListFolderRequest.waitLock()
                waitingListFolderRequest.reset()

                // val cloudFiles: List<FSFileMetaData> = _cloudFileLists.map { it.name }
                // val _localFileLists: List<FSFileMetaData> =
                // fileManager!!.metaDataMap.values.toList()

                // val lists = HashSet(_localFileLists + cloudFiles).toTypedArray()
                // lists.sort()
                // val localSet = HashSet(_localFileLists)
                // val cloudSet = HashSet(cloudFiles)
                val data = _cloudFileLists.map { it.toMap() }
                return data
            }

            override fun uploadFile(path: String): Boolean {
                upload(path)
                return true
            }

            override fun disconnect() {
                runner?.putMsgToSendQueue(
                    FSEventMessage(EventType.LOGOUT, logicalClock.get(), _id, _passwd)
                )
                runner?.stop()
                fileTransfer = null

                _localRepoWatcher!!.stop()
            }

            override fun takeReportMessage(): String? {
                return _reportMsgQueue.poll()
            }

            override fun listUsers(): List<String> {
                return getUserList()
            }

            override fun shareFile(metadata: FSFileMetaData) {
                fileManager!!.registerMetaData(metadata)
                fileManager!!.saveMetaData(metadata)
                runner?.putMsgToSendQueue(
                    FSEventMessage(
                        EventType.SHARE_FILE,
                        logicalClock.get(),
                        *metadata.toStringArray()
                    )
                )
            }
        }
}
