package GUI

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import java.io.File
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun InputScreen() {
    val workFolder = "src/main/kotlin/work"
    // hold list of available model files in work folder
    val modelFiles = remember { mutableStateOf(listModelFiles(workFolder)) }

    // Scroll state for the entire input screen
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)  // make input area scrollable
            .padding(24.dp)
    ) {
        // make dropdown menu and dynamic input fields
        DynamicModelInputSelector(workFolder, modelFiles.value)
    }
}

// scans the work folder for .kt files
fun listModelFiles(folderPath: String): List<String> {
    val folder = File(folderPath)
    return if (folder.exists() && folder.isDirectory) {
        // only files that end with ".kt"
        folder.listFiles { file -> file.isFile && file.extension == "kt" }
            ?.map { it.nameWithoutExtension } ?: emptyList()
    } else emptyList()
}


// dynamic builder
@Composable
fun DynamicModelInputSelector(folderPath: String, modelNames: List<String>) {
    // defaulted to first model in work folder
    var selectedModelName by remember { mutableStateOf(modelNames.firstOrNull() ?: "") }
    // a map storing the controls/parameters for selected model
    var currentModelControls by remember { mutableStateOf(mapOf<String, Any>()) }

    // Update inputs when model selection changes
    LaunchedEffect(selectedModelName) {
        currentModelControls = loadModelControls(selectedModelName)
        // store globally for model and output
        ModelInputData.currentModelName = selectedModelName
        ModelInputData.parameters = currentModelControls
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Select Model File:", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        //dropdown menu
        var expanded by remember { mutableStateOf(false) }

        // model selection dropdown
        Box {
            Button(onClick = { expanded = true }) {
                Text(if (selectedModelName.isNotEmpty()) selectedModelName else "No model files found")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                modelNames.forEach { name ->
                    DropdownMenuItem(onClick = {
                        selectedModelName = name
                        expanded = false
                    }) {
                        Text(name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // display the input fields for selected model
        ModelInputPanel(currentModelControls, selectedModelName)
    }
}

// display editable text fields
@Composable
fun ModelInputPanel(controls: Map<String, Any>, modelName: String) {
    val paramStates = remember(controls) {
        controls.mapValues { mutableStateOf(it.value) }.toMutableMap()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Editing model: $modelName", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        // loop through each model and render a label and input box
        paramStates.forEach { (key, state) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$key: ", modifier = Modifier.width(150.dp))
                // text field for the parameter value
                TextField(
                    value = state.value.toString(),
                    onValueChange = { newVal ->
                        val originalType = controls[key]!!::class
                        state.value = when (originalType) {
                            Int::class -> newVal.toIntOrNull() ?: state.value
                            Double::class -> newVal.toDoubleOrNull() ?: state.value
                            String::class -> newVal
                            else -> newVal
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // save inputs back to global ModelInputData
        Button(onClick = {
            val updatedParams = paramStates.mapValues { it.value.value }
            ModelInputData.currentModelName = modelName
            ModelInputData.parameters = updatedParams
            println("User inputs saved for $modelName: $updatedParams")
        }) {
            Text("Apply Inputs")
        }
    }
}


// dynamically load a model class
fun loadModelControls(modelName: String): Map<String, Any> {
    return try {
        val clazz = Class.forName("work.$modelName").kotlin
        val instance = clazz.createInstance()
        val controlsMethod = clazz.memberFunctions.firstOrNull { it.name == "controls" }
        controlsMethod?.isAccessible = true
        controlsMethod?.call(instance) as? Map<String, Any> ?: emptyMap()
    } catch (e: Exception) {
        println("Failed to load controls for $modelName: ${e.message}")
        emptyMap()
    }
}
