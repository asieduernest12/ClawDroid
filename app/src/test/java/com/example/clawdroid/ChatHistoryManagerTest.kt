package com.example.clawdroid

import android.content.Context
import android.content.SharedPreferences
import com.example.clawdroid.chat.ChatHistoryManager
import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.MessageRole
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class ChatHistoryManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var manager: ChatHistoryManager

    @Before
    fun setUp() {
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.remove(any())).thenReturn(mockEditor)

        manager = ChatHistoryManager(mockContext)
    }

    @Test
    fun createSession_returnsSessionWithDefaults() {
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn("[]")

        val session = manager.createSession()

        assert(session.title == "New Chat")
        assert(session.providerId == "")
        assert(session.modelId == "")
        assert(session.id.isNotEmpty())
        verify(mockEditor).apply()
    }

    @Test
    fun createSession_withParameters_returnsConfiguredSession() {
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn("[]")

        val session = manager.createSession("Test Title", "provider1", "model1")

        assert(session.title == "Test Title")
        assert(session.providerId == "provider1")
        assert(session.modelId == "model1")
    }

    @Test
    fun getSessions_returnsSessionsSortedByUpdatedAtDescending() {
        val sessionsJson = """
            [
                {"id":"session-1","title":"First","createdAt":1000,"updatedAt":1000,"providerId":"","modelId":""},
                {"id":"session-2","title":"Second","createdAt":1000,"updatedAt":3000,"providerId":"","modelId":""},
                {"id":"session-3","title":"Third","createdAt":1000,"updatedAt":2000,"providerId":"","modelId":""}
            ]
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)

        val sessions = manager.getSessions()

        assert(sessions.size == 3)
        assert(sessions[0].id == "session-2")
        assert(sessions[1].id == "session-3")
        assert(sessions[2].id == "session-1")
    }

    @Test
    fun getSession_existingId_returnsSession() {
        val sessionsJson = """
            [
                {"id":"session-abc","title":"Test Session","createdAt":1000,"updatedAt":2000,"providerId":"p1","modelId":"m1"}
            ]
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)

        val result = manager.getSession("session-abc")

        assert(result != null)
        assert(result?.title == "Test Session")
        assert(result?.providerId == "p1")
        assert(result?.modelId == "m1")
    }

    @Test
    fun getSession_nonExistingId_returnsNull() {
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn("[]")

        val result = manager.getSession("non-existent")

        assert(result == null)
    }

    @Test
    fun updateSession_updatesSessionAndPersists() {
        val sessionsJson = """
            [
                {"id":"session-abc","title":"Old Title","createdAt":1000,"updatedAt":2000,"providerId":"","modelId":""}
            ]
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)

        val session = manager.getSession("session-abc")!!
        session.title = "New Title"
        manager.updateSession(session)

        val captor = argumentCaptor<String>()
        verify(mockEditor, atLeast(1)).putString(any(), captor.capture())
        val savedJson = captor.allValues.find { it.contains("New Title") }
        assert(savedJson != null)
    }

    @Test
    fun deleteSession_removesSessionAndMessagesAndCurrentSessionRef() {
        val sessionsJson = """
            [
                {"id":"session-del","title":"To Delete","createdAt":1000,"updatedAt":2000,"providerId":"","modelId":""}
            ]
        """.trimIndent()
        val messagesJson = """
            {"session-del":[{"role":"USER","content":"Hello","timestamp":1234,"sessionId":"session-del"}]}
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)
        whenever(mockPrefs.getString("messages", "{}")).thenReturn(messagesJson)
        whenever(mockPrefs.getString("current_session", null)).thenReturn("session-del")

        manager.deleteSession("session-del")

        verify(mockEditor).remove("current_session")
    }

    @Test
    fun addMessage_addsMessageToSession() {
        val sessionsJson = """
            [
                {"id":"session-msg","title":"Test Session","createdAt":1000,"updatedAt":2000,"providerId":"","modelId":""}
            ]
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)
        whenever(mockPrefs.getString("messages", "{}")).thenReturn("{}")

        val message = ChatMessage(
            role = MessageRole.USER,
            content = "Hello"
        )
        manager.addMessage("session-msg", message)

        val captor = argumentCaptor<String>()
        verify(mockEditor, atLeast(1)).putString(any(), captor.capture())
        val savedMessages = captor.allValues.find { it.contains("Hello") }
        assert(savedMessages != null)
    }

    @Test
    fun addMessage_autoGeneratesTitleFromFirstUserMessage() {
        val sessionsJson = """
            [
                {"id":"session-auto","title":"New Chat","createdAt":1000,"updatedAt":2000,"providerId":"","modelId":""}
            ]
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)
        whenever(mockPrefs.getString("messages", "{}")).thenReturn("{}")

        val message = ChatMessage(
            role = MessageRole.USER,
            content = "This is my first message"
        )
        manager.addMessage("session-auto", message)

        val captor = argumentCaptor<String>()
        verify(mockEditor, atLeast(1)).putString(any(), captor.capture())
        val savedSessions = captor.allValues.find { it.contains("This is my first message") }
        assert(savedSessions != null)
    }

    @Test
    fun addMessage_doesNotOverrideCustomTitle() {
        val sessionsJson = """
            [
                {"id":"session-custom","title":"Custom Title","createdAt":1000,"updatedAt":2000,"providerId":"","modelId":""}
            ]
        """.trimIndent()
        whenever(mockPrefs.getString("sessions", "[]")).thenReturn(sessionsJson)
        whenever(mockPrefs.getString("messages", "{}")).thenReturn("{}")

        val message = ChatMessage(
            role = MessageRole.USER,
            content = "New message"
        )
        manager.addMessage("session-custom", message)

        val captor = argumentCaptor<String>()
        verify(mockEditor, atLeast(1)).putString(any(), captor.capture())
        val savedSessions = captor.allValues.find { it.contains("Custom Title") }
        assert(savedSessions != null)
    }

    @Test
    fun getMessages_returnsMessagesForSession() {
        val messagesJson = """
            {"session-read":[
                {"role":"USER","content":"First","timestamp":1000,"sessionId":"session-read"},
                {"role":"AGENT","content":"Second","timestamp":2000,"sessionId":"session-read"}
            ]}
        """.trimIndent()
        whenever(mockPrefs.getString("messages", "{}")).thenReturn(messagesJson)

        val messages = manager.getMessages("session-read")

        assert(messages.size == 2)
        assert(messages[0].content == "First")
        assert(messages[0].role == MessageRole.USER)
        assert(messages[1].content == "Second")
        assert(messages[1].role == MessageRole.AGENT)
    }

    @Test
    fun getMessages_emptySession_returnsEmptyList() {
        whenever(mockPrefs.getString("messages", "{}")).thenReturn("{}")

        val messages = manager.getMessages("session-empty")

        assert(messages.isEmpty())
    }

    @Test
    fun clearMessages_removesAllMessagesForSession() {
        val messagesJson = """
            {"session-clear":[{"role":"USER","content":"Hello","timestamp":1234,"sessionId":"session-clear"}]}
        """.trimIndent()
        whenever(mockPrefs.getString("messages", "{}")).thenReturn(messagesJson)

        manager.clearMessages("session-clear")

        val captor = argumentCaptor<String>()
        verify(mockEditor, atLeast(1)).putString(any(), captor.capture())
        val savedMessages = captor.allValues.last()
        assert(!savedMessages.contains(""""session-clear""""))
    }

    @Test
    fun setCurrentSessionId_persistsId() {
        manager.setCurrentSessionId("session-xyz")

        verify(mockEditor).putString("current_session", "session-xyz")
        verify(mockEditor).apply()
    }

    @Test
    fun getCurrentSessionId_returnsPersistedId() {
        whenever(mockPrefs.getString("current_session", null)).thenReturn("session-xyz")

        val current = manager.getCurrentSessionId()

        assert(current == "session-xyz")
    }

    @Test
    fun getCurrentSessionId_noSession_returnsNull() {
        whenever(mockPrefs.getString("current_session", null)).thenReturn(null)

        val current = manager.getCurrentSessionId()

        assert(current == null)
    }
}
