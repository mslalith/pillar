package dependent

import app.cash.turbine.test
import app.cash.turbine.testIn
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
import utility.assertJobCancelled
import utility.createPillarJob
import utility.startAndMeasureCompletionTime
import utility.tasks.DependentReturn10Task
import utility.tasks.DependentReturn5Task
import utility.tasks.ParallelReturn10Task
import utility.tasks.ParallelReturn15Task
import utility.tasks.ParallelReturn5Task

@OptIn(ExperimentalCoroutinesApi::class)
internal class MultipleDependentTests {

    @Test
    fun `use multiple dependent tasks, verify combined result`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val parallelReturn10Task = ParallelReturn10Task()
        val parallelReturn15Task = ParallelReturn15Task()
        val dependentReturn5Task = DependentReturn5Task(
            dependsOn = listOf(parallelReturn5Task)
        )
        val dependentReturn10Task = DependentReturn10Task(
            dependsOn = listOf(parallelReturn10Task, parallelReturn15Task)
        )

        val tasks = listOf(dependentReturn5Task, dependentReturn10Task)
        pillarJob.parallel(tasks = tasks)

        launch(context = this.coroutineContext) {
            coroutineScope {
                pillarJob.state.test {
                    assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                    assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                    assertThat(awaitItem()).isEqualTo(PillarJobState.COMPLETED)
                    ensureAllEventsConsumed()

                    val tasksResultSum = tasks.sumOf { it.data.value }
                    assertThat(tasksResultSum).isEqualTo(15)
                }
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()

        val dependentReturn10TaskTurbine = dependentReturn10Task.data.testIn(scope = this)
        assertThat(parallelReturn10Task.data.value).isEqualTo(0)
        assertThat(parallelReturn15Task.data.value).isEqualTo(0)
        assertThat(dependentReturn10TaskTurbine.awaitItem()).isEqualTo(0)
        assertThat(dependentReturn10TaskTurbine.awaitItem()).isEqualTo(10)
        assertThat(parallelReturn10Task.data.value).isEqualTo(10)
        assertThat(parallelReturn15Task.data.value).isEqualTo(15)
        dependentReturn10TaskTurbine.cancel()
    }

    @Test
    fun `use multiple dependent tasks, verify time taken`() = runBlocking {
        val pillarJob = createPillarJob()
        val delayInMillis = 6_000
        val parallelReturn5Task = ParallelReturn5Task(delayInMillis = 2_000)
        val parallelReturn10Task = ParallelReturn10Task(delayInMillis = 3_000)
        val parallelReturn15Task = ParallelReturn15Task(delayInMillis = 4_000)
        val dependentReturn5Task = DependentReturn5Task(
            delayInMillis = 3_000,
            dependsOn = listOf(parallelReturn5Task)
        )
        val dependentReturn10Task = DependentReturn10Task(
            delayInMillis = 2_000,
            dependsOn = listOf(parallelReturn10Task, parallelReturn15Task)
        )

        val tasks = listOf(dependentReturn5Task, dependentReturn10Task)
        pillarJob.parallel(tasks = tasks)

        val timeTaken = pillarJob.startAndMeasureCompletionTime(this)
        assertThat(timeTaken).isGreaterThan(delayInMillis)
        assertThat(timeTaken).isLessThan(delayInMillis + TEST_COMPLETE_DELAY_OFFSET)
    }

    @Test
    fun `use multiple dependent tasks, verify cancellation`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val parallelReturn10Task = ParallelReturn10Task()
        val parallelReturn15Task = ParallelReturn15Task()
        val dependentReturn5Task = DependentReturn5Task(
            dependsOn = listOf(parallelReturn5Task)
        )
        val dependentReturn10Task = DependentReturn10Task(
            dependsOn = listOf(parallelReturn10Task, parallelReturn15Task)
        )

        val tasks = listOf(dependentReturn5Task, dependentReturn10Task)
        pillarJob.parallel(tasks = tasks)

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCancelled()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
        pillarJob.cancel()
    }
}
