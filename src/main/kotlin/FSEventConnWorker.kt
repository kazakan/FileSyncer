import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import message.FSEventMessage

open class FSEventConnWorker(var socket: Socket, var eventHandler: FSEventMessageHandler? = null) {
    val connection = FSEventConnection(socket)
    val eventSendQueue = LinkedBlockingQueue<FSEventMessage>()
    val eventReceiveQueue = LinkedBlockingQueue<FSEventMessage>()
    var closeReserved = false
    private var _closed = false
    val sendMsgThread = Thread {
        while (socket.isConnected && !socket.isClosed && !closeReserved) {
            val msg = eventSendQueue.take()
            if (!socket.isClosed) {
                connection.sendMessage(msg)
                println("Sent message ${msg.mEventcode}, ${msg.userIdField.str}")
            }
        }
    }
    val receiveMsgThread = Thread {
        while (socket.isConnected && !socket.isClosed && !closeReserved) {
            val msg = connection.getMessage()
            if (msg != null) eventReceiveQueue.put(msg) else break
        }

        closeReserved = true
        sendMsgThread.interrupt()
        handleMsgThread.interrupt()
        sendMsgThread.join()
        handleMsgThread.join()
        if (socket.isConnected) connection.close()
        _closed = true
        println("Connworker stopped")
    }
    val handleMsgThread = Thread {
        while (
            (socket.isConnected && !socket.isClosed && !closeReserved) ||
                !eventReceiveQueue.isEmpty()
        ) {
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
        closeReserved = true
        join()
        if (socket.isConnected && !socket.isClosed) connection.close()
        _closed = true
        println("Connworker stopped")
    }

    fun join() {
        sendMsgThread.join()
        receiveMsgThread.join()
        handleMsgThread.join()
    }

    fun putMsgToSendQueue(msg: FSEventMessage) {
        eventSendQueue.put(msg)
    }

    fun putMsgToReceiveQueue(msg: FSEventMessage) {
        eventReceiveQueue.put(msg)
    }

    fun isClosed(): Boolean {
        return _closed
    }
}
