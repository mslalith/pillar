package utility.tasks

import errors.PillarTaskException
import tasks.ParallelPillarTask
import utility.delayAndReturn

class ParallelReturn5Task(private val delayInMillis: Long = 2_000) : ParallelPillarTask<Int>(initial = 0) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) { 5 }
}

class ParallelReturn10Task(private val delayInMillis: Long = 2_000) : ParallelPillarTask<Int>(initial = 0) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) { 10 }
}

class ParallelReturn15Task(private val delayInMillis: Long = 2_000) : ParallelPillarTask<Int>(initial = 0) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) { 15 }
}

class ParallelWithTypeTask<T>(
    initial: T,
    private val returns: T,
    private val delayInMillis: Long = 2_000
) : ParallelPillarTask<T>(initial = initial) {
    override suspend fun run(): T = delayAndReturn(delayInMillis) { returns }
}

class ParallelReturn5TaskWithCrash(private val delayInMillis: Long = 2_000) : ParallelPillarTask<Int>(initial = 0) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) {
        throw PillarTaskException(message = "ParallelReturn5TaskWithCrash run() crashed")
    }
}

class ParallelReturn10TaskWithCrash(private val delayInMillis: Long = 2_000) : ParallelPillarTask<Int>(initial = 0) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) {
        throw PillarTaskException(message = "ParallelReturn10TaskWithCrash run() crashed")
    }
}
