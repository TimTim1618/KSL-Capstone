package simulation

interface SimulationModel {
    fun run(input: SimulationInput): SimulationOutput
}
