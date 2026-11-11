package com.iistable.atreus.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VideoGenScreen(onGenerate: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🎬 AI Video Script Generator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Enter a topic (e.g., How C++ pointers work)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onGenerate(prompt) }, modifier = Modifier.fillMaxWidth()) {
            Text("Generate Video Script")
        }
    }
}
