package filesyncer.common

import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import message.FSEventMessage

open class FSEventConnWorker(
    var socket: Socket,
    var eventHandler: FSEventMessageHandler? = null,
    val verbose: Boolean = false
) {
    val connection = FSEventConnection(socket)
    private val eventSendQueue = LinkedBlockingQueue<FSEventMessage>()
    private val eventReceiveQueue = LinkedBlockingQueue<FSEventMessage>()
    var closeReserved = false
    private var _closed = false

    val sendMsgThread = Thread {
        while (connection.isConnected() && !closeReserved) {
            val msg = eventSendQueue.take()
            if (!socket.isClosed) {
                connection.sendMessage(msg)
                if (verbose) println("Sent message ${msg.mEventcode}")
            }
        }
    }
    val receiveMsgThread = Thread {
        while (connection.isConnected() && !closeReserved) {
            var msg: FSEventMessage? = connection.getMessage()
            if (msg != null) eventReceiveQueue.put(msg) else break
        }

        // connection lost
        closeReserved = true
        sendMsgThread.interrupt()
        handleMsgThread.interrupt()
        if (connection.isConnected()) connection.close()
        _closed = true
    }
    val handleMsgThread = Thread {
        while ((connection.isConnected() && !closeReserved) || !eventReceiveQueue.isEmpty()) {
            val msg = eventReceiveQueue.take()
            if (verbose) println("Handling message ${msg.mEventcode}")

            if (eventHandler != null) eventHandler!!.handleMessage(msg)
        }
    }

    val threadUncaughtExceptionHandler =
        Thread.UncaughtExceptionHandler { t, e ->
            closeReserved = true
            sendMsgThread.interrupt()
            receiveMsgThread.interrupt()
            handleMsgThread.interrupt()

            if (socket.isConnected && !socket.isClosed) connection.close()
            _closed = true
        }

    fun run() {
        sendMsgThread.uncaughtExceptionHandler = threadUncaughtExceptionHandler
        receiveMsgThread.uncaughtExceptionHandler = threadUncaughtExceptionHandler
        handleMsgThread.uncaughtExceptionHandler = threadUncaughtExceptionHandler

        sendMsgThread.start()
        receiveMsgThread.start()
        handleMsgThread.start()
    }

    fun stop() {
        closeReserved = true
        // join()
        if (connection.isConnected()) connection.close()
        _closed = true
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
