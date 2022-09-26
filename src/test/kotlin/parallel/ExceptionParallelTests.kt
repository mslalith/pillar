package parallel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tasks.PillarJobState
import utility.assertCompleted
import utility.assertFailed
import utility.createPillarJob
import utility.tasks.ParallelReturn10Task
import utility.tasks.ParallelReturn10TaskWithCrash
import utility.tasks.ParallelReturn5Task
import utility.tasks.ParallelReturn5TaskWithCrash

@OptIn(ExperimentalCoroutinesApi::class)
class ExceptionParallelTests {

    @Test
    fun `use single parallel task, verify state on crash`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        pillarJob.parallel(return5TaskWithCrash)

        launch(context = this.coroutineContext) {
            pillarJob.state.test {
                assertThat(awaitItem()).isEqualTo(PillarJobState.IDLE)
                assertThat(awaitItem()).isEqualTo(PillarJobState.RUNNING)
                assertThat(awaitItem()).isEqualTo(PillarJobState.COMPLETED)
                ensureAllEventsConsumed()
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use single parallel task, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        pillarJob.parallel(return5TaskWithCrash)

        pillarJob.start()
        return5TaskWithCrash.assertFailed()
    }

    @Test
    fun `use two parallel task, verify crash of first task`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val return10Task = ParallelReturn10Task()
        pillarJob.parallel(tasks = listOf(return5TaskWithCrash, return10Task))

        pillarJob.start()
        return5TaskWithCrash.assertFailed()
        return10Task.assertCompleted()
    }

    @Test
    fun `use two parallel task, verify crash of second task`() = runTest {
        val pillarJob = createPillarJob()
        val return5Task = ParallelReturn5Task()
        val return10TaskWithCrash = ParallelReturn10TaskWithCrash()
        pillarJob.parallel(tasks = listOf(return5Task, return10TaskWithCrash))

        pillarJob.start()
        return5Task.assertCompleted()
        return10TaskWithCrash.assertFailed()
    }

    @Test
    fun `use two parallel task, verify crash of both tasks`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val return10TaskWithCrash = ParallelReturn10TaskWithCrash()
        pillarJob.parallel(tasks = listOf(return5TaskWithCrash, return10TaskWithCrash))

        pillarJob.start()
        return5TaskWithCrash.assertFailed()
        return10TaskWithCrash.assertFailed()
    }
}
