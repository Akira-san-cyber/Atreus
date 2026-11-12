package com.iistable.atreus.ai

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder

object OffensiveToolkit {

    // Helper to run shell commands
    private fun runCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            output.toString()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // 1. Tool: Header Analyzer
    fun analyzeHeaders(target: String): String {
        val url = if (target.startsWith("http")) target else "http://$target"
        val result = runCommand("curl -s -I \"$url\"")
        return "--- 🛡️ HEADER ANALYSIS ---\n$result"
    }

    // 2. Tool: Directory Fuzzer (Finds hidden admin panels, git repos, etc.)
    fun fuzzDirectories(target: String): String {
        val url = if (target.startsWith("http")) target else "http://$target"
        val commonPaths = listOf("/admin", "/.git/config", "/robots.txt", "/api/v1", "/backup.zip", "/login")
        val findings = StringBuilder()
        
        for (path in commonPaths) {
            val fullUrl = "$url$path"
            // Check HTTP status code
            val statusCode = runCommand("curl -s -o /dev/null -w \"%{http_code}\" \"$fullUrl\"").trim()
            if (statusCode == "200" || statusCode == "403") {
                findings.append("[FOUND] $fullUrl (Status: $statusCode)\n")
            }
        }
        return if (findings.isEmpty()) "--- 📂 DIRECTORY FUZZING ---\nNo common directories found.\n" else "--- 📂 DIRECTORY FUZZING ---\n$findings"
    }

    // 3. Tool: Basic SQLi/XSS Payload Injector
    fun injectPayloads(target: String): String {
        val url = if (target.startsWith("http")) target else "http://$target"
        val payloads = listOf(
            "'" to "SQL Error test",
            "<script>alert(1)</script>" to "XSS test",
            "../../../etc/passwd" to "LFI test"
        )
        
        val findings = StringBuilder()
        for ((payload, name) in payloads) {
            val encodedPayload = URLEncoder.encode(payload, "UTF-8")
            val testUrl = "$url?test=$encodedPayload"
            val response = runCommand("curl -s \"$testUrl\"")
            
            // Check for common error strings that indicate a vulnerability
            if (response.contains("SQL syntax", ignoreCase = true) || 
                response.contains("Warning: mysql", ignoreCase = true) ||
                response.contains(payload, ignoreCase = true)) {
                findings.append("[🚨 VULNERABLE] Potential $name found with payload: $payload\n")
            }
        }
        return if (findings.isEmpty()) "--- 🎯 PAYLOAD INJECTION ---\nNo immediate basic vulnerabilities found.\n" else "--- 🎯 PAYLOAD INJECTION ---\n$findings"
    }

    // Master function to run all tools
    fun executeFullScan(target: String): String {
        val report = StringBuilder()
        report.append(analyzeHeaders(target))
        report.append("\n")
        report.append(fuzzDirectories(target))
        report.append("\n")
        report.append(injectPayloads(target))
        return report.toString()
    }
}
