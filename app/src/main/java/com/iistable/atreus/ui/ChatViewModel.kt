package com.iistable.atreus.ui
import android.content.Context
import android.net.Uri
import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.iistable.atreus.ai.*
import com.iistable.atreus.data.TimetableRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UiMessage(val role: String, val text: String)
data class ChatUiState(
    val messages: List<UiMessage> = emptyList(), 
    val draft: String = "", 
    val isGenerating: Boolean = false, 
    val engineStatus: String = "Ready", 
    val ready: Boolean = false, 
    val downloadProgress: Int = -1,
    val showUrlDialog: Boolean = false,
    val urlInput: String = "",
    val showUpdateDialog: Boolean = false,
    val newVersion: String = "",
    val isUpdating: Boolean = false,
    val updateProgress: Int = -1,
    val adminSessionActive: Boolean = false,
    val rulesCount: Int = 0,
    val pendingUpgradeProposal: String? = null
)

class ChatViewModel(private val context: Context, private val engine: LlmEngine, private val timetable: TimetableRepository) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state = _state.asStateFlow()
    private val ttsManager = TtsManager(context)
    private val security = SecurityManager(context)
    private val historyManager = HistoryManager(context)
    private var memory = AgentMemory()
    private var interactionCount = 0

    init { 
        startDownload()
        loadMemory()
        checkForUpdates()
        _state.update { it.copy(adminSessionActive = security.hasAdminAccess()) }
    }

    fun getHistoryList(): List<HistoryItem> = historyManager.getHistory()
    fun saveCurrentChat() {
        val jsonArr = JSONArray()
        for (msg in _state.value.messages) { val obj = JSONObject(); obj.put("role", msg.role); obj.put("text", msg.text); jsonArr.put(obj) }
        historyManager.saveSession(jsonArr.toString())
    }
    fun startNewChat() { saveCurrentChat(); _state.update { it.copy(messages = emptyList()) } }
    fun loadChat(historyItem: HistoryItem) {
        saveCurrentChat(); val msgs = mutableListOf<UiMessage>()
        try { val arr = JSONArray(historyItem.messagesJson); for (i in 0 until arr.length()) { val obj = arr.getJSONObject(i); msgs.add(UiMessage(obj.getString("role"), obj.getString("text"))) } } catch (e: Exception) {}
        _state.update { it.copy(messages = msgs) }
    }
    fun deleteChat(id: Long) { historyManager.deleteSession(id) }
    fun refreshAdminStatus() { _state.update { it.copy(adminSessionActive = security.hasAdminAccess()) } }

    private fun checkForUpdates() { viewModelScope.launch { val latest = UpdateManager.checkForUpdate(); if (latest != null) _state.update { it.copy(showUpdateDialog = true, newVersion = latest) } } }
    fun dismissUpdate() = _state.update { it.copy(showUpdateDialog = false) }
    fun triggerAutoInstall() {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateProgress = 0) }
            val apkUrl = "https://github.com/Akira-san-cyber/Atreus/releases/download/v${_state.value.newVersion}/app-debug.apk"
            AutoUpdater.downloadAndInstall(context, apkUrl) { progress -> _state.update { it.copy(updateProgress = progress) } }
            _state.update { it.copy(isUpdating = false, updateProgress = -1, showUpdateDialog = false) }
        }
    }

    private fun loadMemory() { viewModelScope.launch { memory = EvolutionEngine.loadMemory(context); _state.update { it.copy(rulesCount = memory.positiveRules.size + memory.negativeRules.size) } } }
    private fun saveMemory() { viewModelScope.launch { EvolutionEngine.saveMemory(context, memory) }; _state.update { it.copy(rulesCount = memory.positiveRules.size + memory.negativeRules.size) } }

    private fun startDownload() {
        viewModelScope.launch {
            val success = ModelDownloader.downloadIfNeeded(context, { progress -> _state.update { it.copy(downloadProgress = progress, engineStatus = "Downloading...") } }, { error -> _state.update { it.copy(engineStatus = "Download Failed: $error", showUrlDialog = true) } })
            if (success) { _state.update { it.copy(downloadProgress = -1, engineStatus = "Loading AI...") }; val result = engine.load(); _state.update { it.copy(ready = result.isSuccess, engineStatus = if (result.isSuccess) "Ready" else "Error") } }
        }
    }

    fun onDraftChange(v: String) { _state.update { it.copy(draft = v) } }
    fun readTextFile(uri: Uri) { /* Implementation stays */ }
    fun analyzeImage(uri: Uri) { /* Implementation stays */ }
    fun analyzeVideo(uri: Uri) { /* Implementation stays */ }

    fun send() {
        val text = _state.value.draft.trim(); if (text.isEmpty() || _state.value.isGenerating) return
        _state.update { it.copy(draft = "", isGenerating = true, engineStatus = "Thinking...", messages = it.messages + UiMessage("user", text)) }
        interactionCount++
        
        viewModelScope.launch {
            if (!_state.value.ready) { _state.update { it.copy(isGenerating = false, messages = it.messages + UiMessage("assistant", "⚠ Wait for Brain download.")) }; return@launch }
            
            if (text.startsWith("!bad ")) { memory.negativeRules.add(text.removePrefix("!bad ").trim()); saveMemory(); _state.update { it.copy(isGenerating = false, messages = it.messages + UiMessage("assistant", "🚫 Negative Rule Saved.")) }; return@launch }
            if (text.startsWith("!correct ")) { memory.positiveRules.add(text.removePrefix("!correct ").trim()); saveMemory(); _state.update { it.copy(isGenerating = false, messages = it.messages + UiMessage("assistant", "🧠 Positive Rule Saved.")) }; return@launch }

            try {
                val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                val kbContext = KnowledgeBase.getContext(text) ?: ""
                val rulesCtx = if (memory.positiveRules.isNotEmpty()) "RULES:\n${memory.positiveRules.joinToString("\n")}" else ""
                val negativeCtx = if (memory.negativeRules.isNotEmpty()) "AVOID:\n${memory.negativeRules.joinToString("\n")}" else ""
                val basePrompt = if (memory.customPrompt.isNotBlank()) memory.customPrompt else "You are Atreus."
                val systemPrompt = "$basePrompt\n$rulesCtx\n$negativeCtx\n$kbContext\nToday is $currentDate."

                _state.update { it.copy(engineStatus = "Generating...") }
                var answer = engine.chat(listOf(ChatMsg("system", systemPrompt), ChatMsg("user", text)))
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", answer)) }
                ttsManager.speak(answer)
            } catch (e: Exception) { _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "⚠ Error: ${e.message}")) } }
        }
    }

    fun submitManualUrl(url: String) { ModelDownloader.currentUrl = url; _state.update { it.copy(showUrlDialog = false, urlInput = "", engineStatus = "Retrying...") }; startDownload() }
    fun onUrlInputChange(v: String) = _state.update { it.copy(urlInput = v) }
    override fun onCleared() { super.onCleared(); saveCurrentChat(); ttsManager.shutdown() }
    
    class Factory(private val context: Context, private val engine: LlmEngine, private val timetable: TimetableRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel(context, engine, timetable) as T
    }
}
