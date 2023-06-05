package message

import java.nio.ByteBuffer

class FSVarLenStringListField() : FSMessageField {
    var count = 0
    var listBytes = 0
    var strs: List<String> = mutableListOf<String>()

    constructor(strs: List<String>) : this() {
        this.strs = strs
        count = strs.size
        listBytes = calculateListBytes()
    }

    constructor(msgs: Array<out String>) : this(msgs.toList())

    constructor(byteBuffer: ByteBuffer) : this() {
        unmarshall(byteBuffer)
    }

    override fun marshall(byteBuffer: ByteBuffer) {
        byteBuffer.putInt(count)
        for (s in strs) {
            val byteArr = s.toByteArray()
            byteBuffer.putInt(byteArr.size)
            byteBuffer.put(byteArr)
        }
    }

    override fun unmarshall(byteBuffer: ByteBuffer) {
        strs = mutableListOf()
        count = byteBuffer.getInt()
        for (i in 0 until count) {
            val bbSize = byteBuffer.getInt()
            var byteArr = ByteArray(bbSize)
            byteBuffer.get(byteArr)
            (strs as MutableList<String>).add(String(byteArr))
        }
        listBytes = calculateListBytes()
    }

    override fun getByteNums(): Int {
        return Int.SIZE_BYTES + calculateListBytes()
    }

    private fun calculateListBytes(): Int {
        var ret = 0
        for (s in strs) {
            ret += s.toByteArray().size
            ret += Int.SIZE_BYTES
        }
        return ret
    }
}
