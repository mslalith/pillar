package parallel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tasks.PillarJobState
import utility.TEST_COMPLETE_DELAY_OFFSET
import utility.createPillarJob
import utility.startAndMeasureCompletionTime
import utility.tasks.ParallelReturn5Task

@OptIn(ExperimentalCoroutinesApi::class)
internal class SingleParallelTests {
    
    @Test
    fun `use single parallel task, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val return5Task = ParallelReturn5Task()
        pillarJob.parallel(return5Task)

        launch(context = this.coroutineContext) {
            coroutineScope {
                pillarJob.state.test {
                    assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                    assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                    assertThat(awaitItem()).isEqualTo(PillarJobState.COMPLETED)
                    ensureAllEventsConsumed()
                }
            }

            coroutineScope {
                return5Task.data.test {
                    assertThat(awaitItem()).isEqualTo(5)
                    ensureAllEventsConsumed()
                }
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use single parallel task, verify time taken`() = runBlocking {
        val pillarJob = createPillarJob()
        val delayInMillis = 3_000L
        val return5Task = ParallelReturn5Task(delayInMillis = delayInMillis)
        pillarJob.parallel(return5Task)

        val timeTaken = pillarJob.startAndMeasureCompletionTime(this)
        assertThat(timeTaken).isGreaterThan(delayInMillis)
        assertThat(timeTaken).isLessThan(delayInMillis + TEST_COMPLETE_DELAY_OFFSET)
    }

    @Test
    fun `use single parallel task, verify cancellation`() = runTest {
        val pillarJob = createPillarJob()
        val return5Task = ParallelReturn5Task()
        pillarJob.parallel(return5Task)

        launch(context = this.coroutineContext) {
            pillarJob.state.test {
                assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                assertThat(awaitItem()).isEqualTo(PillarJobState.CANCELLED)
                cancel()
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
        pillarJob.cancel()
    }
}
