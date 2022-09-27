package tasks

import errors.PillarDependantTaskException
import errors.PillarTaskException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

abstract class DependentPillarTask<T>(
    initial: T,
    private val dependsOn: List<PillarTask<*>>
) : PillarTask<T>(initial) {

    override suspend fun CoroutineScope.launchTask(): Result<T> {
        val dependencyDefersList = supervisorScope {
            dependsOn.map { dependentTask ->
                async {
                    dependentTask.execute(this)
                }
            }
        }
        dependencyDefersList.awaitAll()

        val failedTasks = dependsOn.filter {
            it.state.value is PillarTaskState.Failed || it.state.value is PillarTaskState.DependentFailed
        }
        return if (failedTasks.isEmpty()) runCurrentTask() else {
            Result.failure(PillarDependantTaskException(failedTasks = failedTasks))
        }
    }

    private suspend fun runCurrentTask() = try {
        Result.success(run())
    } catch (ex: PillarTaskException) {
        Result.failure(ex)
    }
}
