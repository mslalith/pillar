package utility

import PillarJob
import app.cash.turbine.test
import app.cash.turbine.testIn
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.coroutineScope
import tasks.PillarJobState
import tasks.PillarTask
import tasks.PillarTaskState

suspend fun PillarJob.assertJobState(expectedState: PillarJobState) = coroutineScope {
    state.test {
        assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
        assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
        assertThat(awaitItem()).isEqualTo(expectedState)
        ensureAllEventsConsumed()
    }
}

suspend fun PillarJob.assertJobCompleted() = assertJobState(PillarJobState.COMPLETED)
suspend fun PillarJob.assertJobCancelled() = assertJobState(PillarJobState.CANCELLED)

suspend fun <T> PillarTask<T>.assertItemConsumed(expected: T) = coroutineScope {
    data.test {
        assertThat(awaitItem()).isEqualTo(expected)
        ensureAllEventsConsumed()
    }
}

suspend inline fun <T, reified S : PillarTaskState> PillarTask<T>.assertState() = coroutineScope {
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

suspend fun <T> PillarTask<T>.assertCompleted() = assertState<T, PillarTaskState.Completed>()
suspend fun <T> PillarTask<T>.assertFailed() = assertState<T, PillarTaskState.Failed>()
suspend fun <T> PillarTask<T>.assertDependentFailed() = assertState<T, PillarTaskState.DependentFailed>()
