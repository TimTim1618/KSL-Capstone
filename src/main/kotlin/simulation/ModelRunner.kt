package simulation

/**
 * Minimal runner that executes a SimulationModel.
 * Expand this later with callbacks, progress updates, threading, etc.
 */
class ModelRunner(private val model: SimulationModel) {

    fun run(input: SimulationInput) {
        model.run(input)
    }
}
