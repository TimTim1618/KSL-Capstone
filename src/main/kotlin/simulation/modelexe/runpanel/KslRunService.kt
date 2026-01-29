package simulation.modelexe.runpanel

data class RunCallbacks(
    val onStatus: (String) -> Unit,
    val onProgress: (Int, Int) -> Unit,
    val onConsole: (String) -> Unit,
    val onError: (String) -> Unit
)

interface RunHandle {
    fun requestStop()
    fun isStopRequested(): Boolean
}

interface KslRunService {
    fun runAll(config: RunConfig, callbacks: RunCallbacks): RunHandle
    fun stepOnce(config: RunConfig, callbacks: RunCallbacks): RunHandle
}