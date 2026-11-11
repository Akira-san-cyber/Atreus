package com.iistable.atreus.ai
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CactusEngine(private val context: Context, private val modelPath: String) : LlmEngine {
    private var isLoaded = false
    external fun generateNative(modelPath: String, prompt: String): String
    companion object { init { try { System.loadLibrary("atreuscactus") } catch (e: UnsatisfiedLinkError) { e.printStackTrace() } } }
    override suspend fun load(): Result<Unit> = withContext(Dispatchers.IO) { runCatching { Thread.sleep(500); isLoaded = true } }
    override suspend fun chat(messages: List<ChatMsg>): String = withContext(Dispatchers.IO) {
        if (!isLoaded) return@withContext "Cactus engine not loaded."
        val prompt = messages.joinToString("\n") { "${it.role}: ${it.content}" } + "\nassistant:"
        try { generateNative(modelPath, prompt) } catch (e: Exception) { "JNI Error: ${e.message}" }
    }
    override fun close() { isLoaded = false }
}
