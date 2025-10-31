package GUI

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.Window

// add when finished
import GUI.InputScreen
import GUI.ModelScreen
import GUI.OutputScreen




fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "KSL GUI Launcher") {
        var screen by remember { mutableStateOf("Input") }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Top row of buttons to switch screens
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { screen = "Input" }) { Text("Input") }
                Button(onClick = { screen = "Model" }) { Text("Model") }
                Button(onClick = { screen = "Output" }) { Text("Output") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the selected screen
            when (screen) {
                "Input" -> InputScreen()
                "Model" -> ModelScreen()
                "Output" -> OutputScreen()
            }
        }
    }
}

// --- Placeholder screens ---
// Replace these with actual composables from Input.kt, Model.kt, Output.kt

@Composable
fun ModelScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Model Screen Placeholder", style = MaterialTheme.typography.h5)
    }
}

@Composable
fun OutputScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Output Screen Placeholder", style = MaterialTheme.typography.h5)
    }
}
