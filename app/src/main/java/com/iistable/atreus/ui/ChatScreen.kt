package com.iistable.atreus.ui
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iistable.atreus.ai.SecurityManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val primaryColor = when (state.themeColor.lowercase()) {
        "red" -> Color(0xFFFF5252)
        "blue" -> Color(0xFF448AFF)
        "purple" -> Color(0xFFE040FB)
        "orange" -> Color(0xFFFFAB40)
        else -> Color(0xFF00E676) // Default Green
    }

    var currentScreen by remember { mutableStateOf("CHAT") }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<() -> Unit>({}) }
    var showAttachMenu by remember { mutableStateOf(false) }
    val security = remember { SecurityManager(context) }

    LaunchedEffect(state.messages.size, state.isGenerating) { 
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex) 
    }

    fun requireAdmin(action: () -> Unit) { 
        if (state.adminSessionActive) action() else { pendingAction = action; showAuthDialog = true } 
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> if (uri != null) vm.analyzeImage(uri) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> if (bitmap != null) vm.onDraftChange("📷 [Photo captured]") }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> if (uri != null) vm.analyzeVideo(uri) }
    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> if (uri != null) vm.readTextFile(uri) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) cameraLauncher.launch(null) }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res -> if (res.resultCode == android.app.Activity.RESULT_OK) { val txt = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull(); if (txt != null) vm.onDraftChange(txt) } }

    // DIALOGS
    if (showNewChatDialog) {
        AlertDialog(onDismissRequest = { showNewChatDialog = false }, title = { Text("Start New Chat?") }, text = { Text("Save current chat to History?") },
            confirmButton = { TextButton(onClick = { showNewChatDialog = false; vm.startNewChat() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showNewChatDialog = false }) { Text("Cancel") } })
    }
    if (showAttachMenu) {
        AlertDialog(onDismissRequest = { showAttachMenu = false }, title = { Text("Attach File") }, text = { Text("What to upload?") },
            confirmButton = { TextButton(onClick = { showAttachMenu = false; requireAdmin { fileLauncher.launch(arrayOf("image/*")) } }) { Text("Image") } },
            dismissButton = { Row { TextButton(onClick = { showAttachMenu = false; requireAdmin { videoLauncher.launch(arrayOf("video/*")) } }) { Text("Video") }; TextButton(onClick = { showAttachMenu = false; requireAdmin { docLauncher.launch(arrayOf("text/*")) } }) { Text("Doc") } } })
    }
    if (showAuthDialog) {
        AlertDialog(onDismissRequest = { showAuthDialog = false }, title = { Text("🔒 Admin Required") }, text = { Text("Verify identity.") },
            confirmButton = { TextButton(onClick = {
                val prompt = BiometricPrompt(context as FragmentActivity, ContextCompat.getMainExecutor(context), object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        showAuthDialog = false
                        val dur = android.app.AlertDialog.Builder(context).setTitle("Duration").setItems(arrayOf("6 Hrs", "12 Hrs", "24 Hrs", "48 Hrs")) { _, i -> security.grantAdminAccess(listOf(6, 12, 24, 48)[i]); vm.refreshAdminStatus(); pendingAction() }.show()
                    }
                })
                prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Atreus").setNegativeButtonText("Cancel").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG).build())
            }) { Text("Auth") } }, dismissButton = { TextButton(onClick = { showAuthDialog = false }) { Text("Cancel") } })
    }
    if (state.showUpdateDialog) { AlertDialog(onDismissRequest = { vm.dismissUpdate() }, title = { Text("Update Available") }, text = { Text("Version ${state.newVersion}") }, confirmButton = { TextButton(onClick = { vm.triggerAutoInstall() }) { Text("Auto-Install") } }, dismissButton = { TextButton(onClick = { vm.dismissUpdate() }) { Text("Later") } }) }
    if (state.showUrlDialog) { AlertDialog(onDismissRequest = { }, title = { Text("Download Failed") }, text = { Column { Text("Paste model URL:"); OutlinedTextField(value = state.urlInput, onValueChange = vm::onUrlInputChange) } }, confirmButton = { TextButton(onClick = { vm.submitManualUrl(state.urlInput) }) { Text("Retry") } }) }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            Text(if (state.adminSessionActive) "Atreus (Admin)" else "Atreus (Guest)", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge); Divider()
            NavigationDrawerItem(label = { Text("📚 History") }, selected = currentScreen == "HISTORY", onClick = { currentScreen = "HISTORY"; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("💬 Chat") }, selected = currentScreen == "CHAT", onClick = { currentScreen = "CHAT"; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("🎮 Game Hub") }, selected = currentScreen == "PLAYGROUND", onClick = { requireAdmin { currentScreen = "PLAYGROUND"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("💻 Vibe Code Editor") }, selected = currentScreen == "CODE", onClick = { requireAdmin { currentScreen = "CODE"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("⚙️ Background Tasks") }, selected = currentScreen == "TASKS", onClick = { requireAdmin { currentScreen = "TASKS"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("🤖 Auto-Tester") }, selected = currentScreen == "AUTOTESTER", onClick = { requireAdmin { currentScreen = "AUTOTESTER"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            
            NavigationDrawerItem(label = { Text("🕵️‍♂️ Bug Hunter") }, selected = currentScreen == "BUG_HUNTER", onClick = { requireAdmin { currentScreen = "BUG_HUNTER"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("🛡️ Automated RE Tool") }, selected = currentScreen == "RE_NOTES", onClick = { requireAdmin { currentScreen = "RE_NOTES"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("📷 Live QR Scanner") }, selected = currentScreen == "QR", onClick = { requireAdmin { currentScreen = "QR"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
            NavigationDrawerItem(label = { Text("⌨️ Terminal") }, selected = currentScreen == "TERMINAL", onClick = { requireAdmin { currentScreen = "TERMINAL"; scope.launch { drawerState.close() } } }, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(if (currentScreen == "CHAT") "Atreus" else currentScreen) }, navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") } }, actions = { if (currentScreen == "CHAT") IconButton(onClick = { showNewChatDialog = true }) { Icon(Icons.AutoMirrored.Filled.Chat, "New Chat") } }) },
            bottomBar = {
                if (currentScreen == "CHAT") {
                    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showAttachMenu = true }, modifier = Modifier.padding(bottom = 8.dp)) { Icon(Icons.Default.Add, "Attach", tint = primaryColor) }
                            IconButton(onClick = { requireAdmin { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA) } }, modifier = Modifier.padding(bottom = 8.dp)) { Icon(Icons.Default.PhotoCamera, "Camera", tint = primaryColor) }
                            IconButton(onClick = { val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) }; voiceLauncher.launch(intent) }, modifier = Modifier.padding(bottom = 8.dp)) { Icon(Icons.Default.Mic, "Voice", tint = primaryColor) }
                            OutlinedTextField(value = state.draft, onValueChange = vm::onDraftChange, modifier = Modifier.weight(1f), placeholder = { Text("Ask Atreus...") }, maxLines = 4, shape = RoundedCornerShape(24.dp))
                            Spacer(Modifier.width(8.dp))
                            FloatingActionButton(onClick = vm::send, containerColor = primaryColor, modifier = Modifier.size(48.dp).padding(bottom = 4.dp)) { Icon(Icons.Default.Send, "Send") }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (currentScreen) {
                    "HISTORY" -> HistoryScreen(onLoadChat = { item -> vm.loadChat(item); currentScreen = "CHAT" }, onDeleteChat = { id -> vm.deleteChat(id) }, getHistory = { vm.getHistoryList() })
                    "CHAT" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Surface(tonalElevation = 2.dp) { Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) { Text(if (state.adminSessionActive) "v82.0 · Admin Active" else "v82.0 · Guest Mode", style = MaterialTheme.typography.labelMedium); Text(state.engineStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); if (state.downloadProgress in 0..100) { Text("Downloading: ${state.downloadProgress}%"); LinearProgressIndicator(progress = { state.downloadProgress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) } } }
                            LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.messages) { m -> Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.role == "user") Arrangement.End else Arrangement.Start) { Surface(color = if (m.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.widthIn(max = 320.dp)) { Text(m.text, modifier = Modifier.padding(12.dp)) } } }
                                if (state.isGenerating) item { Text("Thinking...") }
                            }
                        }
                    }
                    "PLAYGROUND" -> GameHubScreen()
                    "CODE" -> CodeEditorScreen()
                    "TASKS" -> TaskDashboardScreen()
                    "AUTOTESTER" -> AutoTesterScreen()
                    "BUG_HUNTER" -> BugHunterScreen(onStartDebate = { target -> vm.executeBugBountyDebate(target); currentScreen = "CHAT" })
                    "RE_NOTES" -> ReverseEngineeringScreen(onAnalyzeTarget = { data -> vm.analyzeTarget(data); currentScreen = "CHAT" })
                    "TERMINAL" -> TerminalScreen()
                    "QR" -> QrScannerScreen(onQrScanned = { code -> vm.onDraftChange(code); currentScreen = "CHAT" })
                }
            }
        }
    }
}
