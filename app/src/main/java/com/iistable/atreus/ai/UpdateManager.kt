package com.iistable.atreus.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
object UpdateManager {
    private const val VERSION_URL = "https://raw.githubusercontent.com/Akira-san-cyber/Atreus/main/version.txt"
    suspend fun checkForUpdate(): String? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(VERSION_URL).openConnection() as HttpURLConnection
            val response = connection.inputStream.bufferedReader().readText().trim()
            if (response.isNotEmpty() && response != "75.0.0") response else null
        } catch (e: Exception) { null }
    }
}
