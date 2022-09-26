package errors

data class PillarTaskException(
    override val message: String? = null
) : Exception()
