package simulation.modelexe.runpanel

import kotlin.random.Random

class SimpleKslDemoModel : ReplicatedModel {

    override fun runOneReplication(
        repIndex: Int,
        config: RunConfig,
        stopRequested: () -> Boolean,
        onConsole: (String) -> Unit
    ) {

        val iterations = 5_000
        val chunks = 10
        val chunkSize = iterations / chunks

        var count = 0
        var chunk = 1

        onConsole(ConsoleUtil.tag("REP", "rep=$repIndex started"))

        while (count < iterations) {

            if (stopRequested()) {
                onConsole(ConsoleUtil.tag("REP", "rep=$repIndex stopped early"))
                return
            }

            val end = minOf(iterations, count + chunkSize)
            while (count < end) {
                Random.nextDouble()
                count++
            }

            onConsole(
                ConsoleUtil.tag(
                    "PROG",
                    "rep=$repIndex chunk $chunk/$chunks ($count/$iterations)"
                )
            )
            chunk++
        }

        val fakeStat = Random.nextDouble()

        onConsole(ConsoleUtil.tag("STAT", "rep=$repIndex result = $fakeStat"))
        onConsole(ConsoleUtil.tag("REP", "rep=$repIndex finished"))
    }
}
