package com.iistable.atreus.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object InternetTools {
    suspend fun searchEverything(query: String): String = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            // Using DuckDuckGo's clean JSON API
            val url = URL("https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            
            val abstractText = json.optString("AbstractText")
            if (abstractText.isNotEmpty()) {
                return@withContext abstractText
            }
            
            // If no direct abstract, check related topics
            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null && relatedTopics.length() > 0) {
                val firstTopic = relatedTopics.getJSONObject(0)
                val text = firstTopic.optString("Text")
                if (text.isNotEmpty()) return@withContext text
            }
            
            return@withContext "No exact data found for '$query'."
        } catch (e: Exception) { 
            "Search failed: ${e.message}" 
        }
    }
}
