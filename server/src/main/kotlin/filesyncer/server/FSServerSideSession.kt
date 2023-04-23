package filesyncer.server

import filesyncer.common.*
import java.io.File
import java.net.Socket
import message.FMEVENT_TYPE
import message.FSEventMessage
import message.FSVarLenStringListField

class FSServerSideSession(
    socket: Socket,
    val userManager: FSUserManager,
    val repoDir: File,
    verbose: Boolean = false
) : FSSession(socket, verbose) {

    override fun createConnWorker(socket: Socket): FSEventConnWorker {
        val connWorker = FSEventConnWorker(socket, verbose = verbose)
        // message handler
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
                            val _user = FSUser(msg.userIdField.str, msg.userPasswordField.str)
                            if (userManager.userExists(_user)) {
                                if (
                                    userManager.addUserSession(_user, this@FSServerSideSession) ==
                                        null
                                ) {
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
                                    user = _user
                                    state = State.LOGGED_IN
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
