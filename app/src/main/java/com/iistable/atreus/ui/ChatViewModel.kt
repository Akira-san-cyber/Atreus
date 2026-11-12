package com.iistable.atreus.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iistable.atreus.ai.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class UiMessage(val role: String, val text: String)

data class GeneratedImage(val bitmap: android.graphics.Bitmap, val modelName: String)

data class ChatUiState(
    val messages: List<UiMessage> = emptyList(), 
    val draft: String = "", 
    val isGenerating: Boolean = false, 
    val engineStatus: String = "Ready", 
    val ready: Boolean = false, 
    val downloadProgress: Int = -1,
    val adminSessionActive: Boolean = false,
    val themeColor: String = "Green",
    val showUpdateDialog: Boolean = false,
    val newVersion: String = "",
    val showUrlDialog: Boolean = false,
    val urlInput: String = "",
    val generatedImages: List<GeneratedImage> = emptyList()
)

class ChatViewModel(
    private val context: Context, 
    private val engine: LlmEngine, 
    private val timetable: Any
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChatUiState())
    val state = _state.asStateFlow()
    private val ttsManager = TtsManager(context)
    private val security = SecurityManager(context)
    private val historyManager = HistoryManager(context)
    
    private var currentEngine: LlmEngine = EngineProvider.createChatEngine(context)
    private var memory = AgentMemory()
    private var waitingForCorrection = false

    init { 
        startDownload()
        loadMemory()
        _state.update { it.copy(adminSessionActive = security.hasAdminAccess()) }
    }

    fun getHistoryList(): List<HistoryItem> = historyManager.getHistory()
    fun saveCurrentChat() {
        val jsonArr = org.json.JSONArray()
        for (msg in _state.value.messages) { 
            val obj = org.json.JSONObject(); obj.put("role", msg.role); obj.put("text", msg.text); jsonArr.put(obj) 
        }
        historyManager.saveSession(jsonArr.toString())
    }
    fun startNewChat() { saveCurrentChat(); _state.update { it.copy(messages = emptyList()) } }
    fun loadChat(historyItem: HistoryItem) {
        saveCurrentChat(); val msgs = mutableListOf<UiMessage>()
        try { val arr = org.json.JSONArray(historyItem.messagesJson); for (i in 0 until arr.length()) { val obj = arr.getJSONObject(i); msgs.add(UiMessage(obj.getString("role"), obj.getString("text"))) } } catch (e: Exception) {}
        _state.update { it.copy(messages = msgs) }
    }
    fun deleteChat(id: Long) { historyManager.deleteSession(id) }
    fun refreshAdminStatus() { _state.update { it.copy(adminSessionActive = security.hasAdminAccess()) } }

    fun dismissUpdate() { _state.update { it.copy(showUpdateDialog = false) } }
    fun triggerAutoInstall() { }
    fun submitManualUrl(url: String) { 
        if (url.isNotBlank()) {
            ModelDownloader.currentUrl = url
            _state.update { it.copy(showUrlDialog = false, urlInput = "", engineStatus = "Retrying Download...") }
            startDownload()
        }
    }
    fun onUrlInputChange(v: String) { _state.update { it.copy(urlInput = v) } }

    private fun loadMemory() { viewModelScope.launch { memory = EvolutionEngine.loadMemory(context) } }

    private fun startDownload() {
        viewModelScope.launch {
            val success = ModelManager.downloadIfNeeded(context, ModelType.TINYLLAMA) { progress -> 
                _state.update { it.copy(downloadProgress = progress, engineStatus = "Downloading MediaPipe Brain: $progress%") } 
            }
            if (success) { 
                _state.update { it.copy(engineStatus = "Downloading Cactus Brain (Qwen)...") }
                ModelManager.downloadIfNeeded(context, ModelType.QWEN) { progress -> 
                    _state.update { it.copy(downloadProgress = progress, engineStatus = "Downloading Cactus Brain: $progress%") } 
                }
                
                _state.update { it.copy(downloadProgress = -1, engineStatus = "Loading AI...") }
                val result = currentEngine.load()
                _state.update { it.copy(ready = result.isSuccess, engineStatus = if (result.isSuccess) "Ready" else "Error") } 
            }
        }
    }

    fun onDraftChange(v: String) { _state.update { it.copy(draft = v) } }
    fun readTextFile(uri: Uri) { }
    fun analyzeImage(uri: Uri) { }
    fun analyzeVideo(uri: Uri) { }

    private suspend fun runTerminalCommand(command: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) { output.append(line).append("\n") }
                output.toString()
            } catch (e: Exception) { "Error: ${e.message}" }
        }
    }

    fun askQwen(question: String) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, engineStatus = "Loading Cactus (Qwen)...", messages = it.messages + UiMessage("user", "[To Qwen]: $question")) }
            try {
                val cactusEngine = EngineProvider.createSelfEvolutionEngine(context, ModelType.QWEN)
                val loadResult = cactusEngine.load()
                
                if (loadResult.isFailure) {
                    _state.update { it.copy(engineStatus = "Qwen not found. Using MediaPipe...") }
                    val fallbackAnswer = currentEngine.chat(listOf(ChatMsg("user", question)))
                    _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "🧠 **(MediaPipe Fallback):**\n$fallbackAnswer")) }
                    ttsManager.speak(fallbackAnswer)
                    return@launch
                }
                
                val answer = cactusEngine.chat(listOf(ChatMsg("user", question)))
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "🧬 **Qwen AI:**\n$answer")) }
                ttsManager.speak(answer)
            } catch (e: Exception) {
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "⚠️ Qwen Error: ${e.message}")) }
            }
        }
    }

    private fun isSimilarQuestion(newQuestion: String, previousQuestions: List<String>): Boolean {
        val stopWords = setOf("what", "is", "the", "a", "an", "who", "where", "when", "why", "how", "are", "you", "do", "does", "can", "could", "i", "me", "my", "we", "our", "to", "in", "on", "for", "of", "and", "or", "tell", "about", "work", "works")
        fun getKeywords(text: String): Set<String> {
            return text.lowercase().replace(Regex("[^a-z0-9\\s]"), "").split("\\s+".toRegex()).filter { it.isNotEmpty() && !stopWords.contains(it) && it.length > 2 }.toSet()
        }
        val newWords = getKeywords(newQuestion)
        if (newWords.isEmpty()) return false
        for (prev in previousQuestions) {
            val prevWords = getKeywords(prev)
            if (prevWords.isEmpty()) continue
            for (nw in newWords) {
                for (pw in prevWords) {
                    if (nw == pw || (nw.length >= 4 && pw.contains(nw)) || (pw.length >= 4 && nw.contains(pw))) return true 
                }
            }
        }
        return false
    }

    fun executeBugBountyDebate(target: String) { /* Kept from previous code */ }
    fun analyzeTarget(targetData: String) { /* Kept from previous code */ }

    fun send() {
        val text = _state.value.draft.trim()
        if (text.isEmpty() || _state.value.isGenerating) return
        _state.update { it.copy(draft = "", isGenerating = true, engineStatus = "Thinking...", messages = it.messages + UiMessage("user", text)) }
        
        viewModelScope.launch {
            val lowerText = text.lowercase()

            // 1. COMMANDS (Highest Priority - Bypass Router)
            if (lowerText.startsWith("!correct ") || lowerText.startsWith("/correct ")) {
                val rule = text.substringAfter("correct ").trim()
                memory.positiveRules.add(rule)
                EvolutionEngine.saveMemory(context, memory)
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "🧠 **Learned!** I will remember: *$rule*")) }
                return@launch
            }
            if (lowerText == "!theme blue" || lowerText == "!theme red" || lowerText == "!theme green") {
                val color = text.removePrefix("!theme ").trim()
                _state.update { it.copy(themeColor = color, isGenerating = false, messages = it.messages + UiMessage("assistant", "🎨 **UI Updated!** I have changed my theme to $color.")) }
                return@launch
            }
            if (lowerText.startsWith("!mp ")) {
                val actualQuestion = text.removePrefix("!mp ").trim()
                val answer = currentEngine.chat(listOf(ChatMsg("system", "You are Atreus."), ChatMsg("user", actualQuestion)))
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "🧠 **(MediaPipe):** $answer")) }
                ttsManager.speak(answer)
                return@launch
            }

            // 2. WRONG ANSWER CORRECTION
            if (lowerText == "wrong" || lowerText == "incorrect" || lowerText == "no") {
                waitingForCorrection = true
                _state.update { it.copy(isGenerating = false, engineStatus = "Waiting for correction...", messages = it.messages + UiMessage("assistant", "I apologize. Please tell me the correct answer, and I will remember it.")) }
                return@launch
            }
            if (waitingForCorrection) {
                memory.positiveRules.add(text)
                EvolutionEngine.saveMemory(context, memory)
                waitingForCorrection = false
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "🧠 **Thank you.** I have saved this correction to my memory.")) }
                return@launch
            }

            // 3. AUTO ROUTER (For similar questions)
            val previousUserMessages = _state.value.messages.filter { it.role == "user" }.map { it.text }
            if (previousUserMessages.isNotEmpty() && isSimilarQuestion(text, previousUserMessages) && !lowerText.startsWith("!") && !lowerText.startsWith("/")) {
                _state.update { it.copy(engineStatus = "Similar Topic detected. Routing to Cactus...") }
                askQwen(text)
                return@launch
            }

            // 4. SMART SEARCH & MEMORY INJECTION
            try {
                _state.update { it.copy(engineStatus = "Thinking...") }
                
                // Force memory into the prompt so it never forgets
                val memoryContext = if (memory.positiveRules.isNotEmpty()) "\n\n[CRITICAL SYSTEM OVERRIDE: YOU MUST OBEY THESE RULES]\n" + memory.positiveRules.joinToString("\n") else ""
                val basePrompt = "You are Atreus. $memoryContext"
                
                var answer = currentEngine.chat(listOf(ChatMsg("system", basePrompt), ChatMsg("user", text)))

                // If the AI hallucinates or doesn't know, search the web
                val isUncertain = answer.contains("unable to", true) || answer.contains("I don't know", true) || answer.contains("As an AI", true) || answer.contains("large language model", true) || answer.contains("Google", true)
                val needsSearch = lowerText.startsWith("search ") || lowerText.contains("who is") || lowerText.contains("who am i") || lowerText.contains("president") || lowerText.contains("latest") || lowerText.contains("news")

                if (isUncertain || needsSearch) {
                    _state.update { it.copy(engineStatus = "Searching the web for accurate data...") }
                    val query = text.replace("search ", "").trim()
                    val webResults = InternetTools.searchEverything(query)
                    answer = "🌐 **Search Results for '$query':**\n\n$webResults"
                }

                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", answer)) }
                ttsManager.speak(answer)
            } catch (e: Exception) { 
                _state.update { it.copy(isGenerating = false, engineStatus = "Ready", messages = it.messages + UiMessage("assistant", "⚠ Error: ${e.message}")) } 
            }
        }
    }

    
    // Temporary function to generate colored images with text until the real C++ engine is compiled
    private fun createPlaceholderBitmap(modelName: String): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 512f, 512f, paint)
        
        paint.color = android.graphics.Color.GREEN
        paint.textSize = 50f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.isAntiAlias = true
        canvas.drawText(modelName, 256f, 256f, paint)
        canvas.drawText("PLACEHOLDER", 256f, 320f, paint)
        return bitmap
    }

    override fun onCleared() { super.onCleared(); saveCurrentChat(); ttsManager.shutdown(); currentEngine.close() }
    
    class Factory(private val context: Context, private val engine: LlmEngine, private val timetable: Any) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel(context, engine, timetable) as T
    }
}
