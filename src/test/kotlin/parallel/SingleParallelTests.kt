package parallel

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import utility.TEST_COMPLETE_DELAY_OFFSET
import utility.assertItemConsumed
import utility.assertJobCancelled
import utility.assertJobCompleted
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
            pillarJob.assertJobCompleted()
            return5Task.assertItemConsumed(5)
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
            pillarJob.assertJobCancelled()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
        pillarJob.cancel()
    }
}
