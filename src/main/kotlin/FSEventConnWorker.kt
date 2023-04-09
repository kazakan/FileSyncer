import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import message.FSEventMessage

open class FSEventConnWorker(var socket: Socket, var eventHandler: FSEventMessageHandler? = null) {
  val connection = FSEventConnection(socket)
  val eventSendQueue = LinkedBlockingQueue<FSEventMessage>()
  val eventReceiveQueue = LinkedBlockingQueue<FSEventMessage>()
  val sendMsgThread = Thread {
    while (socket.isConnected) {
      val msg = eventSendQueue.take()
      connection.sendMessage(msg)
      println("Sent message ${msg.mEventcode}, ${msg.userIdField.str}")
    }
  }
  val receiveMsgThread = Thread {
    while (socket.isConnected) {
      val msg = connection.getMessage()
      eventReceiveQueue.put(msg)
    }
  }
  val handleMsgThread = Thread {
    while (socket.isConnected || !eventReceiveQueue.isEmpty()) {
      val msg = eventReceiveQueue.take()
      println("Handling message ${msg.mEventcode}, ${msg.userIdField.str}")

      if (eventHandler != null) eventHandler!!.handleMessage(msg)
    }
  }

  fun run() {
    sendMsgThread.start()
    receiveMsgThread.start()
    handleMsgThread.start()
  }

  fun stop() {
    if (socket.isConnected) connection.close()
  }

  fun putMsgToSendQueue(msg: FSEventMessage) {
    eventSendQueue.put(msg)
  }

  fun putMsgToReceiveQueue(msg: FSEventMessage) {
    eventReceiveQueue.put(msg)
  }
}
