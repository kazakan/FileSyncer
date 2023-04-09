import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import message.FSEventMessage

class FSEventConnection(var socket: Socket) {

  var ous: OutputStream = socket.getOutputStream()
  var dous: DataOutputStream = DataOutputStream(ous)
  var ios: InputStream = socket.getInputStream()
  var dios = DataInputStream(ios)

  fun sendMessage(message: FSEventMessage) {
    val buffer = message.marshall()
    dous.write(buffer!!.array())
  }

  fun getMessage(): FSEventMessage {
    val msg = FSEventMessage()

    val nBytes = dios.readInt()

    val byteBuffer = ByteBuffer.allocate(nBytes)
    byteBuffer.putInt(nBytes)
    while (byteBuffer.hasRemaining()) {
      byteBuffer.put(dios.readByte())
    }

    msg.unmarshall(byteBuffer)
    return msg
  }

  fun close() {
    dous.flush()
    dous.close()
    socket.close()
  }
}
