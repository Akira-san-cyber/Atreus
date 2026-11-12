package com.iistable.atreus.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReverseEngineeringScreen(onAnalyzeTarget: (String) -> Unit) {
    var targetInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Launcher to pick an APK file from storage
    val apkLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                val archiveInfo = context.packageManager.getPackageArchiveInfo(uri.path!!, 0)
                if (archiveInfo != null) {
                    val pkgName = archiveInfo.packageName
                    val permissions = archiveInfo.requestedPermissions?.joinToString(", ") ?: "None"
                    val activities = archiveInfo.activities?.joinToString(", ") { it.name } ?: "None"
                    
                    val data = "APK Target:\nPackage: $pkgName\nPermissions: $permissions\nActivities: $activities"
                    onAnalyzeTarget(data)
                }
            } catch (e: Exception) {
                onAnalyzeTarget("Failed to analyze APK: ${e.message}")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🛡️ Automated RE Analyzer", style = MaterialTheme.typography.headlineSmall)
        Text("Enter a website URL or select an APK to find vulnerabilities.", style = MaterialTheme.typography.bodySmall)
        
        Spacer(Modifier.height(16.dp))
        
        // Website Analysis Input
        OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            label = { Text("Website URL (e.g., example.com)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(8.dp))
        
        Button(
            onClick = { 
                if (targetInput.isNotBlank()) {
                    onAnalyzeTarget("Website Target: $targetInput") 
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Website")
        }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(24.dp))
        
        // APK Analysis Button
        Text("Analyze Mobile Apps (APK):", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { apkLauncher.launch(arrayOf("application/vnd.android.package-archive")) }, modifier = Modifier.fillMaxWidth()) {
            Text("📂 Select APK File")
        }
    }
}
