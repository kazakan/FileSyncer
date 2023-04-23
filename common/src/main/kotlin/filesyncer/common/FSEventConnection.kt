package filesyncer.common

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import message.FSEventMessage

class FSEventConnection(var socket: Socket, val verbose: Boolean = false) {

    var ous: OutputStream = socket.getOutputStream()
    var dous: DataOutputStream = DataOutputStream(ous)
    var ios: InputStream = socket.getInputStream()
    var dios = DataInputStream(ios)
    var _dead = false

    fun sendMessage(message: FSEventMessage) {
        val buffer = message.marshall()
        dous.write(buffer!!.array())
    }

    fun getMessage(): FSEventMessage? {
        val msg = FSEventMessage()
        var nBytes: Int

        try {
            nBytes = dios.readInt()
        } catch (e: Exception) {
            if (verbose) {
                e.printStackTrace()
                println("Socket is dead.")
            }
            _dead = true
            close()
            return null
        }

        val byteBuffer = ByteBuffer.allocate(nBytes)
        byteBuffer.putInt(nBytes)
        while (byteBuffer.hasRemaining()) {
            byteBuffer.put(dios.readByte())
        }

        msg.unmarshall(byteBuffer)
        return msg
    }

    fun close() {
        if (verbose) println("Closing FSEventConnection with ${socket.inetAddress}")
        dous.flush()
        dous.close()
        dios.close()
        socket.close()
        _dead = true
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
