package com.iistable.atreus.ai
import android.content.Context
import android.content.SharedPreferences
enum class UserRole { ADMIN, GUEST }
class SecurityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("atreus_security", Context.MODE_PRIVATE)
    fun setRole(role: UserRole) { prefs.edit().putString("current_role", role.name).apply() }
    fun isAdmin(): Boolean = prefs.getString("current_role", UserRole.GUEST.name) == UserRole.ADMIN.name
    fun grantAdminAccess(hours: Int) { prefs.edit().putLong("admin_expiry_time", System.currentTimeMillis() + (hours * 60 * 60 * 1000L)).apply(); setRole(UserRole.ADMIN) }
    fun hasAdminAccess(): Boolean = System.currentTimeMillis() < prefs.getLong("admin_expiry_time", 0L)
    fun revokeAdminAccess() { prefs.edit().putLong("admin_expiry_time", 0L).apply(); setRole(UserRole.GUEST) }
}
