package com.iistable.atreus.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("atreus_calendar", Context.MODE_PRIVATE)
    var task by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(prefs.getStringSet("tasks", emptySet())?.toList() ?: emptyList()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📅 Daily Activities", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = task,
                onValueChange = { task = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("New event...") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (task.isNotBlank()) {
                    val newTasks = tasks + task
                    tasks = newTasks
                    prefs.edit().putStringSet("tasks", newTasks.toSet()).apply()
                    task = ""
                }
            }) { Text("Add") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(tasks) { t ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(t, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
