package com.iistable.atreus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@Composable
fun SelfUpgradeDialog(
    jsonProposal: String,
    onApprove: () -> Unit,
    onDecline: () -> Unit
) {
    var proposal by remember { mutableStateOf<JSONObject?>(null) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(jsonProposal) {
        try { proposal = JSONObject(jsonProposal) } catch (e: Exception) { errorMsg = "Failed to parse proposal." }
    }

    val p = proposal ?: return
    val goal = p.optString("goal", "Unknown goal")
    val risk = p.optString("risk", "unknown")
    val diff = p.optString("diff", "")
    val test = p.optString("how_to_test", "")

    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text("🧬 Self-Upgrade Proposal") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (errorMsg.isNotEmpty()) { Text(errorMsg, color = MaterialTheme.colorScheme.error); return@Column }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Risk: ", style = MaterialTheme.typography.labelMedium)
                    Text(risk.uppercase(), color = when(risk.lowercase()) { "low" -> Color(0xFF00E676); "medium" -> Color(0xFFFFEB3B); else -> Color(0xFFF44336) }, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("Goal:", style = MaterialTheme.typography.labelLarge)
                Text(goal, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Text("Code Changes:", style = MaterialTheme.typography.labelLarge)
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
                        diff.lines().forEach { line ->
                            val color = when { line.startsWith("+") -> Color(0xFF00E676); line.startsWith("-") -> Color(0xFFF44336); else -> Color(0xFFE8E8E8) }
                            Text(text = line, color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("How to test: $test", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = { Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)) { Text("Approve & Save") } },
        dismissButton = { TextButton(onClick = onDecline) { Text("Decline", color = Color(0xFFF44336)) } }
    )
}
