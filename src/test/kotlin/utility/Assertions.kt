package utility

import app.cash.turbine.testIn
import kotlinx.coroutines.coroutineScope
import tasks.PillarTask
import tasks.PillarTaskState

suspend inline fun <T, reified S: PillarTaskState> PillarTask<T>.assertState() = coroutineScope {
    val turbine = state.testIn(this)
    val expectMostRecentItem = turbine.expectMostRecentItem()
    if (expectMostRecentItem !is S) {
        while (true) {
            val awaitItem = turbine.awaitItem()
            if (awaitItem is S) break
        }
    }
    turbine.cancel()
}

suspend fun <T> PillarTask<T>.assertCompleted() {
    assertState<T, PillarTaskState.Completed>()
}

suspend fun <T> PillarTask<T>.assertFailed() {
    assertState<T, PillarTaskState.Failed>()
}

suspend fun <T> PillarTask<T>.assertDependentFailed() {
    assertState<T, PillarTaskState.DependentFailed>()
}
