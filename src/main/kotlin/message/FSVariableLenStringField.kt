package message

import java.nio.ByteBuffer

class FSVariableLenStringField() : FSMessageField {

  var size: Int = 0
  var str: String = ""

  constructor(str: String) : this() {
    this.str = str
    size = this.str.toByteArray().size
  }

  constructor(byteBuffer: ByteBuffer) : this() {
    unmarshall(byteBuffer)
  }

  override fun marshall(byteBuffer: ByteBuffer) {
    val byteArr = str.toByteArray()
    size = byteArr.size
    byteBuffer.putInt(size)
    byteBuffer.put(byteArr)
  }

  override fun unmarshall(byteBuffer: ByteBuffer) {
    size = byteBuffer.getInt()
    var byteArr = ByteArray(size)
    byteBuffer.get(byteArr)
    str = String(byteArr)
  }

  override fun getByteNums(): Int {
    return Int.SIZE_BYTES + size
  }
}
