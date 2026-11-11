package com.iistable.atreus.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(val name: String, val packageName: String, val icon: Drawable)

@Composable
fun GameHubScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            apps = getInstalledApps(context)
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🎮 Atreus Game Launcher", style = MaterialTheme.typography.headlineSmall)
        Text("Launch any game. Atreus will auto-tap for you.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(apps) { app ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(app.name, style = MaterialTheme.typography.titleMedium)
                            }
                            Button(onClick = {
                                // Launch the game
                                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                if (intent != null) context.startActivity(intent)
                            }) {
                                Text("Play")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    
    val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
    val list = mutableListOf<InstalledApp>()
    
    for (info in resolvedInfos) {
        val packageName = info.activityInfo.packageName
        // Exclude system apps to make the list clean
        if ((info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
            val appName = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)
            list.add(InstalledApp(appName, packageName, icon))
        }
    }
    return list.sortedBy { it.name }
}
