package server

import FSEventConnWorker
import FSEventMessageHandler
import FSUser
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.util.Vector
import message.FMEVENT_TYPE
import message.FSEventMessage

class FSServer(var rootPath: File, var port: Int = 5050) : FSMessageBroadcaster<FSEventMessage> {

    var sessions = Vector<FSEventConnWorker>()

    var userManager = FSSimpleUserManager(rootPath)
    var running = false

    var mainloopThread = Thread {
        var ss = ServerSocket(port)
        running = true
        var i = 0
        while (running) {
            println("server main loop thread loop : $i")
            val socket = ss.accept()
            val worker = createConnWorker(socket)
            println(
                "User tried make connection. IP : ${socket.inetAddress.hostAddress}, PORT : ${socket.port}"
            )
            worker.run()
            sessions.add(worker)
            ++i
        }
    }

    var removeDeadSessionThread = Thread {
        while (true) {
            userManager.removeClosedConnection()
            Thread.sleep(500)
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
        userManager.initialize()
    }

    fun start() {
        println("FSServer started.")
        mainloopThread.start()
        removeDeadSessionThread.start()
    }

    fun kill() {
        mainloopThread.interrupt()
        removeDeadSessionThread.interrupt()
    }

    override fun broadcast(msg: FSEventMessage) {
        for (session in sessions) {
            if (!session.isClosed()) session.eventSendQueue.put(msg)
        }
    }

    fun createConnWorker(socket: Socket): FSEventConnWorker {
        val connWorker = FSEventConnWorker(socket)
        val handler =
            object : FSEventMessageHandler {
                override fun handleMessage(msg: FSEventMessage) {
                    when (msg.mEventcode) {
                        FMEVENT_TYPE.ANSWER_ALIVE -> {}
                        FMEVENT_TYPE.LOGIN_REQUEST -> {
                            // check user and make response
                            println("server : get lgin requested by ${msg.userIdField.str}")
                            val user = FSUser(msg.userIdField.str, msg.userPasswordField.str)
                            if (userManager.userExists(user)) {
                                if (userManager.addUserSession(user, connWorker) == null) {
                                    // already logged in other device
                                    println(
                                        "server : login requested by ${msg.userIdField.str} rejected."
                                    )
                                    connWorker.putMsgToSendQueue(
                                        FSEventMessage(FMEVENT_TYPE.LOGIN_REJECTED)
                                    )
                                } else {
                                    // granted
                                    println(
                                        "server : login requested by ${msg.userIdField.str} granted."
                                    )
                                    connWorker.putMsgToSendQueue(
                                        FSEventMessage(FMEVENT_TYPE.LOGIN_GRANTED)
                                    )
                                    this@FSServer.broadcast(
                                        FSEventMessage(
                                            FMEVENT_TYPE.BROADCAST_CONNECTED,
                                            userIdStr = user.id
                                        )
                                    )
                                }
                            } else {
                                // rejected
                                println(
                                    "server : login requested by ${msg.userIdField.str} rejected."
                                )
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(FMEVENT_TYPE.LOGIN_REJECTED)
                                )
                            }
                        }
                        FMEVENT_TYPE.LOGOUT -> {
                            // logout and broadcast msg
                            val user = FSUser(msg.userIdField.str, msg.userPasswordField.str)
                            userManager.removeUserSession(user)
                            this@FSServer.broadcast(
                                FSEventMessage(
                                    FMEVENT_TYPE.BROADCAST_CONNECTED,
                                    userIdStr = msg.userIdField.str
                                )
                            )
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
                            } else {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(FMEVENT_TYPE.REGISTER_REJECTED)
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
}
