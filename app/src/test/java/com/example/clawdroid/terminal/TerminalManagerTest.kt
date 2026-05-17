package com.example.clawdroid.terminal

import android.content.Context
import com.example.clawdroid.App
import com.example.clawdroid.terminal.model.TerminalType
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class TerminalManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockApp: App

    private lateinit var manager: TerminalManager

    @Before
    fun setUp() {
        whenever(mockContext.applicationContext).thenReturn(mockApp)
        manager = TerminalManager(mockContext)
    }

    @Test
    fun detectTerminal_returnsNone_whenBootstrapNotReady() {
        whenever(mockApp.bootstrapState).thenReturn(
            MutableStateFlow(TermuxBootstrapState.Uninitialized)
        )

        val result = manager.detectTerminal()
        assert(result == TerminalType.NONE)
    }

    @Test
    fun detectTerminal_returnsEmbedded_whenBootstrapReady() {
        whenever(mockApp.bootstrapState).thenReturn(
            MutableStateFlow(TermuxBootstrapState.Ready)
        )

        val result = manager.detectTerminal()
        assert(result == TerminalType.EMBEDDED)
    }
}
