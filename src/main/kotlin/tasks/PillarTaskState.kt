package tasks

import errors.PillarDependantTaskException

sealed interface PillarTaskState {
    object Idle : PillarTaskState
    object Running: PillarTaskState
    object Completed: PillarTaskState
    data class Failed(val throwable: Throwable): PillarTaskState
    data class DependentFailed(val exception: PillarDependantTaskException): PillarTaskState
    object Cancelled: PillarTaskState
}
