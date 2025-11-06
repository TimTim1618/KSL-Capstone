package simulation

// dynamically load student .jar files

import java.io.File
import java.net.URLClassLoader
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.ExitCode

class ModelRunner {

    // future stuff - upload .jar files
    fun loadAndRunModel(jarPath: String, className: String, input: SimulationInput): SimulationOutput {
        val jarFile = File(jarPath)
        if (!jarFile.exists()) throw IllegalArgumentException("JAR file not found: $jarPath")

        val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), this::class.java.classLoader)
        val modelClass = classLoader.loadClass(className)
        val instance = modelClass.getDeclaredConstructor().newInstance() as SimulationModel

        return instance.run(input)
    }
    fun loadAndRunKotlinFile(ktFilePath: String, className: String, input: SimulationInput): SimulationOutput {
        val ktFile = File(ktFilePath)
        if (!ktFile.exists()) throw IllegalArgumentException("Kotlin file not found: $ktFilePath")

        // Temporary folder for compiled classes
        val outputDir = File("temp_classes")
        if (!outputDir.exists()) outputDir.mkdir()

        // Compile the Kotlin file using the Kotlin compiler CLI
        val process = ProcessBuilder(
            "kotlinc", ktFile.absolutePath,
            "-d", outputDir.absolutePath
        ).inheritIO().start()

        val exitCode = process.waitFor()
        if (exitCode != 0) throw RuntimeException("Kotlin compilation failed for $ktFilePath")

        // Load the compiled class
        val classLoader = URLClassLoader(arrayOf(outputDir.toURI().toURL()), this::class.java.classLoader)
        val modelClass = classLoader.loadClass(className)
        val instance = modelClass.getDeclaredConstructor().newInstance() as SimulationModel

        return instance.run(input)
    }

}

