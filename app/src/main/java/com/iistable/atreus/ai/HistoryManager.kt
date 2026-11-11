package com.iistable.atreus.ai
import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
data class HistoryItem(val id: Long, val title: String, val date: String, val messagesJson: String)
class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("atreus_history", Context.MODE_PRIVATE)
    fun saveSession(messagesJson: String) {
        if (messagesJson == "[]") return
        val currentHistory = getHistory().toMutableList()
        val id = System.currentTimeMillis()
        val date = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date())
        var title = "New Chat"
        try {
            val arr = JSONArray(messagesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("role") == "user") {
                    val text = obj.getString("text")
                    title = if (text.length > 25) text.take(25) + "..." else text
                    break
                }
            }
        } catch (e: Exception) {}
        currentHistory.add(0, HistoryItem(id, title, date, messagesJson))
        val jsonArray = JSONArray()
        for (item in currentHistory) {
            val obj = JSONObject()
            obj.put("id", item.id); obj.put("title", item.title); obj.put("date", item.date); obj.put("messages", item.messagesJson)
            jsonArray.put(obj)
        }
        prefs.edit().putString("sessions", jsonArray.toString()).apply()
    }
    fun getHistory(): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        val data = prefs.getString("sessions", "[]") ?: "[]"
        try {
            val arr = JSONArray(data)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(HistoryItem(obj.getLong("id"), obj.getString("title"), obj.getString("date"), obj.getString("messages")))
            }
        } catch (e: Exception) {}
        return list
    }
    fun deleteSession(id: Long) {
        val currentHistory = getHistory().filter { it.id != id }
        val jsonArray = JSONArray()
        for (item in currentHistory) {
            val obj = JSONObject()
            obj.put("id", item.id); obj.put("title", item.title); obj.put("date", item.date); obj.put("messages", item.messagesJson)
            jsonArray.put(obj)
        }
        prefs.edit().putString("sessions", jsonArray.toString()).apply()
    }
}
