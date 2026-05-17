package com.example.clawdroid.model

import java.util.UUID

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var providerId: String = "",
    var modelId: String = ""
)
