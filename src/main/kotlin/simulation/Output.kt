package simulation

class SimulationOutput(
    private val avg: Double,
    val histogram: IntArray? = null 
) {
    fun average() = avg
}
