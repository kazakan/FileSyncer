package message

import filesyncer.common.message.FSMessage
import java.nio.ByteBuffer

class FSEventMessage(
    eventCode: Int = EventType.NONE,
    timeStamp: Long = 0L,
    vararg messages: String
) : FSMessage() {

    object EventType {
        const val NONE: Int = 0 // empty messsage
        const val ASK_ALIVE: Int = 1 // ACK
        const val ANSWER_ALIVE: Int = 2 // ACK
        const val LOGIN_REQUEST: Int = 3 // request login
        const val LOGIN_GRANTED: Int = 4 // response for LOGIN_REQUEST
        const val LOGIN_REJECTED: Int = 5
        const val LOGOUT: Int = 6 // logout
        const val BROADCAST_CONNECTED: Int = 7 // broadcast message for notify other user connected
        const val BROADCAST_DISCONNECTED: Int =
            8 // broadcast message for notify other user disconnected
        const val UPLOAD_DONE: Int = 9 // Done upload
        const val DOWNLOAD_DONE: Int = 10 // Down download
        const val UPLOAD_REQUEST: Int = 11 // request upload (filename, md5)
        const val UPLOAD_RESPONSE: Int = 18 // response for UPLOAD_REQUEST
        const val DOWNLOAD_REQUEST: Int = 12 // request download
        const val LISTFOLDER_REQUEST: Int = 13 // request list of files
        const val LISTFOLDER_RESPONSE: Int = 14 // response for FILELIST_REQUEST
        const val REGISTER_REQUEST: Int = 15 // request user register
        const val REGISTER_GRANTED: Int = 16 // response for REGISTER_REQUEST
        const val REGISTER_REJECTED: Int = 17

        const val FILE_CREATE: Int = 18 // Tell file created, (filename, md5)
        const val FILE_DELETE: Int = 19 // Tell file deleted, (filename)
        const val FILE_MODIFY: Int = 20 // Tell file modified (filename, md5)
        const val SYNC: Int = 21 // Sync logical clock

        const val LIST_USER_REQUEST: Int = 22 // Request User List
        const val LIST_USER_RESPONSE: Int = 23 // Response for LIST_USER_REQUEST

        const val SHARE_FILE: Int = 24 // share file request.
    }

    var mEventcode = eventCode
    var mTimeStamp = timeStamp

    var messageField = FSVarLenStringListField(*messages)

    override fun marshallBody() {
        mBytebuffer!!.putInt(mEventcode)
        mBytebuffer!!.putLong(mTimeStamp)

        messageField.marshall(mBytebuffer!!)
    }

    override fun unmarshallBody(byteBuffer: ByteBuffer) {
        this.mEventcode = byteBuffer.getInt()
        this.mTimeStamp = byteBuffer.getLong()

        messageField.unmarshall(byteBuffer)
    }

    override fun getByteNums(): Int {
        var ret = super.getByteNums()
        ret += Int.SIZE_BYTES // mEventCode
        ret += Long.SIZE_BYTES // mTimeStamp

        ret += messageField.getByteNums()

        return ret
    }
}
