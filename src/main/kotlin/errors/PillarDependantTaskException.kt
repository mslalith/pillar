package errors

import tasks.PillarTask

data class PillarDependantTaskException(
    val failedTasks: List<PillarTask<*>>,
    override val message: String? = null
) : Exception()
