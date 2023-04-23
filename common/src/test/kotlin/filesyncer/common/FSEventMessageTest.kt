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

    fun testLoginGranted() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    fun testLoginRejected() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    fun testLogout() {
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

    fun testBroadcastConnected() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    fun testBroadcastDisconnected() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    fun testUploadDone() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals(msg.extraStrField.str, reconstructedMsg.extraStrField.str)
    }
    fun testListFolderRequest() {
        val list = arrayListOf("aaa", "bbb", "ccc")
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
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

    fun testListFolderResponse() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    fun testRegisterRequest() {
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

    fun testRegisterGranted() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals("", reconstructedMsg.userIdField.str)
        assertEquals("", reconstructedMsg.userPasswordField.str)
        assertEquals("", reconstructedMsg.extraStrField.str)
    }

    fun testRegisterRejected() {
        val msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
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
