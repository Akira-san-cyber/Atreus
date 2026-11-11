package com.iistable.atreus.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CameraScreen(onScan: (String) -> Unit) {
    var result by remember { mutableStateOf("Point camera at QR code...") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("📷 Camera & QR Scanner", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Simulated Camera View here.", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.weight(1f))
        Button(onClick = { onScan("https://example.com/fake-qr") }) {
            Text("Scan Fake QR Code")
        }
    }
}
