package filesyncer.common

import message.FSEventMessage
import message.FSEventMessage.EventType
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
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)

        // With MESSAGE
        msg = FSEventMessage(EventType.LOGIN_REQUEST, 1, "ididid", "passwspassws")
        reconstructedMsg = FSEventMessage()
        byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.mBytesNums, reconstructedMsg.mBytesNums)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testNONE() {
        val msg = FSEventMessage(EventType.NONE, 1, "ididid", "passwspassws", "extrastr")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLoginRequest() {
        val msg = FSEventMessage(EventType.LOGIN_REQUEST, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLoginGranted() {
        val msg = FSEventMessage(EventType.LOGIN_GRANTED, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLoginRejected() {
        val msg = FSEventMessage(EventType.LOGIN_REJECTED, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLogout() {
        val msg = FSEventMessage(EventType.LOGOUT, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testBroadcastConnected() {
        val msg = FSEventMessage(EventType.BROADCAST_CONNECTED, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testBroadcastDisconnected() {
        val msg = FSEventMessage(EventType.BROADCAST_DISCONNECTED, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testUploadDone() {
        val msg = FSEventMessage(EventType.UPLOAD_DONE, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }
    @Test
    fun testListFolderRequest() {
        val list = arrayListOf("aaa", "bbb", "ccc")
        val msg = FSEventMessage(EventType.LISTFOLDER_REQUEST, 1, "ididid", "passwspassws")
        msg.messageField = FSVarLenStringListField(list)
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testListFolderResponse() {
        val list = arrayListOf("aaa", "bbb", "ccc")
        val msg = FSEventMessage(EventType.LISTFOLDER_RESPONSE, 1, "ididid", "passwspassws")
        msg.messageField = FSVarLenStringListField(list)
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testRegisterRequest() {
        val msg = FSEventMessage(EventType.REGISTER_REQUEST, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testRegisterGranted() {
        val msg = FSEventMessage(EventType.REGISTER_GRANTED, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testRegisterRejected() {
        val msg = FSEventMessage(EventType.REGISTER_GRANTED, 1, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }
}
