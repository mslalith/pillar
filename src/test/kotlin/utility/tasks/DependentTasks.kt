package utility.tasks

import errors.PillarTaskException
import tasks.DependentPillarTask
import tasks.PillarTask
import utility.delayAndReturn

class DependentReturn5Task(
    private val delayInMillis: Long = 2_000,
    dependsOn: List<PillarTask<Int>> = listOf()
) : DependentPillarTask<Int, Int>(
    initial = 0,
    dependsOn = dependsOn
) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) { 5 }
}

class DependentReturn10Task(
    private val delayInMillis: Long = 2_000,
    dependsOn: List<PillarTask<Int>> = listOf()
) : DependentPillarTask<Int, Int>(
    initial = 0,
    dependsOn = dependsOn
) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) { 10 }
}

class DependentReturn15Task(
    private val delayInMillis: Long = 2_000,
    dependsOn: List<PillarTask<Int>> = listOf()
) : DependentPillarTask<Int, Int>(
    initial = 0,
    dependsOn = dependsOn
) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) { 15 }
}

class DependentReturn5TaskWithCrash(
    private val delayInMillis: Long = 2_000,
    dependsOn: List<PillarTask<Int>> = listOf()
) : DependentPillarTask<Int, Int>(
    initial = 0,
    dependsOn = dependsOn
) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) {
        throw PillarTaskException(message = "DependentReturn5TaskWithCrash run() crashed")
    }
}

class DependentReturn10TaskWithCrash(
    private val delayInMillis: Long = 2_000,
    dependsOn: List<PillarTask<Int>> = listOf()
) : DependentPillarTask<Int, Int>(
    initial = 0,
    dependsOn = dependsOn
) {
    override suspend fun run(): Int = delayAndReturn(delayInMillis) {
        throw PillarTaskException(message = "DependentReturn10TaskWithCrash run() crashed")
    }
}
