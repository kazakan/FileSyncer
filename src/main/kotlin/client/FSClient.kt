package client

import FSEventConnWorker
import FSEventMessageHandler
import java.net.Socket
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

class Client : FSEventMessageHandler, FSClientFrontInterface {
    var state = FSCLIENT_STATE.DISCONNECTED
    var runner: FSEventConnWorker? = null
    var webfront = FSClientWebFront(this)

    var address = ""
    var port = 500

    var waitingLoginRequest = REQUEST_STATE.NOT_WAITING
    var waitingRegisterRequest = REQUEST_STATE.NOT_WAITING

    fun start() {
        webfront.start()
    }

    fun startConnection(address: String, port: Int) {
        val socket = Socket(address, port)
        runner = FSEventConnWorker(socket, this)
        runner!!.run()
    }

    override fun handleMessage(msg: FSEventMessage) {
        print("client code = ${msg.mEventcode}, id: ${msg.userIdField.str}")
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
            FMEVENT_TYPE.BROADCAST_CONNECTED -> {}
            FMEVENT_TYPE.BROADCAST_DISCONNECTED -> {}
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
        // TODO("implement")
        val data =
            mutableListOf<Map<String, String>>(
                mapOf("name" to "file1.txt", "status" to "local only", "type" to "file"),
                mapOf("name" to "file2.txt", "status" to "cloud only", "type" to "file"),
                mapOf("name" to "file3.docx", "status" to "both", "type" to "file"),
                mapOf("name" to "file4.cpp", "status" to "local only", "type" to "file"),
            )
        return data
    }

    override fun uploadFile(path: String): Boolean {
        // TODO("Implement")
        return true
    }

    override fun disconnect() {
        runner?.putMsgToSendQueue(FSEventMessage(FMEVENT_TYPE.LOGOUT))
        runner?.stop()
    }
}
