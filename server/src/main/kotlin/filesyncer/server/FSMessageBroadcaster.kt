package filesyncer.server

import message.FSMessage

interface FSMessageBroadcaster<T : FSMessage> {
    fun broadcast(msg: T)
}
