package work

data class ResourceModel(
    var numResources: Int = 3,
    var resourceName: String = "Server",
    var utilizationTarget: Double = 0.75
) {
    fun controls() = mapOf(
        "numResources" to numResources,
        "resourceName" to resourceName,
        "utilizationTarget" to utilizationTarget
    )
}
