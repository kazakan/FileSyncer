package message

import java.nio.ByteBuffer

/**
 * Base class of message classes.
 *
 * @property mBytebuffer ByteBuffer that keeps serialized data
 * @property mBytesNums total numbers of bytes of serialized object
 */
abstract class FSMessage {
    var mBytebuffer: ByteBuffer? = null
    var mBytesNums: Int = 0

    /**
     * Serialize FSMessage to byte buffer
     *
     * @return ByteBuffer?
     */
    fun marshall(): ByteBuffer? {
        synchronized(this) {
            mBytesNums = getByteNums()
            mBytebuffer = ByteBuffer.allocate(mBytesNums)

            mBytebuffer!!.putInt(mBytesNums)
            marshallBody()

            return mBytebuffer
        }
    }

    /**
     * Set FSMessage from byteBuffer
     *
     * @param byteBuffer ByteBuffer contains serialize data
     */
    fun unmarshall(byteBuffer: ByteBuffer) {
        byteBuffer.clear()
        this.mBytesNums = byteBuffer.getInt()
        unmarshallBody(byteBuffer)
    }

    /** Serialize body of message. Implement should put data into mByteBuffer member. */
    abstract fun marshallBody()

    /**
     * Set body of message from byteBuffer.
     *
     * @param byteBuffer ByteBuffer contains serialize data
     */
    abstract fun unmarshallBody(byteBuffer: ByteBuffer)

    /**
     * Returns total bytes of message
     *
     * @return total bytes of serialized message object
     */
    protected open fun getByteNums(): Int {
        return Int.SIZE_BYTES
    }
}
