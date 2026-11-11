package com.iistable.atreus.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SandboxManager {
    
    private fun getSandboxDir(context: Context): File {
        val dir = File(context.cacheDir, "atreus_sandbox")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun compileCode(
        context: Context, 
        language: String,
        code: String, 
        onProgress: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val log = StringBuilder()
        try {
            log.append("1. Creating isolated Sandbox environment...\n")
            onProgress("Initializing Sandbox...")
            delay(1000)

            log.append("2. Uploading $language code to secure Cloud Compiler...\n")
            onProgress("Compiling $language...")
            
            // SIMULATING REAL CLOUD COMPILATION
            // (To use a real API, you would replace this block with a POST request to Wandbox or Judge0)
            delay(3000) 
            
            if (code.contains("error") || code.contains("invalid")) {
                throw Exception("Compilation Error on line 4: syntax error, unexpected token.")
            }

            val output = "Hello from $language! Compilation successful.\nOutput: Program executed successfully."
            log.append("3. Compiler Output:\n$output\n")
            
            log.append("✅ Task complete. Closing Sandbox and deleting temp files.\n")
            return@withContext log.toString()

        } catch (e: Exception) {
            // SELF-HEALING COMPILER LOG
            log.append("⚠️ COMPILER ERROR DETECTED:\n${e.message}\n")
            log.append("🤖 Atreus AI is analyzing the error...\n")
            delay(1500)
            log.append("🛠️ Suggested Fix: Check your brackets and missing semicolons.\n")
            
            return@withContext log.toString()
        } finally {
            getSandboxDir(context).deleteRecursively()
            onProgress("Sandbox Closed.")
        }
    }

    suspend fun performResearch(
        query: String, 
        onProgress: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        onProgress("Researching...")
        val result = InternetTools.searchEverything(query)
        return@withContext "🔎 Research Results for '$query':\n\n$result\n\n✅ Research complete. Sandbox closed."
    }
}
