package filesyncer.server

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

    var mainloopThread = Thread {
        var ss = ServerSocket(port)
        running = true
        var i = 0
        while (running) {

            val socket = ss.accept()
            val worker = FSServerSideSession(socket, userManager, repoDir, verbose)
            if (verbose) {
                println(
                    "User tried make connection. IP : ${socket.inetAddress.hostAddress}, PORT : ${socket.port}"
                )
            }
        }
    }

    var fileDownloadLoopThread = Thread {
        var fss = ServerSocket(7777)
        while (running) {
            var socket = fss.accept()
            val fileDownloadWorkerThread = Thread {
                if (verbose) {
                    println("Start download file from ${socket.remoteSocketAddress.toString()}")
                }
                var ios = socket.getInputStream()
                var dios = DataInputStream(ios)
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
                broadcast(FSEventMessage(EventType.UPLOAD_DONE, extraStr = fname))
            }

            fileDownloadWorkerThread.start()
        }
    }

    fun initialize() {
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

    override fun broadcast(msg: FSEventMessage) {
        println("Broadcast msg code=${msg.mEventcode}, ID=${msg.userIdField.str}")
        for (entry in userManager.sessions) {
            if (!entry.value.worker.isClosed()) entry.value.worker.putMsgToSendQueue(msg)
        }
    }

    override fun onUserSessionRemoved(user: FSUser) {
        this@FSServer.broadcast(
            FSEventMessage(EventType.BROADCAST_DISCONNECTED, userIdStr = user.id)
        )
    }

    override fun onUserSessionAdded(user: FSUser) {
        this@FSServer.broadcast(FSEventMessage(EventType.BROADCAST_CONNECTED, userIdStr = user.id))
    }
}
