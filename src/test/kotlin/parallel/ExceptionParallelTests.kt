package parallel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import utility.TestModel1
import utility.assertCompleted
import utility.assertFailed
import utility.assertJobCompleted
import utility.createPillarJob
import utility.tasks.ParallelReturn10Task
import utility.tasks.ParallelReturn10TaskWithCrash
import utility.tasks.ParallelReturn5Task
import utility.tasks.ParallelReturn5TaskWithCrash
import utility.tasks.ParallelWithTypeTask
import utility.tasks.ParallelWithTypeTaskWithCrash

@OptIn(ExperimentalCoroutinesApi::class)
class ExceptionParallelTests {

    @Test
    fun `use single parallel task, verify state on crash`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        pillarJob.enqueue(return5TaskWithCrash)

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use single parallel task, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        pillarJob.enqueue(return5TaskWithCrash)

        pillarJob.start()
        return5TaskWithCrash.assertFailed()
    }

    @Test
    fun `use single parallel task with non-primitive type, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val model1Task = ParallelWithTypeTaskWithCrash(initial = null)
        pillarJob.enqueue(model1Task)

        pillarJob.start()
        model1Task.assertFailed()
    }

    @Test
    fun `use two parallel tasks, verify crash of first task`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val return10Task = ParallelReturn10Task()
        pillarJob.enqueue(tasks = listOf(return5TaskWithCrash, return10Task))

        pillarJob.start()
        return5TaskWithCrash.assertFailed()
        return10Task.assertCompleted()
    }

    @Test
    fun `use two parallel tasks, verify crash of second task`() = runTest {
        val pillarJob = createPillarJob()
        val return5Task = ParallelReturn5Task()
        val return10TaskWithCrash = ParallelReturn10TaskWithCrash()
        pillarJob.enqueue(tasks = listOf(return5Task, return10TaskWithCrash))

        pillarJob.start()
        return5Task.assertCompleted()
        return10TaskWithCrash.assertFailed()
    }

    @Test
    fun `use two parallel tasks, verify crash of both tasks`() = runTest {
        val pillarJob = createPillarJob()
        val return5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val return10TaskWithCrash = ParallelReturn10TaskWithCrash()
        pillarJob.enqueue(tasks = listOf(return5TaskWithCrash, return10TaskWithCrash))

        pillarJob.start()
        return5TaskWithCrash.assertFailed()
        return10TaskWithCrash.assertFailed()
    }

    @Test
    fun `use two parallel tasks with non-primitive type, verify first is crashed`() = runTest {
        val pillarJob = createPillarJob()
        val model1Result = TestModel1()
        val model1Task = ParallelWithTypeTask(initial = null, returns = model1Result)
        val model2Task = ParallelWithTypeTaskWithCrash(initial = null)
        pillarJob.enqueue(tasks = listOf(model1Task, model2Task))

        pillarJob.start()
        model1Task.assertCompleted()
        model2Task.assertFailed()
    }

    @Test
    fun `use two parallel tasks with non-primitive type, verify second is crashed`() = runTest {
        val pillarJob = createPillarJob()
        val model2Result = TestModel1()
        val model1Task = ParallelWithTypeTaskWithCrash(initial = null)
        val model2Task = ParallelWithTypeTask(initial = null, returns = model2Result)
        pillarJob.enqueue(tasks = listOf(model1Task, model2Task))

        pillarJob.start()
        model1Task.assertFailed()
        model2Task.assertCompleted()
    }

    @Test
    fun `use two parallel tasks with non-primitive type, verify both are crash`() = runTest {
        val pillarJob = createPillarJob()
        val model1Task = ParallelWithTypeTaskWithCrash(initial = null)
        val model2Task = ParallelWithTypeTaskWithCrash(initial = null)
        pillarJob.enqueue(tasks = listOf(model1Task, model2Task))

        pillarJob.start()
        model1Task.assertFailed()
        model2Task.assertFailed()
    }
}
