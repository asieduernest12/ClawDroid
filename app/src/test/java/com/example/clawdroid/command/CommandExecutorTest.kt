package com.example.clawdroid.command

import android.app.Activity
import com.example.clawdroid.chat.ChatAdapter
import com.example.clawdroid.chat.ChatHistoryManager
import com.example.clawdroid.config.ProviderConfigManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class CommandExecutorTest {

    @Mock
    private lateinit var mockActivity: Activity

    @Mock
    private lateinit var mockHistoryManager: ChatHistoryManager

    @Mock
    private lateinit var mockConfigManager: ProviderConfigManager

    @Mock
    private lateinit var mockChatAdapter: ChatAdapter

    private lateinit var executor: CommandExecutor
    private lateinit var context: CommandContext

    @Before
    fun setUp() {
        context = CommandContext(
            activity = mockActivity,
            chatHistoryManager = mockHistoryManager,
            configManager = mockConfigManager,
            chatAdapter = mockChatAdapter
        )
        val registry = CommandRegistry.defaultCommands()
        executor = CommandExecutor(registry, context)
        executor.setupDefaultHandlers()
    }

    @Test
    fun execute_unknownCommand_returnsUnknown() {
        val result = executor.execute("/foobar")
        assert(result is CommandParseResult.Unknown)
    }

    @Test
    fun execute_clear_returnsSuccess() {
        whenever(mockHistoryManager.getCurrentSessionId()).thenReturn("session-1")
        val result = executor.execute("/clear")
        assert(result is CommandParseResult.Success)
    }

    @Test
    fun execute_modelWithoutArgs_returnsMissingArgs() {
        val result = executor.execute("/model")
        assert(result is CommandParseResult.MissingArgs)
    }
}
