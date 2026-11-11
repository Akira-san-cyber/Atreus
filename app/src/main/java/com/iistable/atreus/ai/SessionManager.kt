package com.iistable.atreus.ai

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS = "atreus_session_vault"
    private const val HOUR_MS = 60 * 60 * 1000L

    // 20 Hours for Update Check
    private const val UPDATE_INTERVAL = 20 * HOUR_MS
    // 3 Hours for Login Re-Check
    private const val LOGIN_INTERVAL = 3 * HOUR_MS
    // 24 Hours for Offline Grace Period
    private const val OFFLINE_GRACE = 24 * HOUR_MS

    fun shouldCheckForUpdate(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheck > UPDATE_INTERVAL) {
            prefs.edit().putLong("last_update_check", now).apply()
            return true
        }
        return false
    }

    fun shouldForceReLogin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastLogin = prefs.getLong("last_login_time", 0L)
        val now = System.currentTimeMillis()
        return (now - lastLogin > LOGIN_INTERVAL)
    }

    fun recordLogin(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong("last_login_time", System.currentTimeMillis()).apply()
    }

    // Offline grace period logic
    fun recordOnlinePing(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong("last_online_ping", System.currentTimeMillis()).apply()
    }

    fun isOfflineGraceExpired(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastPing = prefs.getLong("last_online_ping", 0L)
        val now = System.currentTimeMillis()
        return (now - lastPing > OFFLINE_GRACE)
    }
}
