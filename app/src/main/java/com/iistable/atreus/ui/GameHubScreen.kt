package com.iistable.atreus.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun GameHubScreen() {
    val context = LocalContext.current
    var games by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var aiTask by remember { mutableStateOf("Play, Start, Claim") }
    var manualPackage by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Scan for apps (Relaxed filter to find more games)
    fun scanGames() {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        games = installedApps
            .filter { 
                val launchIntent = pm.getLaunchIntentForPackage(it.packageName)
                // Show all user-installed apps, not just flagged games
                (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 && launchIntent != null
            }
            .map { appInfo -> Pair(appInfo.loadLabel(pm).toString(), appInfo.packageName) }
            .sortedBy { it.first }
    }

    LaunchedEffect(Unit) { scanGames() }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Game Manually") },
            text = { 
                Column {
                    Text("Enter the package name (e.g., com.tencent.ig for PUBG):")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = manualPackage, onValueChange = { manualPackage = it }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (manualPackage.isNotBlank()) {
                        try {
                            val appInfo = context.packageManager.getApplicationInfo(manualPackage.trim(), 0)
                            val name = context.packageManager.getApplicationLabel(appInfo).toString()
                            games = games + Pair(name, manualPackage.trim())
                            showAddDialog = false
                            manualPackage = ""
                        } catch (e: Exception) {
                            // Package not found
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("🎮 Game Hub (Auto-Play)", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { showAddDialog = true }) {
                Text("➕", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Text("Type what Atreus should click, then tap a game to launch.", style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = aiTask,
            onValueChange = { aiTask = it },
            label = { Text("AI Click Targets (comma separated)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        if (games.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No installed apps found. Tap ➕ to add manually.")
            }
        } else {
            LazyColumn {
                items(games) { (name, packageName) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            val prefs = context.getSharedPreferences("atreus_automation", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putString("ai_task", aiTask).apply()
                            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                            if (intent != null) context.startActivity(intent)
                        }
                    ) {
                        Text("$name\n$packageName", modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}
