package message

import java.nio.ByteBuffer

object FMEVENT_TYPE {
    const val NONE: Int = 0 // empty messsage
    const val ASK_ALIVE: Int = 1 // ACK
    const val ANSWER_ALIVE: Int = 2  // ACK
    const val LOGIN_REQUEST: Int = 3 // request login
    const val LOGIN_GRANTED: Int = 4 // response for LOGIN_REQUEST
    const val LOGIN_REJECTED: Int = 5
    const val LOGOUT: Int = 6 // logout
    const val BROADCAST_CONNECTED: Int = 7 // broadcast message for notify other user connected
    const val BROADCAST_DISCONNECTED: Int = 8 // broadcast message for notify other user disconnected
    const val UPLOAD_DONE: Int = 9 // Done upload
    const val DOWNLOAD_DONE: Int = 10 // Down download
    const val UPLOAD_REQUEST: Int = 11 // request upload
    const val DOWNLOAD_REQUEST: Int = 12 // request download
}

class FSEventMessage : FSMessage() {

    var mEventcode = FMEVENT_TYPE.NONE

    var userId = FSVariableLenStringField()
    var userPassword = FSVariableLenStringField()


    override fun marshallBody() {
        mBytebuffer!!.putInt(mEventcode)


        when(mEventcode) {
            FMEVENT_TYPE.NONE -> {
                // do nothing
            }

            FMEVENT_TYPE.ASK_ALIVE -> {

            }

            FMEVENT_TYPE.ANSWER_ALIVE -> {

            }

            FMEVENT_TYPE.LOGIN_REQUEST -> {
                userId.marshall(mBytebuffer!!)
                userPassword.marshall(mBytebuffer!!)
            }

            FMEVENT_TYPE.LOGIN_GRANTED -> {

            }

            FMEVENT_TYPE.LOGIN_REJECTED -> {

            }

            FMEVENT_TYPE.LOGOUT -> {

            }

            FMEVENT_TYPE.BROADCAST_CONNECTED, FMEVENT_TYPE.BROADCAST_DISCONNECTED -> {
                userId.marshall(mBytebuffer!!)
            }

            FMEVENT_TYPE.UPLOAD_DONE -> {

            }

            FMEVENT_TYPE.DOWNLOAD_DONE -> {

            }

            FMEVENT_TYPE.UPLOAD_REQUEST -> {

            }

            FMEVENT_TYPE.DOWNLOAD_REQUEST -> {

            }

            else -> {
                // TODO("Raise error or ..")
            }

        }
    }

    override fun unmarshallBody(byteBuffer: ByteBuffer) {
        this.mEventcode = byteBuffer.getInt()

        when(mEventcode) {
            FMEVENT_TYPE.NONE -> {
                // do nothing
            }

            FMEVENT_TYPE.ASK_ALIVE -> {

            }

            FMEVENT_TYPE.ANSWER_ALIVE -> {

            }

            FMEVENT_TYPE.LOGIN_REQUEST -> {
                userId.unmarshall(byteBuffer)
                userPassword.unmarshall(byteBuffer)
            }

            FMEVENT_TYPE.LOGIN_GRANTED -> {

            }

            FMEVENT_TYPE.LOGIN_REJECTED -> {

            }

            FMEVENT_TYPE.LOGOUT -> {

            }

            FMEVENT_TYPE.BROADCAST_CONNECTED, FMEVENT_TYPE.BROADCAST_DISCONNECTED -> {
                userId.unmarshall(byteBuffer)
            }

            FMEVENT_TYPE.UPLOAD_DONE -> {

            }

            FMEVENT_TYPE.DOWNLOAD_DONE -> {

            }

            FMEVENT_TYPE.UPLOAD_REQUEST -> {

            }

            FMEVENT_TYPE.DOWNLOAD_REQUEST -> {

            }

            else -> {
                // TODO("Raise error or ..")
            }

        }
    }

    override fun getByteNums(): Int {
        var ret = super.getByteNums()
        ret += Int.SIZE_BYTES // mEventCode

        when(mEventcode) {
            FMEVENT_TYPE.NONE -> {
                // do nothing
            }

            FMEVENT_TYPE.ASK_ALIVE -> {

            }

            FMEVENT_TYPE.ANSWER_ALIVE -> {

            }

            FMEVENT_TYPE.LOGIN_REQUEST -> {
                ret += userId.getByteNums()
                ret += userPassword.getByteNums()
            }

            FMEVENT_TYPE.LOGIN_GRANTED -> {

            }

            FMEVENT_TYPE.LOGIN_REJECTED -> {

            }

            FMEVENT_TYPE.LOGOUT -> {

            }

            FMEVENT_TYPE.BROADCAST_CONNECTED, FMEVENT_TYPE.BROADCAST_DISCONNECTED -> {
                ret += userId.getByteNums()
            }

            FMEVENT_TYPE.UPLOAD_DONE -> {

            }

            FMEVENT_TYPE.DOWNLOAD_DONE -> {

            }

            FMEVENT_TYPE.UPLOAD_REQUEST -> {

            }

            FMEVENT_TYPE.DOWNLOAD_REQUEST -> {

            }
        }

        return ret
    }
}
