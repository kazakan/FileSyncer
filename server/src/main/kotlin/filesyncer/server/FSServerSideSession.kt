package filesyncer.server

import filesyncer.common.*
import java.io.File
import java.net.Socket
import message.FSEventMessage
import message.FSEventMessage.EventType
import message.FSVarLenStringListField

class FSServerSideSession(
    socket: Socket,
    val userManager: FSUserManager,
    val repoDir: File,
    val clock: FSLogicalClock,
    verbose: Boolean = false
) : FSSession(socket, verbose) {

    override fun createConnWorker(socket: Socket): FSEventConnWorker {
        val connWorker = FSEventConnWorker(socket, verbose = verbose)
        // message handler
        val handler =
            object : FSEventMessageHandler {
                override fun handleMessage(msg: FSEventMessage) {
                    if (verbose)
                        println(
                            "Got Msg with Code=${msg.mEventcode}, ID=${msg.messageField.strs[0]}"
                        )
                    when (msg.mEventcode) {
                        EventType.ANSWER_ALIVE -> {}
                        EventType.LOGIN_REQUEST -> {
                            // check user and make response
                            if (verbose)
                                println("Got login requested by ${msg.messageField.strs[0]}")
                            val _user = FSUser(msg.messageField.strs[0], msg.messageField.strs[1])
                            if (userManager.userExists(_user)) {
                                if (
                                    userManager.addUserSession(_user, this@FSServerSideSession) ==
                                        null
                                ) {
                                    // already logged in other device
                                    if (verbose)
                                        println(
                                            "Login requested by ${msg.messageField.strs[0]} rejected."
                                        )
                                    connWorker.putMsgToSendQueue(
                                        FSEventMessage(EventType.LOGIN_REJECTED)
                                    )
                                } else {
                                    // granted
                                    if (verbose)
                                        println(
                                            "Login requested by ${msg.messageField.strs[0]} granted."
                                        )
                                    user = _user
                                    state = State.LOGGED_IN
                                    connWorker.putMsgToSendQueue(
                                        FSEventMessage(EventType.LOGIN_GRANTED)
                                    )
                                }
                            } else {
                                // rejected
                                if (verbose)
                                    println(
                                        "Login requested by ${msg.messageField.strs[0]} rejected."
                                    )
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(EventType.LOGIN_REJECTED)
                                )
                            }
                        }
                        EventType.LOGOUT -> {
                            // logout and broadcast msg
                            val user = FSUser(msg.messageField.strs[0], msg.messageField.strs[1])
                            if (verbose) println("User ${msg.messageField.strs[0]} logged out.")
                            // userManager.removeUserSession(user)
                        }
                        EventType.REGISTER_REQUEST -> {
                            val user = FSUser(msg.messageField.strs[0], msg.messageField.strs[1])
                            val result = userManager.registerUser(user)
                            if (result) {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(EventType.REGISTER_GRANTED)
                                )
                                if (verbose) println("User ${msg.messageField.strs[0]} registered.")
                            } else {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(EventType.REGISTER_REJECTED)
                                )
                                if (verbose)
                                    println(
                                        "Register of User ${msg.messageField.strs[0]} rejected."
                                    )
                            }
                        }
                        EventType.LISTFOLDER_REQUEST -> {
                            val names = repoDir.listFiles()?.filter { it.isFile }?.map { it.name }
                            if (verbose) println("List folder request handled.")
                            if (names != null) {
                                connWorker.putMsgToSendQueue(
                                    FSEventMessage(EventType.LISTFOLDER_RESPONSE).apply {
                                        this.messageField = FSVarLenStringListField(names)
                                    }
                                )
                            }
                        }
                        EventType.SYNC -> {
                            val clientTime = msg.messageField.strs[0].toLong()
                            clock.sync(clientTime)
                            connWorker.putMsgToSendQueue(
                                FSEventMessage(EventType.SYNC, "${clock.get()}")
                            )
                        }
                        else -> {
                            // TODO("Do nothing.")
                        }
                    }
                }
            }

        connWorker.eventHandler = handler

        val onStateChanged =
            object : FSEventConnWorker.StateChangeListener {
                override fun onStart() {
                    // do nothing
                }

                override fun onClose() {

                    state = State.CLOSED
                    if (user != null) {
                        // remove session from user
                        userManager.removeUserSession(user!!)
                    }
                }
            }

        connWorker.listener = onStateChanged

        return connWorker
    }
}
