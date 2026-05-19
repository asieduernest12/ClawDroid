package com.example.clawdroid.state

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class AppStateManagerTest {

    @Test
    fun initialState_hasDefaults() {
        assertEquals(0, AppStateManager.snapshot().chat.messageCount)
        assertEquals(false, AppStateManager.snapshot().terminal.isRunning)
        assertEquals(8080, AppStateManager.snapshot().server.port)
        assertEquals(0, AppStateManager.snapshot().config.providerCount)
    }

    @Test
    fun updateChat_modifiesChatState() {
        AppStateManager.updateChat({ state -> state.copy(messageCount = 5) })
        assertEquals(5, AppStateManager.snapshot().chat.messageCount)
    }

    @Test
    fun updateTerminal_modifiesTerminalState() {
        AppStateManager.updateTerminal({ state -> state.copy(isRunning = true) })
        assertEquals(true, AppStateManager.snapshot().terminal.isRunning)
    }

    @Test
    fun updateServer_modifiesServerState() {
        AppStateManager.updateServer({ state -> state.copy(port = 9090) })
        assertEquals(9090, AppStateManager.snapshot().server.port)
    }

    @Test
    fun updateConfig_modifiesConfigState() {
        AppStateManager.updateConfig({ state -> state.copy(providerCount = 3) })
        assertEquals(3, AppStateManager.snapshot().config.providerCount)
    }

    @Test
    fun history_recordsStateChanges() {
        AppStateManager.clearHistory()
        AppStateManager.updateChat({ state -> state.copy(messageCount = 1) }, source = "test")
        AppStateManager.updateTerminal({ state -> state.copy(isRunning = true) }, source = "test")
        val history = AppStateManager.getHistory()
        assertEquals(2, history.size)
        assertEquals("chat", history[0].slice)
        assertEquals("terminal", history[1].slice)
    }

    @Test
    fun clearHistory_emptiesHistory() {
        AppStateManager.updateChat({ state -> state.copy(messageCount = 1) })
        AppStateManager.clearHistory()
        assertTrue(AppStateManager.getHistory().isEmpty())
    }

    @Test
    fun snapshot_returnsStateAtMoment() {
        AppStateManager.updateChat({ state -> state.copy(currentModel = "gpt-4") }, source = "test")
        val snap = AppStateManager.snapshot()
        assertEquals("gpt-4", snap.chat.currentModel)
    }
}
