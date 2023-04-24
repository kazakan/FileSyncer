package message

import filesyncer.common.message.FSMessage

abstract class FSMessageHandler {
    abstract fun handle(msg: FSMessage)
}
