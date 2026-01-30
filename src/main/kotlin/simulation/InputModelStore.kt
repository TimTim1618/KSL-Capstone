package simulation

/**
 * Simple in-memory handoff between DistributionModeler and Model Executor.
 * Stores the last loaded dataset and file name.
 *
 * Later, we can extend this to store best-fit distribution + parameters too.
 */
object InputModelStore {

    @Volatile
    var lastFileName: String? = null
        private set

    @Volatile
    var lastData: DoubleArray? = null
        private set

    fun setData(fileName: String, data: DoubleArray) {
        lastFileName = fileName
        lastData = data
    }

    fun clear() {
        lastFileName = null
        lastData = null
    }

    fun summaryString(): String {
        val fn = lastFileName ?: "(none)"
        val n = lastData?.size ?: 0
        return "Input: $fn | n=$n"
    }
}
