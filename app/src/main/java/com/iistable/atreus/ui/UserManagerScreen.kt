package com.iistable.atreus.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iistable.atreus.ai.SecurityManager
import com.iistable.atreus.ai.UserRole
import androidx.compose.ui.platform.LocalContext

@Composable
fun UserManagerScreen() {
    val context = LocalContext.current
    val security = remember { SecurityManager(context) }
    var currentRole by remember { mutableStateOf(if (security.isAdmin()) UserRole.ADMIN else UserRole.GUEST) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("👑 User Manager", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Current Role: ${currentRole.name}", style = MaterialTheme.typography.titleMedium)
        
        Spacer(Modifier.height(32.dp))
        
        Button(onClick = { 
            security.setRole(UserRole.ADMIN)
            currentRole = UserRole.ADMIN
        }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Switch to Admin")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { 
            security.setRole(UserRole.GUEST)
            currentRole = UserRole.GUEST
        }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Switch to Guest")
        }
    }
}
