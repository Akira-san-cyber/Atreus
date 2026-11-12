package com.iistable.atreus.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.iistable.atreus.ai.BugHunterService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugHunterScreen(
    onStartDebate: (String) -> Unit
) {
    var targetInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🕵️‍♂️ White-Hat Consensus Hunter", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter a URL or Package. The Cortex and Limbic System will debate and find vulnerabilities.", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            label = { Text("Target URL or App Package") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                if (targetInput.isNotBlank()) {
                    val serviceIntent = Intent(context, BugHunterService::class.java).apply {
                        putExtra("TARGET", targetInput)
                    }
                    ContextCompat.startForegroundService(context, serviceIntent)
                    onStartDebate(targetInput) 
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start AI Debate & Hunt")
        }
    }
}
