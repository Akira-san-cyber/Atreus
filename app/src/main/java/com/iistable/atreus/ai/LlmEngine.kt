package com.iistable.atreus.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMsg(val role: String, val content: String)

interface LlmEngine {
    suspend fun load(): Result<Unit>
    suspend fun chat(messages: List<ChatMsg>): String
    fun close()
}

class LlamaEngine(private val context: Context) : LlmEngine {
    private var inference: LlmInference? = null
    private var isLoaded = false

    override suspend fun load(): Result<Unit> = withContext(Dispatchers.IO) { 
        runCatching {
            val file = ModelDownloader.getFile(context)
            if (!file.exists() || file.length() < 800_000_000L) error("Model incomplete.")
            inference = LlmInference.createFromOptions(
                context, 
                LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(file.absolutePath)
                    .setMaxTokens(1024)
                    .build()
            )
            isLoaded = true 
        } 
    }

    override suspend fun chat(messages: List<ChatMsg>): String = withContext(Dispatchers.IO) {
        if (!isLoaded || inference == null) return@withContext "Brain not loaded."
        
        val promptBuilder = StringBuilder()
        for (msg in messages) {
            when (msg.role) {
                "system" -> promptBuilder.append("<|system|>\n${msg.content}</s>\n")
                "user" -> promptBuilder.append("<|user|>\n${msg.content}</s>\n")
                "assistant" -> promptBuilder.append("<|assistant|>\n${msg.content}</s>\n")
            }
        }
        promptBuilder.append("<|assistant|>\n")
        
        try { 
            inference!!.generateResponse(promptBuilder.toString()) 
        } catch (e: Exception) { 
            "Error: ${e.message}" 
        } 
    }

    override fun close() { inference?.close() }
}
