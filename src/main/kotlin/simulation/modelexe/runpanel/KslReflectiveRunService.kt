package simulation.modelexe.runpanel

import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean

/**
 * You MUST implement buildModel() to return your model instance.
 */
class KslReflectiveRunService(
    private val buildModel: () -> Any
) : KslRunService {

    override fun runAll(config: RunConfig, callbacks: RunCallbacks): RunHandle {
        val stop = StopFlag()

        callbacks.onStatus("Building model…")
        val model = buildModel()

        try {
            callbacks.onStatus("Configuring experiment…")
            val experiment = createExperimentOrNull()
            if (experiment != null) {
                setIfExists(experiment, "numberOfReplications", config.numReps)
                setIfExists(experiment, "numReps", config.numReps)

                setIfExists(experiment, "warmUpPeriod", config.warmUp)
                setIfExists(experiment, "warmUp", config.warmUp)

                if (!config.finiteHorizon && config.repLength != null) {
                    setIfExists(experiment, "lengthOfReplication", config.repLength!!)
                    setIfExists(experiment, "replicationLength", config.repLength!!)
                    setIfExists(experiment, "runLength", config.repLength!!)
                }

                if (config.outputDir.isNotBlank()) {
                    setIfExists(experiment, "outputDirectory", config.outputDir)
                    setIfExists(experiment, "outputDir", config.outputDir)
                }

                setIfExists(experiment, "autoCSV", config.autoCsv)
                setIfExists(experiment, "autoCsv", config.autoCsv)
            }

            callbacks.onStatus("Running…")
            callbacks.onConsole("Run started. reps=${config.numReps}, finiteHorizon=${config.finiteHorizon}")

            val ran = tryRunWithExecutive(model, experiment, stop)
                    || tryRunDirectlyOnModel(model, stop)

            if (!ran) {
                callbacks.onError(
                    "Could not find a compatible KSL run method.\n" +
                            "Next step: paste your Chapter 5 runner snippet and I’ll wire it with typed KSL calls."
                )
                callbacks.onStatus("Error")
                return stop
            }

            if (stop.isStopRequested()) {
                callbacks.onStatus("Stopped")
                callbacks.onConsole("Stop requested.")
            } else {
                callbacks.onStatus("Done")
                callbacks.onConsole("Run completed.")
                callbacks.onProgress(config.numReps, config.numReps) // coarse completion
            }
        } catch (t: Throwable) {
            callbacks.onError(t.stackTraceToString())
            callbacks.onStatus("Error")
        }

        return stop
    }

    override fun stepOnce(config: RunConfig, callbacks: RunCallbacks): RunHandle {
        callbacks.onStatus("Step not implemented yet")
        callbacks.onConsole("Step is a future enhancement (needs a KSL single-step hook).")
        return StopFlag()
    }

    // ---------------- internals ----------------

    private fun createExperimentOrNull(): Any? {
        val candidates = listOf(
            "ksl.simulation.Experiment",
            "ksl.simulation.experiment.Experiment",
            "ksl.utilities.experiment.Experiment"
        )
        for (cn in candidates) {
            try {
                val c = Class.forName(cn)
                return try {
                    c.getDeclaredConstructor().newInstance()
                } catch (_: NoSuchMethodException) {
                    c.getDeclaredConstructor(String::class.java).newInstance("GUI Experiment")
                }
            } catch (_: Throwable) {
                // continue
            }
        }
        return null
    }

    private fun tryRunWithExecutive(model: Any, experiment: Any?, stop: StopFlag): Boolean {
        val execCandidates = listOf(
            "ksl.simulation.ModelExecutive",
            "ksl.simulation.execution.ModelExecutive",
            "ksl.simulation.executive.ModelExecutive"
        )

        for (cn in execCandidates) {
            try {
                val execClass = Class.forName(cn)

                val exec = try {
                    if (experiment != null) {
                        execClass.constructors.firstOrNull { it.parameterTypes.size == 2 }
                            ?.newInstance(model, experiment)
                    } else null
                } catch (_: Throwable) {
                    null
                }

                val exec2 = exec ?: try {
                    execClass.constructors.firstOrNull { it.parameterTypes.size == 1 }
                        ?.newInstance(model)
                } catch (_: Throwable) {
                    null
                }

                val executive = exec2 ?: continue

                if (stop.isStopRequested()) {
                    invokeIfExists(executive, "stop")
                    invokeIfExists(executive, "requestStop")
                    return true
                }

                val ok = invokeIfExists(executive, "execute")
                        || invokeIfExists(executive, "run")
                        || invokeIfExists(executive, "start")

                if (ok) return true
            } catch (_: Throwable) {
                // continue
            }
        }
        return false
    }

    private fun tryRunDirectlyOnModel(model: Any, stop: StopFlag): Boolean {
        if (stop.isStopRequested()) return true
        return invokeIfExists(model, "run")
                || invokeIfExists(model, "simulate")
                || invokeIfExists(model, "execute")
    }

    private fun setIfExists(target: Any, propertyOrSetter: String, value: Any): Boolean {
        val setterName = "set" + propertyOrSetter.replaceFirstChar { it.uppercase() }

        try {
            val m = findMethod(target.javaClass, setterName, value.javaClass)
                ?: findCompatibleSetter(target.javaClass, setterName, value)
            if (m != null) {
                m.isAccessible = true
                m.invoke(target, value)
                return true
            }
        } catch (_: Throwable) {
        }

        try {
            val f = target.javaClass.declaredFields.firstOrNull { it.name == propertyOrSetter }
            if (f != null) {
                f.isAccessible = true
                f.set(target, value)
                return true
            }
        } catch (_: Throwable) {
        }

        return false
    }

    private fun invokeIfExists(target: Any, name: String): Boolean {
        return try {
            val m = findMethodNoArgs(target.javaClass, name) ?: return false
            m.isAccessible = true
            m.invoke(target)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun findMethodNoArgs(c: Class<*>, name: String): Method? =
        c.methods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }
            ?: c.declaredMethods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }

    private fun findMethod(c: Class<*>, name: String, arg: Class<*>): Method? =
        c.methods.firstOrNull {
            it.name == name && it.parameterTypes.size == 1 && it.parameterTypes[0].isAssignableFrom(arg)
        } ?: c.declaredMethods.firstOrNull {
            it.name == name && it.parameterTypes.size == 1 && it.parameterTypes[0].isAssignableFrom(arg)
        }

    private fun findCompatibleSetter(c: Class<*>, name: String, value: Any): Method? {
        val candidates = (c.methods + c.declaredMethods).filter { it.name == name && it.parameterTypes.size == 1 }
        return candidates.firstOrNull { m ->
            val pt = m.parameterTypes[0]
            pt.isAssignableFrom(value.javaClass)
                    || (pt == Int::class.javaPrimitiveType && value is Int)
                    || (pt == Double::class.javaPrimitiveType && value is Double)
                    || (pt == Boolean::class.javaPrimitiveType && value is Boolean)
                    || (pt == String::class.java && value is String)
        }
    }

    private class StopFlag : RunHandle {
        private val stop = AtomicBoolean(false)
        override fun requestStop() {
            stop.set(true)
        }
        override fun isStopRequested(): Boolean = stop.get()
    }
}
