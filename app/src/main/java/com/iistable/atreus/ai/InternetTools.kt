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
            val url = URL("https://html.duckduckgo.com/html/?q=${URLEncoder.encode(query, "UTF-8")}")
            val html = (url.openConnection() as HttpURLConnection).apply { setRequestProperty("User-Agent", "Mozilla/5.0") }.inputStream.bufferedReader().readText()
            val titles = """<a rel="nofollow" class="result__a" href="(.*?)">(.*?)</a>""".toRegex().findAll(html).take(3).toList()
            if (titles.isEmpty()) return@withContext "No results."
            buildString { appendLine("Results for '$query':\n"); titles.forEach { appendLine("- ${it.groupValues[2].replace("<[^>]*>".toRegex(), "")}") } }
        } catch (e: Exception) { "Search failed." }
    }
    suspend fun searchGitHub(query: String): String = withContext(Dispatchers.IO) {
         try {
            val url = URL("https://api.github.com/search/repositories?q=${URLEncoder.encode(query, "UTF-8")}&sort=stars")
            val json = (url.openConnection() as HttpURLConnection).apply { setRequestProperty("Accept", "application/vnd.github.v3+json") }.inputStream.bufferedReader().readText()
            val items = JSONObject(json).getJSONArray("items")
            if (items.length() == 0) return@withContext "No repos."
            val repo = items.getJSONObject(0)
            "GitHub: ${repo.getString("full_name")} (${repo.getInt("stargazers_count")} stars)"
        } catch (e: Exception) { "GitHub failed." }
    }
}
