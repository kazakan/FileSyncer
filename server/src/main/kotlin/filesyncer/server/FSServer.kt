package filesyncer.server

import filesyncer.common.FSLogicalClock
import filesyncer.common.FSUser
import filesyncer.common.FSUserManager
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import message.FSEventMessage
import message.FSEventMessage.EventType

class FSServer(var rootPath: File, var port: Int = 5050, val verbose: Boolean = false) :
    FSMessageBroadcaster<FSEventMessage>, FSUserManager.FSUserManagerListener {

    var userManager = FSSimpleUserManager(rootPath, verbose, this)
    var running = false
    var repoDir = rootPath.resolve(File("repo"))

    var logicalClock = FSLogicalClock()

    // main loop accepts connection from client
    var mainloopThread = Thread {
        val ss = ServerSocket(port)
        running = true
        while (running) {
            val socket = ss.accept()
            val worker = FSServerSideSession(socket, userManager, repoDir, logicalClock, verbose)
            if (verbose) {
                println(
                    "User tried make connection. IP : ${socket.inetAddress.hostAddress}, PORT : ${socket.port}"
                )
            }
        }
    }

    var fileDownloadLoopThread = Thread {
        val fss = ServerSocket(7777)
        while (running) {
            val socket = fss.accept()
            val fileDownloadWorkerThread = Thread {
                if (verbose) {
                    println("Start download file from ${socket.remoteSocketAddress.toString()}")
                }
                val ios = socket.getInputStream()
                val dios = DataInputStream(ios)
                val fsize = dios.readLong()
                val fnameLen = dios.readInt()
                val fnameByteArr = ios.readNBytes(fnameLen)
                val fname = String(fnameByteArr)

                val fos = FileOutputStream(repoDir.resolve(File(fname)))

                fos.use { ios.copyTo(it) }

                socket.close()
                if (verbose) {
                    println("Done download file")
                }
                broadcast(FSEventMessage(EventType.UPLOAD_DONE, fname))
            }

            fileDownloadWorkerThread.start()
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
        fileDownloadLoopThread.start()
    }

    fun kill() {
        mainloopThread.interrupt()
        // removeDeadSessionThread.interrupt()
        fileDownloadLoopThread.interrupt()
    }

    // broadcast message to all alive event connections
    override fun broadcast(msg: FSEventMessage) {
        println("Broadcast msg code=${msg.mEventcode}, ID=${msg.messageField.strs[0]}")
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
}
