package filesyncer.common

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.jvm.Throws
import message.FSEventMessage

class FSEventConnection(var socket: Socket, val verbose: Boolean = false) {

    private var dous = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))
    private var dios = DataInputStream(BufferedInputStream(socket.getInputStream()))
    private var _dead = false

    fun sendMessage(message: FSEventMessage) {
        val buffer = message.marshall()
        dous.write(buffer!!.array())
        dous.flush()
    }

    @Throws(IOException::class)
    fun getMessage(): FSEventMessage {
        val msg = FSEventMessage()

        val nBytes: Int = dios.readInt()
        val byteBuffer = ByteBuffer.allocate(nBytes)
        byteBuffer.putInt(nBytes)
        while (byteBuffer.hasRemaining()) {
            byteBuffer.put(dios.readByte())
        }

        msg.unmarshall(byteBuffer)

        return msg
    }

    fun close() {
        if (_dead) return
        _dead = true
        if (verbose) println("Closing FSEventConnection with ${socket.inetAddress}")
        dous.flush()
        dous.close()
        dios.close()
    }

    fun isConnected(): Boolean {
        return socket.isConnected && !socket.isClosed || !_dead
    }

    fun isClosed(): Boolean {
        return socket.isClosed || _dead
    }

    fun isNotConnectedYet(): Boolean {
        return !socket.isConnected && !isClosed()
    }
}
