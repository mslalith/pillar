package tasks

import errors.PillarTaskException
import kotlinx.coroutines.CoroutineScope

abstract class ParallelPillarTask<T>(initial: T) : PillarTask<T>(initial) {

    override suspend fun CoroutineScope.launchTask(): Result<T> = try {
        Result.success(run())
    } catch (ex: PillarTaskException) {
        Result.failure(ex)
    }
}
