package com.iistable.atreus.ai

import android.content.Context
import android.content.SharedPreferences

object PlanManager {
    private const val PREFS = "atreus_plan"
    private const val KEY_IS_PRO = "is_pro"

    fun isPro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PRO, false)
    }

    fun upgradeToPro(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PRO, true).apply()
    }
}
