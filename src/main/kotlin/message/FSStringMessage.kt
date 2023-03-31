package message

import java.nio.ByteBuffer

class FSStringMessage(message: String = "") : FSMessage() {

    var strField = FSVariableLenStringField(message)

    override fun marshallBody() {
        strField.marshall(mBytebuffer!!)
    }

    override fun unmarshallBody(byteBuffer: ByteBuffer) {
        strField.unmarshall(byteBuffer)
    }

    override fun getByteNums(): Int {

        return super.getByteNums() + strField.getByteNums()
    }
}
