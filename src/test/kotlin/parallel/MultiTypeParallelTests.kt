package parallel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tasks.PillarJobState
import utility.TestModel1
import utility.TestModel2
import utility.TestModel3
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
        pillarJob.parallel(tasks = listOf(intTask, stringTask, booleanTask))

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
                intTask.data.test {
                    assertThat(awaitItem()).isEqualTo(10)
                    ensureAllEventsConsumed()
                }
            }


            coroutineScope {
                stringTask.data.test {
                    assertThat(awaitItem()).isEqualTo("Hello")
                    ensureAllEventsConsumed()
                }
            }

            coroutineScope {
                booleanTask.data.test {
                    assertThat(awaitItem()).isEqualTo(true)
                    ensureAllEventsConsumed()
                }
            }
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
        pillarJob.parallel(tasks = listOf(model1Task, model2Task, model3Task))

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
                model1Task.data.test {
                    assertThat(awaitItem()).isEqualTo(model1Result)
                    ensureAllEventsConsumed()
                }
            }


            coroutineScope {
                model2Task.data.test {
                    assertThat(awaitItem()).isEqualTo(model2Result)
                    ensureAllEventsConsumed()
                }
            }

            coroutineScope {
                model3Task.data.test {
                    assertThat(awaitItem()).isEqualTo(model3Result)
                    ensureAllEventsConsumed()
                }
            }
        }

        advanceTimeBy(delayTimeMillis = 200)
        pillarJob.start()
    }
}
