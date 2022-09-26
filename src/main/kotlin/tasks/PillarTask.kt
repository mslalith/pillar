package tasks

import errors.PillarDependantTaskException
import errors.PillarTaskException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class PillarTask<T>(initial: T) {

    private val _data = MutableStateFlow(initial)
    val data: StateFlow<T>
        get() = _data

    private val _state: MutableStateFlow<PillarTaskState> = MutableStateFlow(value = PillarTaskState.Idle)
    val state: StateFlow<PillarTaskState>
        get() = _state

    abstract suspend fun run(): T

    internal abstract suspend fun CoroutineScope.launchTask(): Result<T>

    internal suspend fun execute(coroutineScope: CoroutineScope) = coroutineScope.launch {
        _state.value = PillarTaskState.Running
        launchTask().onSuccess {
            handleOnSuccess(it)
        }.onFailure {
            handleOnFailure(it)
        }
    }

    private fun handleOnSuccess(result: T) {
        _data.value = result
        _state.value = PillarTaskState.Completed
    }

    private fun handleOnFailure(throwable: Throwable) {
        when (throwable) {
            is CancellationException -> _state.value = PillarTaskState.Cancelled
            is PillarTaskException -> _state.value = PillarTaskState.Failed(throwable)
            is PillarDependantTaskException -> _state.value = PillarTaskState.DependentFailed(throwable)
            else -> Unit
        }
    }
}
