package dependent

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import utility.TestModel1
import utility.TestModel2
import utility.TestModel3
import utility.assertCompleted
import utility.assertItemConsumed
import utility.assertJobCompleted
import utility.createPillarJob
import utility.tasks.DependentWithTypeTask
import utility.tasks.ParallelWithTypeTask

@OptIn(ExperimentalCoroutinesApi::class)
class MultiTypeDependentTests {

    @Test
    fun `use 3 dependent tasks with different primitive types, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val intTask = DependentWithTypeTask(initial = 0, returns = 10)
        val stringTask = DependentWithTypeTask(initial = "", returns = "Hello")
        val booleanTask = DependentWithTypeTask(initial = false, returns = true)
        pillarJob.parallel(tasks = listOf(intTask, stringTask, booleanTask))

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
    fun `use 3 dependent tasks with different non-primitive types, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val model1Result = TestModel1()
        val model2Result = TestModel2()
        val model3Result = TestModel3()
        val model1Task = DependentWithTypeTask(initial = null, returns = model1Result)
        val model2Task = DependentWithTypeTask(initial = null, returns = model2Result)
        val model3Task = DependentWithTypeTask(initial = null, returns = model3Result)
        pillarJob.parallel(tasks = listOf(model1Task, model2Task, model3Task))

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()

            model1Task.assertItemConsumed(model1Result)
            model2Task.assertItemConsumed(model2Result)
            model3Task.assertItemConsumed(model3Result)
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use 2 dependent tasks with different types which depends on parallel tasks, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val model1Result = TestModel1()
        val model2Result = TestModel2()
        val parallelModel1Task = ParallelWithTypeTask(initial = null, returns = model1Result)
        val model1Task = DependentWithTypeTask(initial = null, returns = model1Result)
        val model2Task = DependentWithTypeTask(
            initial = null,
            returns = model2Result,
            dependsOn = listOf(parallelModel1Task)
        )
        pillarJob.parallel(tasks = listOf(model1Task, model2Task))

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()

            model1Task.assertItemConsumed(model1Result)
            model1Task.assertCompleted()

            parallelModel1Task.assertItemConsumed(model1Result)
            parallelModel1Task.assertCompleted()
            model2Task.assertItemConsumed(model2Result)
            model2Task.assertCompleted()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use single dependent tasks with different types which has inner dependencies, verify result`() = runTest {
        val pillarJob = createPillarJob()
        val model1Result = TestModel1()
        val model2Result = TestModel2()
        val parallelModel1Task = ParallelWithTypeTask(initial = null, returns = model1Result)
        val model1Task = DependentWithTypeTask(
            initial = null,
            returns = model1Result,
            dependsOn = listOf(parallelModel1Task)
        )
        val model2Task = DependentWithTypeTask(
            initial = null,
            returns = model2Result,
            dependsOn = listOf(model1Task)
        )
        pillarJob.parallel(tasks = listOf(model1Task, model2Task))

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()

            parallelModel1Task.assertItemConsumed(model1Result)
            parallelModel1Task.assertCompleted()

            model1Task.assertItemConsumed(model1Result)
            model1Task.assertCompleted()

            model2Task.assertItemConsumed(model2Result)
            model2Task.assertCompleted()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }
}
