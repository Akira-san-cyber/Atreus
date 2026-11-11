package com.iistable.atreus.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
@Composable
fun TerminalScreen() {
    var cmd by remember { mutableStateOf("") }; var out by remember { mutableStateOf(listOf("Shell")) }; val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Color.Black).padding(8.dp)) {
        LazyColumn(Modifier.weight(1f)) { items(out) { Text(it, color = Color.Green, fontFamily = FontFamily.Monospace) } }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(value = cmd, onValueChange = { cmd = it }, modifier = Modifier.weight(1f)); Button(onClick = { val c = cmd; cmd = ""; out = out + "❯ $c"; scope.launch { out = out + exec(c) } }) { Text("Run") } }
    }
}
suspend fun exec(c: String): String = withContext(Dispatchers.IO) { try { val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", c)); val r = BufferedReader(InputStreamReader(p.inputStream)); val s = StringBuilder(); var l: String?; while (r.readLine().also { l = it } != null) s.append(l).append("\n"); if (s.isEmpty()) "Done" else s.toString() } catch (e: Exception) { "Err: ${e.message}" } }
