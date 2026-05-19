package com.example.clawdroid.state

data class ChatState(
    val activeSessionId: String? = null,
    val messageCount: Int = 0,
    val isTyping: Boolean = false,
    val currentProvider: String = "",
    val currentModel: String = ""
)

data class TerminalState(
    val isRunning: Boolean = false,
    val lastCommand: String = "",
    val outputLineCount: Int = 0
)

data class ServerState(
    val port: Int = 8080,
    val isRunning: Boolean = false,
    val uptimeMs: Long = 0L
)

data class ConfigState(
    val providerCount: Int = 0,
    val autoConnect: Boolean = false
)

data class AppState(
    val chat: ChatState = ChatState(),
    val terminal: TerminalState = TerminalState(),
    val server: ServerState = ServerState(),
    val config: ConfigState = ConfigState()
)
