package filesyncer.server

import filesyncer.common.message.FSMessage

interface FSMessageBroadcaster<T : FSMessage> {
    fun broadcast(msg: T)
}
