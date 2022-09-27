package parallel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import utility.TestModel1
import utility.TestModel2
import utility.TestModel3
import utility.assertItemConsumed
import utility.assertJobCompleted
import utility.createPillarJob
import utility.tasks.ParallelWithTypeTask

@OptIn(ExperimentalCoroutinesApi::class)
class MultiTypeParallelTests {

    @Test
    fun `use 3 parallel tasks with different primitive types, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val intTask = ParallelWithTypeTask(initial = 0, returns = 10)
        val stringTask = ParallelWithTypeTask(initial = "", returns = "Hello")
        val booleanTask = ParallelWithTypeTask(initial = false, returns = true)
        pillarJob.enqueue(tasks = listOf(intTask, stringTask, booleanTask))

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()

            intTask.assertItemConsumed(10)
            stringTask.assertItemConsumed("Hello")
            booleanTask.assertItemConsumed(true)
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use 3 parallel tasks with different non-primitive types, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val model1Result = TestModel1()
        val model2Result = TestModel2()
        val model3Result = TestModel3()
        val model1Task = ParallelWithTypeTask(initial = null, returns = model1Result)
        val model2Task = ParallelWithTypeTask(initial = null, returns = model2Result)
        val model3Task = ParallelWithTypeTask(initial = null, returns = model3Result)
        pillarJob.enqueue(tasks = listOf(model1Task, model2Task, model3Task))

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()

            model1Task.assertItemConsumed(model1Result)
            model2Task.assertItemConsumed(model2Result)
            model3Task.assertItemConsumed(model3Result)
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }
}
