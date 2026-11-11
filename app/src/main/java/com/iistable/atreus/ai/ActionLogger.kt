package com.iistable.atreus.ai
import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object ActionLogger {
    fun logAction(context: Context, role: String, action: String) {
        val prefs = context.getSharedPreferences("atreus_logs", Context.MODE_PRIVATE)
        val currentLogs = prefs.getString("history", "") ?: ""
        val time = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        val newLog = "[$time] $role: $action\n"
        prefs.edit().putString("history", newLog + currentLogs).apply()
    }

    fun getLogs(context: Context): String {
        val prefs = context.getSharedPreferences("atreus_logs", Context.MODE_PRIVATE)
        return prefs.getString("history", "No activity recorded yet.") ?: "No activity recorded."
    }
}
