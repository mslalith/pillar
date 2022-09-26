import kotlinx.coroutines.CoroutineScope

object Pillar {
    fun newJob(coroutineScope: CoroutineScope): PillarJob {
        return PillarJobImpl(coroutineScope = coroutineScope)
    }
}
