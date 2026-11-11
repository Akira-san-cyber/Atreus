package com.iistable.atreus.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iistable.atreus.ai.HistoryItem
@Composable
fun HistoryScreen(onLoadChat: (HistoryItem) -> Unit, onDeleteChat: (Long) -> Unit, getHistory: () -> List<HistoryItem>) {
    var history by remember { mutableStateOf(getHistory()) }
    if (history.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No history.") } }
    else { LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(history) { item -> Card(onClick = { onLoadChat(item) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.title, style = MaterialTheme.typography.titleMedium); Text(item.date, style = MaterialTheme.typography.bodySmall) }; IconButton(onClick = { onDeleteChat(item.id); history = getHistory() }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) } } } } } }
}
