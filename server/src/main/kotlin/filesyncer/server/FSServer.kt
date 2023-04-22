package filesyncer.server

import filesyncer.common.FSEventConnWorker
import filesyncer.common.FSEventMessageHandler
import filesyncer.common.FSUser
import filesyncer.common.FSUserManager
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import message.FMEVENT_TYPE
import message.FSEventMessage
import message.FSVarLenStringListField

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
            val worker = createConnWorker(socket)
            if (verbose) {
                println(
                    "User tried make connection. IP : ${socket.inetAddress.hostAddress}, PORT : ${socket.port}"
                )
            }

            worker.run()
            ++i
        }
    }

    var removeDeadSessionThread = Thread {
        while (true) {
            userManager.removeClosedConnection()
            Thread.sleep(500)
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
        removeDeadSessionThread.start()
        fileDownloadLoopThread.start()
    }

    fun kill() {
        mainloopThread.interrupt()
        removeDeadSessionThread.interrupt()
        fileDownloadLoopThread.interrupt()
    }

    override fun broadcast(msg: FSEventMessage) {
        println("Broadcast msg code=${msg.mEventcode}, ID=${msg.userIdField.str}")
        for (entry in userManager.sessions) {
            if (!entry.value.isClosed()) entry.value.putMsgToSendQueue(msg)
        }
    }

    fun createConnWorker(socket: Socket): FSEventConnWorker {
        val connWorker = FSEventConnWorker(socket)
        val handler =
            object : FSEventMessageHandler {
                override fun handleMessage(msg: FSEventMessage) {
                    if (verbose)
                        println("Got Msg with Code=${msg.mEventcode}, ID=${msg.userIdField.str}")
                    when (msg.mEventcode) {
                        FMEVENT_TYPE.ANSWER_ALIVE -> {}
                        FMEVENT_TYPE.LOGIN_REQUEST -> {
                            // check user and make response
                            if (verbose) println("Got login requested by ${msg.userIdField.str}")
                            val user = FSUser(msg.userIdField.str, msg.userPasswordField.str)
                            if (userManager.userExists(user)) {
                                if (userManager.addUserSession(user, connWorker) == null) {
                                    // already logged in other device
                                    if (verbose)
                                        println(
                                            "Login requested by ${msg.userIdField.str} rejected."
                                        )
                                    connWorker.putMsgToSendQueue(
                                        FSEventMessage(FMEVENT_TYPE.LOGIN_REJECTED)
                                    )
                                } else {
                                    // granted
                                    if (verbose)
                                        println(
                                            "Login requested by ${msg.userIdField.str} granted."
                                        )
                                    connWorker.putMsgToSendQueue(
                                        FSEventMessage(FMEVENT_TYPE.LOGIN_GRANTED)
                                    )
                                }
                            } else {
                                // rejected
                                if (verbose)
                                    println("Login requested by ${msg.userIdField.str} rejected.")
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(FMEVENT_TYPE.LOGIN_REJECTED)
                                )
                            }
                        }
                        FMEVENT_TYPE.LOGOUT -> {
                            // logout and broadcast msg
                            val user = FSUser(msg.userIdField.str, msg.userPasswordField.str)
                            if (verbose) println("User ${msg.userIdField.str} logged out.")
                            // userManager.removeUserSession(user)
                        }
                        FMEVENT_TYPE.UPLOAD_DONE -> {
                            // check result and send download done
                        }
                        FMEVENT_TYPE.DOWNLOAD_DONE -> {}
                        FMEVENT_TYPE.UPLOAD_REQUEST -> {
                            // make file upload connection
                        }
                        FMEVENT_TYPE.DOWNLOAD_REQUEST -> {}
                        FMEVENT_TYPE.REGISTER_REQUEST -> {
                            val user = FSUser(msg.userIdField.str, msg.userPasswordField.str)
                            val result = userManager.registerUser(user)
                            if (result) {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(FMEVENT_TYPE.REGISTER_GRANTED)
                                )
                                if (verbose) println("User ${msg.userIdField.str} registered.")
                            } else {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(FMEVENT_TYPE.REGISTER_REJECTED)
                                )
                                if (verbose)
                                    println("Register of User ${msg.userIdField.str} rejected.")
                            }
                        }
                        FMEVENT_TYPE.LISTFOLDER_REQUEST -> {
                            val names = repoDir.listFiles()?.filter { it.isFile }?.map { it.name }
                            if (verbose) println("List folder request handled.")
                            if (names != null) {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(FMEVENT_TYPE.LISTFOLDER_RESPONSE).apply {
                                        this.fileListField = FSVarLenStringListField(names)
                                    }
                                )
                            }
                        }
                        else -> {
                            // TODO("Do nothing.")
                        }
                    }
                }
            }

        connWorker.eventHandler = handler

        return connWorker
    }

    override fun onUserSessionRemoved(user: FSUser) {
        this@FSServer.broadcast(
            FSEventMessage(FMEVENT_TYPE.BROADCAST_DISCONNECTED, userIdStr = user.id)
        )
    }

    override fun onUserSessionAdded(user: FSUser) {
        this@FSServer.broadcast(
            FSEventMessage(FMEVENT_TYPE.BROADCAST_CONNECTED, userIdStr = user.id)
        )
    }
}
