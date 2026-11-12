package com.iistable.atreus.ai

import android.content.Context
import com.cactus.CactusLM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CactusEngine(private val context: Context, private val modelPath: String) : LlmEngine {
    private var lm: CactusLM? = null
    private var isLoaded = false

    override suspend fun load(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(modelPath)
            if (!file.exists() || file.length() < 100_000_000L) error("Cactus model file missing or incomplete.")
            
            lm = CactusLM()
            // Use the official Cactus API to load the model
            lm?.loadModel(modelPath) 
            isLoaded = true
        }
    }

    override suspend fun chat(messages: List<ChatMsg>): String = withContext(Dispatchers.IO) {
        if (!isLoaded || lm == null) return@withContext "Cactus engine not loaded."
        
        try {
            val prompt = messages.joinToString("\n") { "${it.role}: ${it.content}" } + "\nassistant:"
            // Use the official Cactus API to generate text
            val result = lm?.generate(prompt)
            result ?: "No response from Cactus."
        } catch (e: Exception) {
            "Cactus Error: ${e.message}"
        }
    }

    override fun close() { 
        lm?.close()
        isLoaded = false 
    }
}
