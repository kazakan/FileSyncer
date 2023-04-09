import message.FMEVENT_TYPE
import message.FSEventMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FSEventMessageTest {
  @Test
  fun testMarshallUnmarshall() {
    // NONE MESSAGE
    var msg = FSEventMessage()
    var reconstructedMsg = FSEventMessage()
    reconstructedMsg.unmarshall(msg.marshall()!!)

    assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
    assertEquals(msg.mBytesNums, reconstructedMsg.mBytesNums)
    assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
    assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
    assertEquals(msg.extraStrField.str, reconstructedMsg.extraStrField.str)

    // With MESSAGE
    msg = FSEventMessage(FMEVENT_TYPE.LOGIN_REQUEST, "ididid", "passwspassws")
    reconstructedMsg = FSEventMessage()
    reconstructedMsg.unmarshall(msg.marshall()!!)

    assertEquals(msg.mEventcode, reconstructedMsg.mEventcode)
    assertEquals(msg.mBytesNums, reconstructedMsg.mBytesNums)
    assertEquals(msg.userIdField.str, reconstructedMsg.userIdField.str)
    assertEquals(msg.userPasswordField.str, reconstructedMsg.userPasswordField.str)
    assertEquals(msg.extraStrField.str, reconstructedMsg.extraStrField.str)
  }
}
