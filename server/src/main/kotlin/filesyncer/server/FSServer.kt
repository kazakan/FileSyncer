package filesyncer.server

import filesyncer.common.FSLogicalClock
import filesyncer.common.FSUser
import filesyncer.common.FSUserManager
import filesyncer.common.file.FSFileMetaData
import filesyncer.common.file.FSFileTransfer
import java.io.File
import java.net.ServerSocket
import message.FSEventMessage
import message.FSEventMessage.EventType

class FSServer(
    var rootPath: File,
    var port: Int = 5050,
    val verbose: Boolean = false,
    val receivePort: Int = 7777,
    val sendPort: Int = 7778
) : FSMessageBroadcaster<FSEventMessage>, FSUserManager.FSUserManagerListener {

    var userManager = FSSimpleUserManager(rootPath, verbose, this)
    var running = false
    var repoDir = rootPath.resolve(File("repo"))

    var logicalClock = FSLogicalClock()
    val fileManager = FSServerFileManager(rootPath)
    var fileTransfer = FSFileTransfer()

    // main loop accepts connection from client
    var mainloopThread = Thread {
        val ss = ServerSocket(port)
        running = true
        while (running) {
            val socket = ss.accept()
            val worker =
                FSServerSideSession(
                    socket,
                    userManager,
                    repoDir,
                    logicalClock,
                    fileManager,
                    verbose
                )
            if (verbose) {
                println(
                    "User tried make connection. IP : ${socket.inetAddress.hostAddress}, PORT : ${socket.port}"
                )
            }
        }
    }

    // client to server
    var receiveThread = Thread {
        val ss = ServerSocket(receivePort)
        running = true
        while (running) {
            val socket = ss.accept()
            val socketInputStream = socket.getInputStream()
            val socketOutputStream = socket.getOutputStream()

            // get client file metadata
            val metaDataMsg = fileTransfer.receiveMetaData(socketInputStream)
            val metaData = metaDataMsg.toFileMetaData()
            val requester = metaDataMsg.requester

            val serverMetadata = fileManager.getNewestMetaData(metaData) ?: FSFileMetaData()
            fileManager.saveMetaData(metaData)

            // send metadata
            fileTransfer.sendMetaData("", serverMetadata, socketOutputStream)

            // TODO("get client decision")

            // receive file
            val file = fileManager.getFile(metaData)
            val fileOutputStream = file.outputStream()

            fileOutputStream.use { socketInputStream.copyTo(it) }

            // broadcast
            broadcast(
                FSEventMessage(EventType.FILE_MODIFY, *metaData.toStringArray()),
                metaData.shared + metaData.owner
            )
        }
    }

    // server to client
    var sendThread = Thread {
        val ss = ServerSocket(sendPort)
        running = true
        while (running) {
            val socket = ss.accept()
            val socketInputStream = socket.getInputStream()
            val socketOutputStream = socket.getOutputStream()

            // get client meta data
            val metaDataMsg = fileTransfer.receiveMetaData(socketInputStream)
            val metaData = metaDataMsg.toFileMetaData()
            val requester = metaDataMsg.requester

            val newestMetaData = fileManager.getNewestMetaData(metaData) ?: metaData

            val file = fileManager.getFile(newestMetaData)
            val fileInputStream = file.inputStream()

            // send metadata
            fileTransfer.sendMetaData("", newestMetaData, socketOutputStream)

            // TODO("get client decision")

            // send file
            socketOutputStream.use { fileInputStream.copyTo(it) }
        }
    }

    fun initialize() {
        // check repository folder is valid
        val rootDir = rootPath
        if (!rootDir.exists()) {
            if (!rootDir.mkdir()) throw Error("Failed to mkdir() $rootDir.")
        } else {
            if (!rootDir.isDirectory)
                throw Error("Repo path($rootDir) is already occupied by other file")
        }

        // create repository file
        if (!repoDir.exists()) {
            if (!repoDir.mkdir()) throw Error("Failed to mkdir() $repoDir.")
        } else {
            if (!repoDir.isDirectory)
                throw Error("Repo path($repoDir) is already occupied by other file")
        }

        userManager.initialize()
    }

    fun start() {
        if (verbose) {
            println("FSServer started.")
        }
        running = true
        mainloopThread.start()
        receiveThread.start()
        sendThread.start()
    }

    fun kill() {
        mainloopThread.interrupt()
        receiveThread.interrupt()
        sendThread.interrupt()
        // removeDeadSessionThread.interrupt()
    }

    // broadcast message to all alive event connections
    override fun broadcast(msg: FSEventMessage) {
        println("Broadcast msg code=${msg.mEventcode}, msg=${msg.messageField.strs}")
        for (entry in userManager.sessions) {
            if (!entry.value.worker.isClosed()) entry.value.worker.putMsgToSendQueue(msg)
        }
    }

    // when a user's session is removed, alert it to all clients.
    override fun onUserSessionRemoved(user: FSUser) {
        this@FSServer.broadcast(FSEventMessage(EventType.BROADCAST_DISCONNECTED, user.id))
    }

    // when a user's session is added, alert it to all clients.
    override fun onUserSessionAdded(user: FSUser) {
        this@FSServer.broadcast(FSEventMessage(EventType.BROADCAST_CONNECTED, user.id))
    }

    override fun broadcast(msg: FSEventMessage, users: List<String>) {
        println("Broadcast msg code=${msg.mEventcode}, msg=${msg.messageField.strs}")
        for (id in users) {
            val fsUser = userManager.findUserById(id)
            if (fsUser != null) {
                if (userManager.sessions[fsUser]?.worker?.isClosed() == false) {
                    userManager.sessions[fsUser]?.worker?.putMsgToSendQueue(msg)
                }
            }
        }
    }
}
