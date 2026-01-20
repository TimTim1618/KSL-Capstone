
package simulation

/**
 * Contract for anything that can be executed by ModelRunner.
 * Implement this in your actual simulation model later.
 */
interface SimulationModel {
    fun run(input: SimulationInput)
}
