package com.iistable.atreus.ai
object SecurityTools {
    fun scanThreat(text: String): String? {
        if (text.contains(Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b"""))) return "⚠️ Warning: Raw IP address detected."
        if (text.lowercase().startsWith("http://")) return "⚠️ Warning: Unencrypted HTTP link."
        return null
    }
}
