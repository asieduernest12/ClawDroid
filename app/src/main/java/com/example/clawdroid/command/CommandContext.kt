package com.example.clawdroid.command

import android.app.Activity
import com.example.clawdroid.chat.ChatAdapter
import com.example.clawdroid.chat.ChatHistoryManager
import com.example.clawdroid.config.ProviderConfigManager

data class CommandContext(
    val activity: Activity,
    val chatHistoryManager: ChatHistoryManager,
    val configManager: ProviderConfigManager,
    val chatAdapter: ChatAdapter
)
