package filesyncer.server

import filesyncer.common.message.FSMessage

interface FSMessageBroadcaster<T : FSMessage> {
    fun broadcast(msg: T)
    fun broadcast(msg: T, users: List<String>)
}
