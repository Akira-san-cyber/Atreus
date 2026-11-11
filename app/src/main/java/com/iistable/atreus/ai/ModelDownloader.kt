package com.iistable.atreus.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ModelDownloader {
    var currentUrl = "https://huggingface.co/litert-community/TinyLlama-1.1B-Chat-v1.0/resolve/main/TinyLlama-1.1B-Chat-v1.0.bin"
    private const val MIN_SIZE = 800_000_000L
    
    fun getFile(context: Context): File = File(context.getExternalFilesDir(null) ?: context.filesDir, "tinyllama.bin")
    
    suspend fun downloadIfNeeded(context: Context, onProgress: (Int) -> Unit, onError: (String) -> Unit): Boolean = withContext(Dispatchers.IO) {
        val file = getFile(context)
        if (file.exists() && file.length() > MIN_SIZE) return@withContext true
        if (file.exists()) file.delete()
        
        try {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) { 
                onError("Server error: ${connection.responseCode}")
                return@withContext false 
            }
            
            val fileLength = connection.contentLength.toLong()
            file.outputStream().use { output -> connection.inputStream.use { input ->
                val buffer = ByteArray(1024 * 1024)
                var bytesRead: Int
                var totalRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) { 
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (fileLength > 0) onProgress(((totalRead * 100) / fileLength).toInt()) 
                }
            }}
            true
        } catch (e: Exception) { 
            onError(e.message ?: "Download Failed")
            false 
        }
    }
}
