package com.iistable.atreus.ai

import android.content.Context
import android.content.SharedPreferences

object ReminderManager {
    fun setReminder(context: Context, task: String, time: String): String {
        val prefs: SharedPreferences = context.getSharedPreferences("atreus_reminders", Context.MODE_PRIVATE)
        val currentReminders = prefs.getStringSet("tasks", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        val newReminder = "$time - $task"
        currentReminders.add(newReminder)
        prefs.edit().putStringSet("tasks", currentReminders).apply()
        
        return "✅ Reminder set: I will remind you to '$task' at $time."
    }

    fun getReminders(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("atreus_reminders", Context.MODE_PRIVATE)
        val tasks = prefs.getStringSet("tasks", emptySet())
        if (tasks.isNullOrEmpty()) return "You have no reminders set."
        return "📅 Your Reminders:\n" + tasks.joinToString("\n")
    }
}
