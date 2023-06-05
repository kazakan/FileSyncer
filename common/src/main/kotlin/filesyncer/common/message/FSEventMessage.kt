package message

import filesyncer.common.message.FSMessage
import java.nio.ByteBuffer

class FSEventMessage(
    eventCode: Int = EventType.NONE,
    userIdStr: String = "",
    userPasswordStr: String = "",
    extraStr: String = ""
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
        const val UPLOAD_REQUEST: Int = 11 // request upload
        const val UPLOAD_RESPONSE: Int = 18 // response for UPLOAD_REQUEST
        const val DOWNLOAD_REQUEST: Int = 12 // request download
        const val LISTFOLDER_REQUEST: Int = 13 // request list of files
        const val LISTFOLDER_RESPONSE: Int = 14 // response for FILELIST_REQUEST
        const val REGISTER_REQUEST: Int = 15 // request user register
        const val REGISTER_GRANTED: Int = 16 // response for REGISTER_REQUEST
        const val REGISTER_REJECTED: Int = 17

        const val FILE_CREATE: Int = 15 // Tell file created
        const val FILE_DELETE: Int = 16 // Tell file deleted
        const val FILE_MODIFY: Int = 17 // Tell file modified
    }

    var mEventcode = eventCode

    var userIdField = FSVariableLenStringField(userIdStr)
    var userPasswordField = FSVariableLenStringField(userPasswordStr)
    var extraStrField = FSVariableLenStringField(extraStr)
    var fileListField = FSVarLenStringListField()

    override fun marshallBody() {
        mBytebuffer!!.putInt(mEventcode)

        when (mEventcode) {
            EventType.NONE -> {
                // do nothing
            }
            EventType.ASK_ALIVE -> {}
            EventType.ANSWER_ALIVE -> {}
            EventType.LOGIN_REQUEST -> {
                userIdField.marshall(mBytebuffer!!)
                userPasswordField.marshall(mBytebuffer!!)
            }
            EventType.LOGIN_GRANTED -> {}
            EventType.LOGIN_REJECTED -> {}
            EventType.LOGOUT -> {
                userIdField.marshall(mBytebuffer!!)
                userPasswordField.marshall(mBytebuffer!!)
            }
            EventType.BROADCAST_CONNECTED,
            EventType.BROADCAST_DISCONNECTED -> {
                userIdField.marshall(mBytebuffer!!)
            }
            EventType.UPLOAD_DONE -> {
                extraStrField.marshall(mBytebuffer!!)
            }
            EventType.DOWNLOAD_DONE -> {}
            EventType.UPLOAD_REQUEST -> {
                extraStrField.marshall(mBytebuffer!!) // file name!
            }
            EventType.DOWNLOAD_REQUEST -> {}
            EventType.REGISTER_REQUEST -> {
                userIdField.marshall(mBytebuffer!!)
                userPasswordField.marshall(mBytebuffer!!)
            }
            EventType.LISTFOLDER_RESPONSE -> {
                fileListField.marshall(mBytebuffer!!)
            }
            else -> {
                // TODO("Raise error or ..")
            }
        }
    }

    override fun unmarshallBody(byteBuffer: ByteBuffer) {
        this.mEventcode = byteBuffer.getInt()

        when (mEventcode) {
            EventType.NONE -> {
                // do nothing
            }
            EventType.ASK_ALIVE -> {}
            EventType.ANSWER_ALIVE -> {}
            EventType.LOGIN_REQUEST -> {
                userIdField.unmarshall(byteBuffer)
                userPasswordField.unmarshall(byteBuffer)
            }
            EventType.LOGIN_GRANTED -> {}
            EventType.LOGIN_REJECTED -> {}
            EventType.LOGOUT -> {
                userIdField.unmarshall(byteBuffer)
                userPasswordField.unmarshall(byteBuffer)
            }
            EventType.BROADCAST_CONNECTED,
            EventType.BROADCAST_DISCONNECTED -> {
                userIdField.unmarshall(byteBuffer)
            }
            EventType.UPLOAD_DONE -> {
                extraStrField.unmarshall(byteBuffer)
            }
            EventType.DOWNLOAD_DONE -> {}
            EventType.UPLOAD_REQUEST -> {
                extraStrField.unmarshall(byteBuffer)
            }
            EventType.DOWNLOAD_REQUEST -> {}
            EventType.REGISTER_REQUEST -> {
                userIdField.unmarshall(byteBuffer)
                userPasswordField.unmarshall(byteBuffer)
            }
            EventType.LISTFOLDER_RESPONSE -> {
                fileListField.unmarshall(byteBuffer)
            }
            else -> {
                // TODO("Raise error or ..")
            }
        }
    }

    override fun getByteNums(): Int {
        var ret = super.getByteNums()
        ret += Int.SIZE_BYTES // mEventCode

        when (mEventcode) {
            EventType.NONE -> {
                // do nothing
            }
            EventType.ASK_ALIVE -> {}
            EventType.ANSWER_ALIVE -> {}
            EventType.LOGIN_REQUEST -> {
                ret += userIdField.getByteNums()
                ret += userPasswordField.getByteNums()
            }
            EventType.LOGIN_GRANTED -> {}
            EventType.LOGIN_REJECTED -> {}
            EventType.LOGOUT -> {
                ret += userIdField.getByteNums()
                ret += userPasswordField.getByteNums()
            }
            EventType.BROADCAST_CONNECTED,
            EventType.BROADCAST_DISCONNECTED -> {
                ret += userIdField.getByteNums()
            }
            EventType.UPLOAD_DONE -> {
                ret += extraStrField.getByteNums()
            }
            EventType.DOWNLOAD_DONE -> {}
            EventType.UPLOAD_REQUEST -> {
                ret += extraStrField.getByteNums()
            }
            EventType.DOWNLOAD_REQUEST -> {}
            EventType.REGISTER_REQUEST -> {
                ret += userIdField.getByteNums()
                ret += userPasswordField.getByteNums()
            }
            EventType.LISTFOLDER_RESPONSE -> {
                ret += fileListField.getByteNums()
            }
        }

        return ret
    }
}
