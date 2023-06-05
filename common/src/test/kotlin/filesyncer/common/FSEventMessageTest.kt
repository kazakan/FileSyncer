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
        msg = FSEventMessage(EventType.LOGIN_REQUEST, "ididid", "passwspassws")
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
        val msg = FSEventMessage(EventType.NONE, "ididid", "passwspassws", "extrastr")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLoginRequest() {
        val msg = FSEventMessage(EventType.LOGIN_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLoginGranted() {
        val msg = FSEventMessage(EventType.LOGIN_GRANTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLoginRejected() {
        val msg = FSEventMessage(EventType.LOGIN_REJECTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testLogout() {
        val msg = FSEventMessage(EventType.LOGOUT, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testBroadcastConnected() {
        val msg = FSEventMessage(EventType.BROADCAST_CONNECTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testBroadcastDisconnected() {
        val msg = FSEventMessage(EventType.BROADCAST_DISCONNECTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testUploadDone() {
        val msg = FSEventMessage(EventType.UPLOAD_DONE, "ididid", "passwspassws")
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
        val msg = FSEventMessage(EventType.LISTFOLDER_REQUEST, "ididid", "passwspassws")
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
        val msg = FSEventMessage(EventType.LISTFOLDER_RESPONSE, "ididid", "passwspassws")
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
        val msg = FSEventMessage(EventType.REGISTER_REQUEST, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testRegisterGranted() {
        val msg = FSEventMessage(EventType.REGISTER_GRANTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }

    @Test
    fun testRegisterRejected() {
        val msg = FSEventMessage(EventType.REGISTER_GRANTED, "ididid", "passwspassws")
        val reconstructedMsg = FSEventMessage()
        val byteBuffer = msg.marshall()!!
        reconstructedMsg.unmarshall(byteBuffer)

        assertEquals(0, byteBuffer.remaining())

        assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
        assertEquals(msg.messageField.strs, reconstructedMsg.messageField.strs)
    }
}
