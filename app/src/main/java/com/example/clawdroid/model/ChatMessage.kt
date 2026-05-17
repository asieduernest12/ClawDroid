package com.example.clawdroid.model

data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = java.util.UUID.randomUUID().toString(),
    val thinkingContent: String? = null,
    val toolName: String? = null,
    val toolArguments: String? = null,
    val toolResult: String? = null
)

enum class MessageRole {
    USER,
    AGENT,
    SYSTEM,
    THINKING,
    TOOL_CALL
}
