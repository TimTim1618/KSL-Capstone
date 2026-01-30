package simulation.modelexe.runpanel

interface ReplicatedModel {
    fun runOneReplication(
        repIndex: Int,
        config: RunConfig,
        stopRequested: () -> Boolean,
        onConsole: (String) -> Unit
    )
}
