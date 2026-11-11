package com.iistable.atreus.ui
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable
fun NotesScreen() { val context = LocalContext.current; val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE); var text by remember { mutableStateOf(prefs.getString("re", "") ?: "") }; Column(Modifier.fillMaxSize().padding(16.dp)) { OutlinedTextField(value = text, onValueChange = { text = it; prefs.edit().putString("re", it).apply() }, modifier = Modifier.fillMaxSize(), placeholder = { Text("Auto-saving notes...") }) } }
