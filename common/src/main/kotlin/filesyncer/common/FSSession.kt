package filesyncer.common

import java.net.Socket

abstract class FSSession(val socket: Socket, val verbose: Boolean = false) {
    enum class State {
        NOT_CONNECTED,
        CONNECTED,
        LOGGED_IN,
        CLOSED
    }

    var state = State.NOT_CONNECTED
    var user: FSUser? = null
    lateinit var worker: FSEventConnWorker

    init {
        run {
            if (socket.isConnected) state = State.CONNECTED
            if (socket.isClosed) {
                if (verbose) println("Initializing FSSession but, socket is already closed.")
                state = State.CLOSED
                return@run
            }

            worker = createConnWorker(socket)
            worker.run()
        }
    }

    abstract fun createConnWorker(socket: Socket): FSEventConnWorker
}
