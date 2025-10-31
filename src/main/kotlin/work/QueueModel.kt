package work

data class QueueModel(
    var arrivalRate: Double = 5.0,
    var serviceRate: Double = 2.0,
    var maxQueueLength: Int = 10,
    var queueName: String = "Queue1"
) {
    fun controls() = mapOf(
        "arrivalRate" to arrivalRate,
        "serviceRate" to serviceRate,
        "maxQueueLength" to maxQueueLength,
        "queueName" to queueName
    )
}
