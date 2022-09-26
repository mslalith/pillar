package dependent

import app.cash.turbine.test
import app.cash.turbine.testIn
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tasks.PillarJobState
import utility.TEST_COMPLETE_DELAY_OFFSET
import utility.createPillarJob
import utility.startAndMeasureCompletionTime
import utility.tasks.DependentReturn5Task
import utility.tasks.ParallelReturn5Task

@OptIn(ExperimentalCoroutinesApi::class)
internal class SingleDependentTests {

    @Test
    fun `use single dependent task, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val dependentReturn5Task = DependentReturn5Task(
            dependsOn = listOf(parallelReturn5Task)
        )
        pillarJob.parallel(dependentReturn5Task)

        launch(context = this.coroutineContext) {
            pillarJob.state.test {
                assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                assertThat(awaitItem()).isEqualTo(PillarJobState.COMPLETED)
                expectNoEvents()
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()

        val dependentReturn5TaskTurbine = dependentReturn5Task.data.testIn(scope = this)
        assertThat(parallelReturn5Task.data.value).isEqualTo(0)
        assertThat(dependentReturn5TaskTurbine.awaitItem()).isEqualTo(0)
        assertThat(dependentReturn5TaskTurbine.awaitItem()).isEqualTo(5)
        assertThat(parallelReturn5Task.data.value).isEqualTo(5)
        dependentReturn5TaskTurbine.cancel()
    }

    @Test
    fun `use single dependent task, verify time taken`() = runBlocking {
        val pillarJob = createPillarJob()
        val delayInMillis = 5_000L
        val parallelReturn5Task = ParallelReturn5Task(delayInMillis = 2_000)
        val dependentReturn5Task = DependentReturn5Task(
            delayInMillis = 3_000L,
            dependsOn = listOf(parallelReturn5Task)
        )
        pillarJob.parallel(dependentReturn5Task)

        val timeTaken = pillarJob.startAndMeasureCompletionTime(this)
        assertThat(timeTaken).isGreaterThan(delayInMillis)
        assertThat(timeTaken).isLessThan(delayInMillis + TEST_COMPLETE_DELAY_OFFSET)
    }

    @Test
    fun `use single dependent task, verify cancellation`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val dependentReturn5Task = DependentReturn5Task(
            dependsOn = listOf(parallelReturn5Task)
        )
        pillarJob.parallel(dependentReturn5Task)

        launch(context = this.coroutineContext) {
            pillarJob.state.test {
                assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                assertThat(awaitItem()).isEqualTo(PillarJobState.CANCELLED)
                expectNoEvents()
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
        pillarJob.cancel()
    }
}
