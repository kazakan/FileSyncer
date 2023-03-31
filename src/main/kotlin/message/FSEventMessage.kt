package message

import java.nio.ByteBuffer

object FMEVENT_TYPE {
    const val NONE: Int = 0 // empty messsage
    const val ASK_ALIVE: Int = 1 // ACK
    const val ANSWER_ALIVE: Int = 2  // ACK
    const val LOGIN_REQUEST: Int = 3 // request login
    const val LOGIN_RESPONSE: Int = 4 // response for LOGIN_REQUEST
    const val LOGOUT: Int = 5 // logout
    const val BROADCAST_CONNECTED: Int = 6 // broadcast message for notify other user connected
    const val BROADCAST_DISCONNECTED: Int = 7 // broadcast message for notify other user disconnected
    const val UPLOAD_DONE: Int = 8 // Done upload
    const val DOWNLOAD_DONE: Int = 9 // Down download
    const val UPLOAD_REQUEST: Int = 10 // request upload
}

class FSEventMessage : FSMessage() {

    var mEventcode = FMEVENT_TYPE.NONE

    var userId  = FSVariableLenStringField()
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

            FMEVENT_TYPE.LOGIN_RESPONSE -> {

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

            FMEVENT_TYPE.UPLOAD_REQUEST ->{

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

            FMEVENT_TYPE.LOGIN_RESPONSE -> {

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

            FMEVENT_TYPE.UPLOAD_REQUEST ->{

            }

            else -> {
                // TODO("Raise error or ..")
            }

        }
    }

    override fun getByteNums(): Int {
        var ret = super.getByteNums()
        ret += Int.SIZE_BYTES // mEventCode
        // ret += 40
        return ret
    }
}
