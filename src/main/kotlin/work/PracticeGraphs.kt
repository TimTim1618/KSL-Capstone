import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Practice Graphs") {
        App()
    }
}

@Composable
fun App() {
    var count by remember { mutableStateOf(0) }

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Count: $count")
            Button(onClick = { count++ }) {
                Text("Add 1")
            }
        }
    }
}
