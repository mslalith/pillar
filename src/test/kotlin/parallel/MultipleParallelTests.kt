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
import utility.assertItemConsumed
import utility.createPillarJob
import utility.startAndMeasureCompletionTime
import utility.tasks.ParallelReturn10Task
import utility.tasks.ParallelReturn15Task
import utility.tasks.ParallelReturn5Task

@OptIn(ExperimentalCoroutinesApi::class)
internal class MultipleParallelTests {

    @Test
    fun `use multiple parallel tasks, verify combined result`() = runTest {
        val pillarJob = createPillarJob()
        val return5Task = ParallelReturn5Task()
        val return10Task = ParallelReturn10Task()
        val return15Task = ParallelReturn15Task()
        val tasks = listOf(return5Task, return10Task, return15Task)
        pillarJob.parallel(tasks = tasks)

        launch(context = this.coroutineContext) {
            coroutineScope {
                pillarJob.state.test {
                    assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                    assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                    assertThat(awaitItem()).isEqualTo(PillarJobState.COMPLETED)
                    ensureAllEventsConsumed()

                    val tasksResultSum = tasks.sumOf { it.data.value }
                    assertThat(tasksResultSum).isEqualTo(30)
                }
            }

            return5Task.assertItemConsumed(5)
            return10Task.assertItemConsumed(10)
            return15Task.assertItemConsumed(15)
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use multiple parallel tasks, verify time taken`() = runBlocking {
        val pillarJob = createPillarJob()
        val delayInMillis = 4_000
        val return5Task = ParallelReturn5Task(delayInMillis = 2_000)
        val return10Task = ParallelReturn10Task(delayInMillis = 3_000)
        val return15Task = ParallelReturn15Task(delayInMillis = 4_000)
        val tasks = listOf(return5Task, return10Task, return15Task)
        pillarJob.parallel(tasks = tasks)

        val timeTaken = pillarJob.startAndMeasureCompletionTime(this)
        assertThat(timeTaken).isGreaterThan(delayInMillis)
        assertThat(timeTaken).isLessThan(delayInMillis + TEST_COMPLETE_DELAY_OFFSET)
    }

    @Test
    fun `use multiple parallel tasks, verify cancellation`() = runTest {
        val pillarJob = createPillarJob()
        val return5Task = ParallelReturn5Task()
        val return10Task = ParallelReturn10Task()
        val return15Task = ParallelReturn15Task()
        val tasks = listOf(return5Task, return10Task, return15Task)
        pillarJob.parallel(tasks = tasks)

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
