package dependent

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import utility.TestModel1
import utility.TestModel2
import utility.assertCompleted
import utility.assertDependentFailed
import utility.assertFailed
import utility.assertJobCompleted
import utility.createPillarJob
import utility.tasks.DependentReturn10Task
import utility.tasks.DependentReturn10TaskWithCrash
import utility.tasks.DependentReturn15Task
import utility.tasks.DependentReturn5Task
import utility.tasks.DependentReturn5TaskWithCrash
import utility.tasks.DependentWithTypeTask
import utility.tasks.DependentWithTypeTaskWithCrash
import utility.tasks.ParallelReturn10Task
import utility.tasks.ParallelReturn5Task
import utility.tasks.ParallelReturn5TaskWithCrash
import utility.tasks.ParallelWithTypeTask
import utility.tasks.ParallelWithTypeTaskWithCrash

@OptIn(ExperimentalCoroutinesApi::class)
class ExceptionDependentTests {

    @Test
    fun `use single dependent task with no dependencies, verify state on crash`() = runTest {
        val pillarJob = createPillarJob()
        val dependentReturn5TaskWithCrash = DependentReturn5TaskWithCrash()
        pillarJob.parallel(dependentReturn5TaskWithCrash)

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use multiple dependent task with no dependencies, verify state on crash`() = runTest {
        val pillarJob = createPillarJob()
        val dependentReturn5Task = DependentReturn5Task()
        val dependentReturn10TaskWithCrash = DependentReturn10TaskWithCrash()
        pillarJob.parallel(tasks = listOf(dependentReturn5Task, dependentReturn10TaskWithCrash))

        launch(context = this.coroutineContext) {
            pillarJob.assertJobCompleted()
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }

    @Test
    fun `use single dependent task with 1 dependency, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val dependentReturn5TaskWithCrash = DependentReturn5TaskWithCrash(
            dependsOn = listOf(parallelReturn5Task)
        )
        pillarJob.parallel(dependentReturn5TaskWithCrash)

        pillarJob.start()
        assertThat(parallelReturn5Task.data.value).isEqualTo(0)
        dependentReturn5TaskWithCrash.assertFailed()
        assertThat(parallelReturn5Task.data.value).isEqualTo(5)
    }

    @Test
    fun `use single dependent task with 1 dependency which crashes, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val dependentReturn5Task = DependentReturn5Task(
            dependsOn = listOf(parallelReturn5TaskWithCrash)
        )
        pillarJob.parallel(dependentReturn5Task)

        pillarJob.start()
        parallelReturn5TaskWithCrash.assertFailed()
        dependentReturn5Task.assertDependentFailed()
    }

    @Test
    fun `use single non-primitive dependent task with 1 dependency, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val parallelTask = ParallelWithTypeTask(initial = null, returns = TestModel1())
        val dependentTaskWithCrash = DependentWithTypeTaskWithCrash(
            initial = null,
            dependsOn = listOf(parallelTask)
        )
        pillarJob.parallel(dependentTaskWithCrash)

        pillarJob.start()
        parallelTask.assertCompleted()
        dependentTaskWithCrash.assertFailed()
    }

    @Test
    fun `use single non-primitive dependent task with 1 dependency which crashes, verify crash`() = runTest {
        val pillarJob = createPillarJob()
        val parallelTask = ParallelWithTypeTaskWithCrash(initial = null)
        val dependentTask = DependentWithTypeTask(
            initial = null,
            returns = TestModel1(),
            dependsOn = listOf(parallelTask)
        )
        pillarJob.parallel(dependentTask)

        pillarJob.start()
        parallelTask.assertFailed()
        dependentTask.assertDependentFailed()
    }

    @Test
    fun `use single dependent task with 1 dependency, verify crash where both crashes`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val dependentReturn5TaskWithCrash = DependentReturn5TaskWithCrash(
            dependsOn = listOf(parallelReturn5TaskWithCrash)
        )
        pillarJob.parallel(dependentReturn5TaskWithCrash)

        pillarJob.start()
        parallelReturn5TaskWithCrash.assertFailed()
        dependentReturn5TaskWithCrash.assertDependentFailed()
    }

    @Test
    fun `use two dependent task, verify crash of first task`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val parallelReturn10Task = ParallelReturn10Task()
        val dependentReturn5TaskWithCrash = DependentReturn5TaskWithCrash(
            dependsOn = listOf(parallelReturn5Task)
        )
        val dependentReturn10Task = DependentReturn10Task(
            dependsOn = listOf(parallelReturn10Task)
        )
        pillarJob.parallel(tasks = listOf(dependentReturn5TaskWithCrash, dependentReturn10Task))

        pillarJob.start()
        parallelReturn5Task.assertCompleted()
        dependentReturn5TaskWithCrash.assertFailed()
        parallelReturn10Task.assertCompleted()
        dependentReturn10Task.assertCompleted()
    }

    @Test
    fun `use two dependent task, verify crash of second task`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val parallelReturn10Task = ParallelReturn10Task()
        val dependentReturn5Task = DependentReturn5Task(
            dependsOn = listOf(parallelReturn5Task)
        )
        val dependentReturn10TaskWithCrash = DependentReturn10TaskWithCrash(
            dependsOn = listOf(parallelReturn10Task)
        )
        pillarJob.parallel(tasks = listOf(dependentReturn5Task, dependentReturn10TaskWithCrash))

        pillarJob.start()
        parallelReturn5Task.assertCompleted()
        dependentReturn5Task.assertCompleted()
        parallelReturn10Task.assertCompleted()
        dependentReturn10TaskWithCrash.assertFailed()
    }

    @Test
    fun `use single dependent task which has 2 inner dependencies, leaf task should fail`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5TaskWithCrash = ParallelReturn5TaskWithCrash()
        val dependentReturn10Task = DependentReturn10Task(
            dependsOn = listOf(parallelReturn5TaskWithCrash)
        )
        val dependentReturn15Task = DependentReturn15Task(
            dependsOn = listOf(dependentReturn10Task)
        )
        pillarJob.parallel(dependentReturn15Task)

        pillarJob.start()
        parallelReturn5TaskWithCrash.assertFailed()
        dependentReturn10Task.assertDependentFailed()
        dependentReturn15Task.assertDependentFailed()
    }

    @Test
    fun `use single dependent task which has 2 inner non-primitive dependencies, leaf task should fail`() = runTest {
        val pillarJob = createPillarJob()
        val parallelTaskWithCrash = ParallelWithTypeTaskWithCrash(initial = null)
        val dependentTask1 = DependentWithTypeTask(
            initial = null,
            returns = TestModel1(),
            dependsOn = listOf(parallelTaskWithCrash)
        )
        val dependentTask2 = DependentWithTypeTask(
            initial = null,
            returns = TestModel2(),
            dependsOn = listOf(dependentTask1)
        )
        pillarJob.parallel(dependentTask2)

        pillarJob.start()
        parallelTaskWithCrash.assertFailed()
        dependentTask1.assertDependentFailed()
        dependentTask2.assertDependentFailed()
    }

    @Test
    fun `use single dependent task which has 2 inner dependencies, inner dependent task should fail`() = runTest {
        val pillarJob = createPillarJob()
        val parallelReturn5Task = ParallelReturn5Task()
        val dependentReturn10TaskWithCrash = DependentReturn10TaskWithCrash(
            dependsOn = listOf(parallelReturn5Task)
        )
        val dependentReturn15Task = DependentReturn15Task(
            dependsOn = listOf(dependentReturn10TaskWithCrash)
        )
        pillarJob.parallel(dependentReturn15Task)

        pillarJob.start()
        parallelReturn5Task.assertCompleted()
        dependentReturn10TaskWithCrash.assertFailed()
        dependentReturn15Task.assertDependentFailed()
    }

    @Test
    fun `use single dependent task which has 2 inner non-primitive dependencies, inner dependent task should fail`() = runTest {
        val pillarJob = createPillarJob()
        val parallelTask = ParallelWithTypeTask(initial = null, returns = TestModel1())
        val dependentTask1WithCrash = DependentWithTypeTaskWithCrash(
            initial = null,
            dependsOn = listOf(parallelTask)
        )
        val dependentTask2 = DependentWithTypeTask(
            initial = null,
            returns = TestModel2(),
            dependsOn = listOf(dependentTask1WithCrash)
        )
        pillarJob.parallel(dependentTask2)

        pillarJob.start()
        parallelTask.assertCompleted()
        dependentTask1WithCrash.assertFailed()
        dependentTask2.assertDependentFailed()
    }
}
