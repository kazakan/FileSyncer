package filesyncer.common

import java.net.ServerSocket
import java.net.Socket
import message.FMEVENT_TYPE
import message.FSEventMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FSEventConnectionTest {
    @Test
    fun testSingleMessageSendAndReceive() {
        var runClient = Runnable {
            var client = Socket("localhost", 5000)
            var clientConn = FSEventConnection(client)
            var messageToSend = FSEventMessage()
            messageToSend.mEventcode = 77
            clientConn.sendMessage(messageToSend)
            client.close()
        }

        var server = ServerSocket(5000)
        Thread(runClient).start()
        var ss = server.accept()
        var serverConn = FSEventConnection(ss)
        var receivedMessage = serverConn.getMessage()

        assertEquals(77, receivedMessage?.mEventcode)

        ss.close()
        server.close()
    }

    @Test
    fun testTwoMessageSendAndReceive() {
        val CODE1 = 111
        val CODE2 = 222
        val PORT = 5000

        var runClient = Runnable {
            var client = Socket("localhost", PORT)
            var clientConn = FSEventConnection(client)

            var msg = FSEventMessage()
            msg.mEventcode = CODE1
            clientConn.sendMessage(msg)
            msg.mEventcode = CODE2
            clientConn.sendMessage(msg)

            client.close()
        }

        var server = ServerSocket(PORT)
        Thread(runClient).start()
        var ss = server.accept()

        var serverConn = FSEventConnection(ss)
        var msg1 = serverConn.getMessage()
        var msg2 = serverConn.getMessage()

        assertEquals(CODE1, msg1?.mEventcode)
        assertEquals(CODE2, msg2?.mEventcode)

        ss.close()
        server.close()
    }

    @Test
    fun testVarLenMessageSendAndReceive() {
        val PORT = 5000
        val msg1 = FSEventMessage(FMEVENT_TYPE.BROADCAST_CONNECTED, "aaaaa")
        val msg2 = FSEventMessage(FMEVENT_TYPE.BROADCAST_CONNECTED, "bbbbb")
        val msg3 = FSEventMessage(FMEVENT_TYPE.BROADCAST_CONNECTED, "ccccc")

        var runClient = Runnable {
            var client = Socket("localhost", PORT)
            var clientConn = FSEventConnection(client)

            clientConn.sendMessage(msg1)
            clientConn.sendMessage(msg2)
            clientConn.sendMessage(msg3)

            client.close()
        }

        var server = ServerSocket(PORT)
        Thread(runClient).start()
        var ss = server.accept()

        var serverConn = FSEventConnection(ss)
        var gotmsg1 = serverConn.getMessage()
        var gotmsg2 = serverConn.getMessage()
        var gotmsg3 = serverConn.getMessage()

        assertEquals(msg1.mEventcode, gotmsg1.mEventcode)
        assertEquals(msg1.userIdField.str, gotmsg1.userIdField.str)
        assertEquals(msg2.mEventcode, gotmsg2.mEventcode)
        assertEquals(msg2.userIdField.str, gotmsg2.userIdField.str)
        assertEquals(msg3.mEventcode, gotmsg3.mEventcode)
        assertEquals(msg3.userIdField.str, gotmsg3.userIdField.str)

        ss.close()
        server.close()
    }
}
