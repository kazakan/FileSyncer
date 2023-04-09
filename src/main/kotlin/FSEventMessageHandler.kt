import message.FSEventMessage

interface FSEventMessageHandler {
  fun handleMessage(msg: FSEventMessage)
}
