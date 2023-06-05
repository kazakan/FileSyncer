package filesyncer.common

class FSRequestFsm {

    interface OnStateChangeListener {
        fun onStateChange(state: STATE)
    }
    enum class STATE {
        NOT_WAITING,
        WAITING,
        GRANTED,
        REJECTED
    }

    var state = STATE.NOT_WAITING
        private set

    var stateChangeListener: OnStateChangeListener? = null

    fun reset() {
        state = STATE.NOT_WAITING
        stateChangeListener?.onStateChange(state)
    }

    fun wait() {
        state = STATE.WAITING
        stateChangeListener?.onStateChange(state)
        while (state == STATE.WAITING) {
            Thread.sleep(200L)
        }
    }

    fun grant() {
        state = STATE.GRANTED
        stateChangeListener?.onStateChange(state)
    }

    fun reject() {
        state = STATE.REJECTED
        stateChangeListener?.onStateChange(state)
    }
}
