package filesyncer.common.message

import filesyncer.common.file.FSFileMetaData
import java.io.File
import java.nio.ByteBuffer
import message.FSVarLenStringListField

class FSFileMetaDataMessage(
    var name: String = "",
    var fileSize: Long = 0L,
    var timeStamp: Long = 0L,
    var md5: String = "",
    var path: File? = null,
    var owner: String = "",
    var shared: List<String> = emptyList(),
    var requester: String = "",
) : FSMessage() {

    constructor(
        metaData: FSFileMetaData,
        requester: String
    ) : this(
        metaData.name,
        metaData.fileSize,
        metaData.timeStamp,
        metaData.md5,
        metaData.path,
        metaData.owner,
        metaData.shared,
        requester
    )

    var strListField = FSVarLenStringListField(name, md5, owner, requester, *shared.toTypedArray())

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
        requester = strListField.strs[3]
        shared = strListField.strs.subList(4, strListField.strs.size)
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
