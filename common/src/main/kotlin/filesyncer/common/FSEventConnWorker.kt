package filesyncer.common

import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import message.FSEventMessage

open class FSEventConnWorker(
    var socket: Socket,
    var eventHandler: FSEventMessageHandler? = null,
    val verbose: Boolean = false,
    var listener: FSEventConnWorker.StateChangeListener? = null
) {
    interface StateChangeListener {
        fun onStart()
        fun onClose()
    }
    val connection = FSEventConnection(socket, verbose)
    private val eventSendQueue = LinkedBlockingQueue<FSEventMessage>()
    private val eventReceiveQueue = LinkedBlockingQueue<FSEventMessage>()
    private var closeReserved = false
    private var onCloseCalled = false

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
        try {
            while (connection.isConnected() && !closeReserved) {
                val msg: FSEventMessage = connection.getMessage()
                eventReceiveQueue.put(msg)
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Stopping FSEventConnworker due to socket is dead")
                // e.printStackTrace()
            }
        } finally {
            closeReserved = true
            if (!onCloseCalled) {
                onCloseCalled = true
                listener?.onClose()
            }
            sendMsgThread.interrupt()
            handleMsgThread.interrupt()

            try {
                connection.close()
            } catch (_: Exception) {}
        }
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

            if (!onCloseCalled) {
                onCloseCalled = true
                listener?.onClose()
            }

            try {
                connection.close()
            } catch (_: Exception) {}
        }

    fun run() {
        listener?.onStart()
        sendMsgThread.uncaughtExceptionHandler = threadUncaughtExceptionHandler
        receiveMsgThread.uncaughtExceptionHandler = threadUncaughtExceptionHandler
        handleMsgThread.uncaughtExceptionHandler = threadUncaughtExceptionHandler

        sendMsgThread.start()
        receiveMsgThread.start()
        handleMsgThread.start()
    }

    fun stop() {
        closeReserved = true
        sendMsgThread.interrupt()
        receiveMsgThread.interrupt()
        handleMsgThread.interrupt()
        if (!onCloseCalled) {
            onCloseCalled = true
            listener?.onClose()
        }
        if (connection.isConnected()) connection.close()
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
        return connection.isClosed()
    }

    fun isNotConnectedYet(): Boolean {
        return connection.isNotConnectedYet()
    }
}
