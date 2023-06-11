package filesyncer.common

import kotlin.math.max

/** Simple LogicalClock class. */
class FSLogicalClock {
    var time = 0L
        private set
    var lastSynched = 0L
        private set

    /** Set [time] to max([value], [time]) */
    fun sync(value: Long) {
        synchronized(this) {
            time = max(value, time)
            lastSynched = time
        }
    }

    /** Add 1 to [time] and return [time]. Call get() whenever you need clock value. */
    fun get(): Long {
        synchronized(this) { ++time }
        return time
    }
}
