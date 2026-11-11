package com.iistable.atreus.ui
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable
fun WindowsModule(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Atreus Windows Core", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = {
            val intent = context.packageManager.getLaunchIntentForPackage("com.winlator")
            if (intent != null) context.startActivity(intent)
            else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/brunodev85/winlator/releases")))
        }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("🚀 Launch Winlator") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("Back") }
    }
}
