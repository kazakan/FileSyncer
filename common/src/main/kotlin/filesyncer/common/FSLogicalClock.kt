package filesyncer.common

import kotlin.math.max

/** Simple LogicalClock class. */
class FSLogicalClock(private var time: Long = 0L) {

    /** Set [time] to max([value], [time]) */
    fun sync(value: Long) {
        time = max(value, time)
    }

    /** Add 1 to [time] and return [time]. Call get() whenever you need clock value. */
    fun get(): Long {
        return ++time
    }
}
