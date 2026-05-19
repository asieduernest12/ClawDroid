package com.example.clawdroid

import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.MessageRole
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class ContextAwarenessTest {

    private fun estimateTokens(text: String): Int = (text.length / 4).coerceAtLeast(1)

    private fun truncateMessages(messages: List<ChatMessage>, maxTokens: Int = 128000): List<ChatMessage> {
        if (messages.isEmpty()) return messages
        val systemPrompt = "You are a helpful AI assistant running inside ClawDroid on Android."
        var runningTokens = estimateTokens(systemPrompt)
        val result = mutableListOf<ChatMessage>()
        for (msg in messages.reversed()) {
            val tokens = estimateTokens(msg.content)
            if (runningTokens + tokens > maxTokens && result.isNotEmpty()) break
            result.add(0, msg)
            runningTokens += tokens
        }
        return result
    }

    @Test
    fun truncateMessages_shortConversation_returnsAll() {
        val messages = listOf(
            ChatMessage(MessageRole.USER, "Hello"),
            ChatMessage(MessageRole.AGENT, "Hi there!"),
            ChatMessage(MessageRole.USER, "How are you?")
        )
        val result = truncateMessages(messages)
        assertEquals(3, result.size)
    }

    @Test
    fun truncateMessages_empty_returnsEmpty() {
        assertTrue(truncateMessages(emptyList()).isEmpty())
    }

    @Test
    fun truncateMessages_fitsWithinLimit_returnsAll() {
        val messages = List(10) { i ->
            ChatMessage(MessageRole.USER, "Message number $i")
        }
        val result = truncateMessages(messages, maxTokens = 100000)
        assertEquals(10, result.size)
    }

    @Test
    fun truncateMessages_veryTightLimit_dropsOldest() {
        val messages = listOf(
            ChatMessage(MessageRole.USER, "A"),
            ChatMessage(MessageRole.AGENT, "B"),
            ChatMessage(MessageRole.USER, "C")
        )
        val result = truncateMessages(messages, maxTokens = 3)
        assertTrue(result.size in 1..2)
        assertEquals("C", result.last().content)
    }

    @Test
    fun truncateMessages_preservesNewestMessages() {
        val messages = List(100) { i ->
            ChatMessage(MessageRole.USER, "Message with some content for number $i")
        }
        val result = truncateMessages(messages, maxTokens = 20)
        assertTrue(result.size < 100)
        assertEquals(messages.last().content, result.last().content)
    }

    @Test
    fun historyMessages_includeAllRoles() {
        val messages = listOf(
            ChatMessage(MessageRole.USER, "Hello"),
            ChatMessage(MessageRole.AGENT, "Hi"),
            ChatMessage(MessageRole.SYSTEM, "Status message"),
            ChatMessage(MessageRole.THINKING, "thinking..."),
            ChatMessage(MessageRole.TOOL_CALL, "tool result", toolName = "fn1", toolResult = "done")
        )
        val visibleMessages = messages.filter { it.role in listOf(MessageRole.USER, MessageRole.AGENT, MessageRole.TOOL_CALL) }
        assertEquals(3, visibleMessages.size)
        assertTrue(visibleMessages.none { it.role == MessageRole.SYSTEM || it.role == MessageRole.THINKING })
    }
}
