import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tasks.PillarJobState
import tasks.PillarTask

interface PillarJob {
    val state: StateFlow<PillarJobState>

    fun <T> parallel(task: PillarTask<T>)
    fun <T> parallel(tasks: List<PillarTask<T>>)

    fun start()
    fun cancel()
}

internal class PillarJobImpl(
    private val coroutineScope: CoroutineScope
) : PillarJob {

    private val _state = MutableStateFlow(value = PillarJobState.IDLE)
    override val state: StateFlow<PillarJobState>
        get() = _state

    private val tasks: MutableList<PillarTask<*>> = mutableListOf()

    private var pillarJob: Job? = null

    override fun <T> parallel(task: PillarTask<T>) {
        tasks.add(task)
    }

    override fun <T> parallel(tasks: List<PillarTask<T>>) {
        tasks.forEach { parallel(it) }
    }

    override fun start() {
        _state.value = PillarJobState.RUNNING
        coroutineScope.launch {
            tasks.forEach {
                it.execute(this)
            }
        }.also { job ->
            job.invokeOnCompletion {
                handleJobComplete(it)
            }
            pillarJob = job
        }
    }

    override fun cancel() {
        pillarJob?.cancel()
        pillarJob = null
    }

    private fun handleJobComplete(throwable: Throwable?) {
        when (throwable) {
            null -> _state.value = PillarJobState.COMPLETED
            is CancellationException -> _state.value = PillarJobState.CANCELLED
            else -> _state.value = PillarJobState.FAILED
        }
    }
}
