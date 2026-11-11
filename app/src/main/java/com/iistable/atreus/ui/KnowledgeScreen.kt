package com.iistable.atreus.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KnowledgeScreen() {
    val topics = listOf(
        "Hardware Security Modules (HSM) & TPM",
        "Buffer Overflow & Stack Exploits",
        "Reverse Engineering PE and ELF files",
        "ROP & ASLR/DEP Bypasses",
        "Kernel Driver Inspection",
        "FPGA Programming (Verilog/VHDL)"
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📚 Offline Knowledge Base", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(topics) { topic ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(topic, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
