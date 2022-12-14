package utility

import Pillar
import PillarJob
import app.cash.turbine.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import tasks.PillarJobState

data class TestModel1(val int: Int = 0)

data class TestModel2(
    val int: Int = 0,
    val string: String = ""
)

data class TestModel3(
    val int: Int = 0,
    val string: String = "",
    val model1: TestModel1 = TestModel1()
)

const val TEST_COMPLETE_DELAY_OFFSET = 200

fun CoroutineScope.createPillarJob() = Pillar.newJob(coroutineScope = this)

suspend fun <T> delayAndReturn(delayInMillis: Long, block: () -> T): T {
    delay(delayInMillis)
    return block()
}

suspend fun PillarJob.startAndMeasureCompletionTime(coroutineScope: CoroutineScope): Long {
    val startTime = System.currentTimeMillis()
    val elapsedTime: Long
    start()
    val turbine = state.testIn(coroutineScope)
    while (true) {
        if (turbine.awaitItem() == PillarJobState.COMPLETED) {
            elapsedTime = System.currentTimeMillis() - startTime
            break
        }
    }
    turbine.cancel()
    return elapsedTime
}
