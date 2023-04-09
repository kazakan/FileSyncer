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

class Client : FSEventMessageHandler {
  var state = FSCLIENT_STATE.DISCONNECTED
  var id = "dddd"
  var passwd = "passwd"
  var runner: FSEventConnWorker? = null
  var webfront = FSClientWebFront()

  fun start() {
    webfront.start()
  }

  fun startConnection(address: String, port: Int) {
    val socket = Socket(address, port)
    runner = FSEventConnWorker(socket, this)
    runner!!.run()
  }

  fun requestLogin() {
    println("client : requested login")
    runner!!.putMsgToSendQueue(FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, id, passwd))
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
        state = FSCLIENT_STATE.LOGGEDIN
      }
      FMEVENT_TYPE.LOGIN_REJECTED -> {}
      FMEVENT_TYPE.LOGOUT -> {}
      FMEVENT_TYPE.BROADCAST_CONNECTED -> {}
      FMEVENT_TYPE.BROADCAST_DISCONNECTED -> {}
      FMEVENT_TYPE.UPLOAD_DONE -> {}
      FMEVENT_TYPE.DOWNLOAD_DONE -> {}
      FMEVENT_TYPE.UPLOAD_REQUEST -> {}
      FMEVENT_TYPE.DOWNLOAD_REQUEST -> {}
      else -> {
        // TODO("Raise error or ..")
      }
    }
  }
}
