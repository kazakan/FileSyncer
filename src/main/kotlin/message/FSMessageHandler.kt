package message

abstract class FSMessageHandler {
    abstract fun handle(msg: FSMessage)
}
