package com.iistable.atreus.ai
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
object CalendarHelper {
    fun getTodayEvents(context: Context): String {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return "Calendar permission not granted."
        val events = StringBuilder()
        val startOfDay = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val endOfDay = startOfDay + 86400000L
        val projection = arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART)
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val cursor = context.contentResolver.query(CalendarContract.Events.CONTENT_URI, projection, selection, arrayOf(startOfDay.toString(), endOfDay.toString()), CalendarContract.Events.DTSTART + " ASC")
        cursor?.use { while (it.moveToNext()) { events.append("- ${it.getString(0)} at ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it.getLong(1)))}\n") } }
        return if (events.isEmpty()) "No events today." else "Events today:\n$events"
    }
}
