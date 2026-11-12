package com.iistable.atreus.ai

import android.content.Context
import java.io.File

enum class ModelType(val fileName: String, val url: String, val isChatModel: Boolean) {
    TINYLLAMA("tinyllama.bin", "https://huggingface.co/litert-community/TinyLlama-1.1B-Chat-v1.0/resolve/main/TinyLlama-1.1B-Chat-v1.0.bin", true),
    QWEN("qwen2.5-0.5b-q4_k_m.gguf", "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf", false)
}

object ModelManager {
    fun getFile(context: Context, type: ModelType): File {
        return File(context.getExternalFilesDir(null) ?: context.filesDir, type.fileName)
    }

    suspend fun downloadIfNeeded(context: Context, type: ModelType, onProgress: (Int) -> Unit): Boolean {
        val file = getFile(context, type)
        
        // Check if file exists and is fully downloaded
        val minSize = if (type == ModelType.TINYLLAMA) 800_000_000L else 300_000_000L
        if (file.exists() && file.length() > minSize) return true
        
        if (file.exists()) file.delete() // Delete broken file
        
        // Use the existing ModelDownloader for both
        ModelDownloader.currentUrl = type.url
        return ModelDownloader.downloadIfNeeded(context, onProgress) { _ -> }
    }
}
