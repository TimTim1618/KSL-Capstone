package work

data class HistogramModel(
    var numBins: Int = 10,
    var minValue: Double = 0.0,
    var maxValue: Double = 100.0,
    var title: String = "Sample Histogram"
) {
    fun controls() = mapOf(
        "numBins" to numBins,
        "minValue" to minValue,
        "maxValue" to maxValue,
        "title" to title
    )
}
