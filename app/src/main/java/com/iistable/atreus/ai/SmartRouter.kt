package com.iistable.atreus.ai

object SmartRouter {
    // If the user asks a factual question, trigger a web search
    fun shouldSearchWeb(text: String): Boolean {
        val lower = text.lowercase()
        val triggers = listOf("who is", "what is the latest", "news about", "weather in", "current price", "search for")
        return triggers.any { lower.contains(it) }
    }
}
