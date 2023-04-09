package message

import java.nio.ByteBuffer

abstract class FSMessage {
  var mBytebuffer: ByteBuffer? = null
  var mBytesNums: Int = 0

  fun marshall(): ByteBuffer? {
    mBytesNums = getByteNums()
    mBytebuffer = ByteBuffer.allocate(mBytesNums)

    mBytebuffer!!.putInt(mBytesNums)
    marshallBody()

    return mBytebuffer
  }

  fun unmarshall(byteBuffer: ByteBuffer) {
    byteBuffer.clear()
    this.mBytesNums = byteBuffer.getInt()
    unmarshallBody(byteBuffer)
  }

  abstract fun marshallBody()
  abstract fun unmarshallBody(byteBuffer: ByteBuffer)
  protected open fun getByteNums(): Int {
    return Int.SIZE_BYTES
  }
}
