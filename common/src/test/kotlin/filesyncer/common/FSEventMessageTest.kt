package filesyncer.common

import message.FMEVENT_TYPE
import message.FSEventMessage
import message.FSVarLenStringListField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FSEventMessageTest {
    @Test
    fun testMarshallUnmarshall() {
        // NONE MESSAGE
        var msg = FSEventMessage()
        var reconstructedMsg = FSEventMessage()
        var byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.mBytesNums, reconstructedMsg.mBytesNums)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
        assertEquals(msg.extraStrField.str, reconstructedMsg.extraStrField.str)

        // With MESSAGE
        msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        reconstructedMsg = FSEventMessage()
        byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.mBytesNums, reconstructedMsg.mBytesNums)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
        assertEquals(msg.extraStrField.str, reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testNONE() {
        val msg = FSEventMessage(FMEVENT_TYPE.NONE, "ididid", "passwspassws", "extrastr")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)

        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testLoginRequest() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testLoginGranted() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_GRANTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testLoginRejected() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REJECTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testLogout() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGOUT, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testBroadcastConnected() {
        val msg = FSEventMessage(FMEVENT_TYPE.BROADCAST_CONNECTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testBroadcastDisconnected() {
        val msg = FSEventMessage(FMEVENT_TYPE.BROADCAST_DISCONNECTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testUploadDone() {
        val msg = FSEventMessage(FMEVENT_TYPE.UPLOAD_DONE, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals(msg.extraStrField.str, reconstructedMsg.extraStrField.str)
    }
    @Test
    fun testListFolderRequest() {
        val list = arrayListOf("aaa", "bbb", "ccc")
        val msg = FSEventMessage(FMEVENT_TYPE.LISTFOLDER_REQUEST, "ididid", "passwspassws")
        msg.fileListField = FSVarLenStringListField(list)
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
        assertEquals(emptyList<String>(), reconstructedMsg.fileListField.strs)
    }

    @Test
    fun testListFolderResponse() {
        val list = arrayListOf("aaa", "bbb", "ccc")
        val msg = FSEventMessage(FMEVENT_TYPE.LISTFOLDER_RESPONSE, "ididid", "passwspassws")
        msg.fileListField = FSVarLenStringListField(list)
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
        assertEquals(list, reconstructedMsg.fileListField.strs)
    }

    @Test
    fun testRegisterRequest() {
        val msg = FSEventMessage(FMEVENT_TYPE.REGISTER_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testRegisterGranted() {
        val msg = FSEventMessage(FMEVENT_TYPE.REGISTER_GRANTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    @Test
    fun testRegisterRejected() {
        val msg = FSEventMessage(FMEVENT_TYPE.REGISTER_GRANTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }
}
