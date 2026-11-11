package com.iistable.atreus.ai

import org.json.JSONObject
import java.io.File

object SelfUpgradeGate {
    private val allowed = setOf(
        "assets/personality.txt", "data/rules.json", "data/examples.json",
        "ui/ChatScreen.kt", "ui/ChatViewModel.kt"
    )

    // Saves the proposal as a patch file. It never applies anything.
    fun saveIfApproved(filesDir: File, json: String, userApproved: Boolean): Boolean {
        if (!userApproved) return false
        val proposal = try { JSONObject(json) } catch (e: Exception) { return false }
        if (proposal.has("skip")) return false

        val diff = proposal.getString("diff")
        if (diff.length > 4000) return false

        // Check the paths the diff actually touches, not just the listed files
        val touched = diff.lines()
            .filter { it.startsWith("+++ ") }
            .map { it.removePrefix("+++ ").removePrefix("b/").trim() }
        if (touched.isEmpty() || touched.any { it !in allowed }) return false

        val out = File(filesDir, "proposals/${System.currentTimeMillis()}.patch")
        out.parentFile?.mkdirs()
        out.writeText(diff)
        return true
    }
}
