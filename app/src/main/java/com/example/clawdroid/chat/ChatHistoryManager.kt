package com.example.clawdroid.chat

import android.content.Context
import android.content.SharedPreferences
import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.ChatSession
import com.example.clawdroid.model.MessageRole
import org.json.JSONArray
import org.json.JSONObject

class ChatHistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "chat_history"
        private const val KEY_SESSIONS = "sessions"
        private const val KEY_MESSAGES = "messages"
        private const val KEY_CURRENT_SESSION = "current_session"
    }

    fun createSession(
        title: String = "New Chat",
        providerId: String = "",
        modelId: String = ""
    ): ChatSession {
        val session = ChatSession(
            title = title,
            providerId = providerId,
            modelId = modelId
        )
        val sessions = getSessions().toMutableList()
        sessions.add(session)
        saveSessionsRaw(sessions)
        return session
    }

    fun getSessions(): List<ChatSession> {
        val json = prefs.getString(KEY_SESSIONS, "[]") ?: "[]"
        val arr = JSONArray(json)
        val sessions = mutableListOf<ChatSession>()
        for (i in 0 until arr.length()) {
            sessions.add(sessionFromJson(arr.getJSONObject(i)))
        }
        return sessions.sortedByDescending { it.updatedAt }
    }

    fun getSession(id: String): ChatSession? {
        return getSessions().find { it.id == id }
    }

    fun updateSession(session: ChatSession) {
        val sessions = getSessions().toMutableList()
        val index = sessions.indexOfFirst { it.id == session.id }
        if (index != -1) {
            val updated = session.copy(updatedAt = System.currentTimeMillis())
            sessions[index] = updated
            saveSessionsRaw(sessions)
        }
    }

    fun deleteSession(id: String) {
        val sessions = getSessions().filterNot { it.id == id }
        saveSessionsRaw(sessions)

        val messages = getAllMessages().toMutableMap()
        messages.remove(id)
        saveMessages(messages)

        if (getCurrentSessionId() == id) {
            prefs.edit().remove(KEY_CURRENT_SESSION).apply()
        }
    }

    fun addMessage(sessionId: String, message: ChatMessage) {
        val allMessages = getAllMessages().toMutableMap()
        val list = allMessages.getOrPut(sessionId) { mutableListOf() }.toMutableList()
        list.add(message)
        allMessages[sessionId] = list
        saveMessages(allMessages)

        val sessions = getSessions().toMutableList()
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index != -1) {
            val session = sessions[index]
            val newTitle = if (session.title == "New Chat" && message.role == MessageRole.USER) {
                message.content.take(30).ifEmpty { "New Chat" }
            } else {
                session.title
            }
            val updated = session.copy(
                title = newTitle,
                updatedAt = System.currentTimeMillis()
            )
            sessions[index] = updated
            saveSessionsRaw(sessions)
        }
    }

    fun getMessages(sessionId: String): List<ChatMessage> {
        return getAllMessages()[sessionId] ?: emptyList()
    }

    fun clearMessages(sessionId: String) {
        val messages = getAllMessages().toMutableMap()
        messages.remove(sessionId)
        saveMessages(messages)
    }

    fun getCurrentSessionId(): String? {
        return prefs.getString(KEY_CURRENT_SESSION, null)
    }

    fun setCurrentSessionId(id: String) {
        prefs.edit().putString(KEY_CURRENT_SESSION, id).apply()
    }

    private fun getAllMessages(): Map<String, List<ChatMessage>> {
        val json = prefs.getString(KEY_MESSAGES, "{}") ?: "{}"
        val obj = JSONObject(json)
        val result = mutableMapOf<String, List<ChatMessage>>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val arr = obj.getJSONArray(key)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until arr.length()) {
                list.add(messageFromJson(arr.getJSONObject(i)))
            }
            result[key] = list
        }
        return result
    }

    private fun saveSessionsRaw(sessions: List<ChatSession>) {
        val arr = JSONArray()
        sessions.forEach { arr.put(sessionToJson(it)) }
        prefs.edit().putString(KEY_SESSIONS, arr.toString()).apply()
    }

    private fun saveMessages(messages: Map<String, List<ChatMessage>>) {
        val obj = JSONObject()
        messages.forEach { (key, list) ->
            val arr = JSONArray()
            list.forEach { arr.put(messageToJson(it)) }
            obj.put(key, arr)
        }
        prefs.edit().putString(KEY_MESSAGES, obj.toString()).apply()
    }

    private fun sessionToJson(session: ChatSession): JSONObject {
        return JSONObject().apply {
            put("id", session.id)
            put("title", session.title)
            put("createdAt", session.createdAt)
            put("updatedAt", session.updatedAt)
            put("providerId", session.providerId)
            put("modelId", session.modelId)
        }
    }

    private fun sessionFromJson(obj: JSONObject): ChatSession {
        return ChatSession(
            id = obj.getString("id"),
            title = obj.getString("title"),
            createdAt = obj.getLong("createdAt"),
            updatedAt = obj.getLong("updatedAt"),
            providerId = obj.optString("providerId", ""),
            modelId = obj.optString("modelId", "")
        )
    }

    private fun messageToJson(message: ChatMessage): JSONObject {
        return JSONObject().apply {
            put("role", message.role.name)
            put("content", message.content)
            put("timestamp", message.timestamp)
            put("id", message.id)
            message.thinkingContent?.let { put("thinkingContent", it) }
            message.toolName?.let { put("toolName", it) }
            message.toolArguments?.let { put("toolArguments", it) }
            message.toolResult?.let { put("toolResult", it) }
        }
    }

    private fun messageFromJson(obj: JSONObject): ChatMessage {
        return ChatMessage(
            role = MessageRole.valueOf(obj.getString("role")),
            content = obj.getString("content"),
            timestamp = obj.getLong("timestamp"),
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            thinkingContent = obj.optString("thinkingContent")
                .takeIf { it.isNotEmpty() },
            toolName = obj.optString("toolName")
                .takeIf { it.isNotEmpty() },
            toolArguments = obj.optString("toolArguments")
                .takeIf { it.isNotEmpty() },
            toolResult = obj.optString("toolResult")
                .takeIf { it.isNotEmpty() }
        )
    }
}
