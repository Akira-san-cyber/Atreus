package com.iistable.atreus.ai
import android.content.Context
import android.content.SharedPreferences
class SelfUpdateManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("atreus_memory", Context.MODE_PRIVATE)
    suspend fun learnFromMistake(topic: String, correctRule: String) {
        val current = prefs.getString("rules", "") ?: ""
        val newRule = "- About $topic: $correctRule\n"
        prefs.edit().putString("rules", current + newRule).apply()
    }
    suspend fun getLearnedRules(): String {
        val rules = prefs.getString("rules", "")
        if (rules.isNullOrEmpty()) return "No learned rules yet."
        return "Here are rules you learned from corrections:\n$rules"
    }
}
