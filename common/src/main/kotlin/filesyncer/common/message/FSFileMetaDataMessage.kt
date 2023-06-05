package filesyncer.common.message

import filesyncer.common.file.FSFileMetaData
import java.io.File
import java.nio.ByteBuffer
import message.FSVarLenStringListField

class FSFileMetaDataMessage(
    var name: String,
    var fileSize: Long,
    var timeStamp: Long = 0L,
    var md5: String = "",
    var path: File? = null,
    var owner: String = "",
    var shared: List<String> = emptyList()
) : FSMessage() {

    var strListField = FSVarLenStringListField(name, md5, owner, *shared.toTypedArray())

    override fun marshallBody() {
        mBytebuffer!!.putLong(fileSize)
        mBytebuffer!!.putLong(timeStamp)

        strListField.marshall(mBytebuffer!!)
    }

    override fun unmarshallBody(byteBuffer: ByteBuffer) {
        fileSize = byteBuffer.getLong()
        timeStamp = byteBuffer.getLong()

        strListField.unmarshall(byteBuffer)
        name = strListField.strs[0]
        md5 = strListField.strs[1]
        owner = strListField.strs[2]
        shared = strListField.strs.subList(3, strListField.strs.size)
    }

    override fun getByteNums(): Int {
        var ret = super.getByteNums()
        ret += Long.SIZE_BYTES * 2 // fileSize, timeStamp
        ret += strListField.getByteNums()
        return ret
    }

    fun toFileMetaData(): FSFileMetaData {
        return FSFileMetaData(name, fileSize, timeStamp, md5, path, owner, shared)
    }
}
