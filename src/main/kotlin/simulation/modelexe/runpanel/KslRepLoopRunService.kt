package simulation.modelexe.runpanel

import java.util.concurrent.atomic.AtomicBoolean

class KslRepLoopRunService(
    private val buildModel: () -> ReplicatedModel
) : KslRunService {

    override fun runAll(config: RunConfig, callbacks: RunCallbacks): RunHandle {
        val stop = StopFlag()

        try {
            val header = ConsoleUtil.header("RUN START")
            callbacks.onConsole(header)
            callbacks.onConsole(
                ConsoleUtil.tag(
                    "RUN",
                    "Config: reps=${config.numReps}, warmUp=${config.warmUp}, repLength=${config.repLength}"
                )
            )

            callbacks.onStatus("Building model...")
            val model = buildModel()

            val total = config.numReps.coerceAtLeast(1)
            callbacks.onProgress(0, total)
            callbacks.onStatus("Running...")

            for (rep in 1..total) {

                if (stop.isStopRequested()) {
                    callbacks.onConsole(ConsoleUtil.tag("RUN", "Stop requested."))
                    callbacks.onStatus("Stopped")
                    return stop
                }

                model.runOneReplication(
                    repIndex = rep,
                    config = config,
                    stopRequested = { stop.isStopRequested() },
                    onConsole = callbacks.onConsole
                )

                callbacks.onProgress(rep, total)
            }

            callbacks.onConsole(ConsoleUtil.header("RUN COMPLETE"))
            callbacks.onStatus("Done")

        } catch (t: Throwable) {
            callbacks.onConsole(ConsoleUtil.tag("ERROR", t.message ?: "Unknown error"))
            callbacks.onStatus("Error")
            callbacks.onError(t.stackTraceToString())
        }

        return stop
    }

    override fun stepOnce(config: RunConfig, callbacks: RunCallbacks): RunHandle {
        callbacks.onConsole(ConsoleUtil.tag("RUN", "Step not implemented."))
        return StopFlag()
    }

    private class StopFlag : RunHandle {
        private val stop = AtomicBoolean(false)
        override fun requestStop() { stop.set(true) }
        override fun isStopRequested(): Boolean = stop.get()
    }
}
