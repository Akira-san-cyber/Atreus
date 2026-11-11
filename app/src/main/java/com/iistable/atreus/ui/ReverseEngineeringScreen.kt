package com.iistable.atreus.ui
import android.content.pm.ApplicationInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable
fun ReverseEngineeringScreen(onAnalyzeApk: (String) -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(Unit) { val pm = context.packageManager; apps = pm.getInstalledPackages(0).mapNotNull { pkg -> val ai = pkg.applicationInfo ?: return@mapNotNull null; if ((ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0) Pair(ai.loadLabel(pm).toString(), pkg.packageName) else null }.sortedBy { it.first } }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { val info = context.packageManager.getPackageArchiveInfo(it.path!!, 0); info?.let { i -> onAnalyzeApk("Package: ${i.packageName}\nPerms: ${i.requestedPermissions?.joinToString()}") } } }
    Column(Modifier.fillMaxSize().padding(16.dp)) { Text("⚙️ Automated RE Analyzer", style = MaterialTheme.typography.headlineSmall); Button(onClick = { launcher.launch(arrayOf("application/vnd.android.package-archive")) }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("📂 Select APK") }; LazyColumn { items(apps) { (n, p) -> Card(onClick = { onAnalyzeApk("Package: $p") }, modifier = Modifier.fillMaxWidth().padding(4.dp)) { Text("$n\n$p", Modifier.padding(12.dp)) } } } }
}
