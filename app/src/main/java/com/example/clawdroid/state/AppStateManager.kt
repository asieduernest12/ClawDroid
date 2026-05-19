package com.example.clawdroid.state

import com.example.clawdroid.telemetry.TelemetryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList

object AppStateManager {

    private val _chat = MutableStateFlow(ChatState())
    val chat: StateFlow<ChatState> = _chat.asStateFlow()

    private val _terminal = MutableStateFlow(TerminalState())
    val terminal: StateFlow<TerminalState> = _terminal.asStateFlow()

    private val _server = MutableStateFlow(ServerState())
    val server: StateFlow<ServerState> = _server.asStateFlow()

    private val _config = MutableStateFlow(ConfigState())
    val config: StateFlow<ConfigState> = _config.asStateFlow()

    private val _stateHistory = MutableStateFlow<List<StateChangeEvent>>(emptyList())
    val stateHistory: StateFlow<List<StateChangeEvent>> = _stateHistory.asStateFlow()

    private const val MAX_HISTORY = 1000

    @Synchronized
    private fun recordEvent(slice: String, prev: Any?, next: Any?, source: String) {
        val event = StateChangeEvent(
            slice = slice,
            previousState = prev,
            newState = next,
            source = source
        )
        val current = _stateHistory.value.toMutableList()
        current.add(event)
        if (current.size > MAX_HISTORY) {
            _stateHistory.value = current.drop(current.size - MAX_HISTORY)
        } else {
            _stateHistory.value = current
        }
        TelemetryService.track("state_change", mapOf(
            "slice" to slice,
            "source" to source
        ))
    }

    fun updateChat(transform: (ChatState) -> ChatState, source: String = "") {
        val prev = _chat.value
        val next = transform(prev)
        _chat.value = next
        recordEvent("chat", prev, next, source)
    }

    fun updateTerminal(transform: (TerminalState) -> TerminalState, source: String = "") {
        val prev = _terminal.value
        val next = transform(prev)
        _terminal.value = next
        recordEvent("terminal", prev, next, source)
    }

    fun updateServer(transform: (ServerState) -> ServerState, source: String = "") {
        val prev = _server.value
        val next = transform(prev)
        _server.value = next
        recordEvent("server", prev, next, source)
    }

    fun updateConfig(transform: (ConfigState) -> ConfigState, source: String = "") {
        val prev = _config.value
        val next = transform(prev)
        _config.value = next
        recordEvent("config", prev, next, source)
    }

    fun snapshot(): AppState = AppState(
        chat = _chat.value,
        terminal = _terminal.value,
        server = _server.value,
        config = _config.value
    )

    fun getHistory(): List<StateChangeEvent> = _stateHistory.value

    fun clearHistory() {
        _stateHistory.value = emptyList()
    }
}
