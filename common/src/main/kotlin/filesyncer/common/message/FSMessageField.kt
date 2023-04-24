package message

import java.nio.ByteBuffer

interface FSMessageField {
    fun marshall(byteBuffer: ByteBuffer)
    fun unmarshall(byteBuffer: ByteBuffer)
    fun getByteNums(): Int
}
