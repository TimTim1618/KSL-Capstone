package simulation.modelexe.runpanel

data class RunConfig(
    var numReps: Int = 20,
    var repLength: Double? = null,
    var warmUp: Double = 0.0,
    var autoCsv: Boolean = false,
    var outputDir: String = "",
    var finiteHorizon: Boolean = true
)
