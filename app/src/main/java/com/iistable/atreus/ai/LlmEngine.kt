package com.iistable.atreus.ai

data class ChatMsg(val role: String, val content: String)

interface LlmEngine {
    suspend fun load(): Result<Unit>
    suspend fun chat(messages: List<ChatMsg>): String
    fun close()
}
