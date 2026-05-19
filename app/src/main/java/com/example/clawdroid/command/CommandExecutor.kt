package com.example.clawdroid.command

import android.widget.Toast
import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.MessageRole
import com.example.clawdroid.state.AppStateManager

typealias CommandHandler = (args: List<String>, context: CommandContext) -> Unit

class CommandExecutor(
    val registry: CommandRegistry,
    private val context: CommandContext
) {
    private val handlers = mutableMapOf<String, CommandHandler>()

    fun registerHandler(commandName: String, handler: CommandHandler) {
        handlers[commandName] = handler
    }

    fun execute(rawInput: String): CommandParseResult {
        val result = CommandParser.parse(rawInput, registry)
        when (result) {
            is CommandParseResult.Success -> {
                val handler = handlers[result.command.name]
                if (handler != null) {
                    handler(result.args, context)
                }
            }
            is CommandParseResult.MissingArgs -> {
                val msg = result.message
                context.activity.runOnUiThread {
                    context.chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, "⚠️ $msg"))
                }
            }
            is CommandParseResult.Unknown -> {
                context.activity.runOnUiThread {
                    context.chatAdapter.addMessage(
                        ChatMessage(MessageRole.SYSTEM, "⚠️ Unknown command: ${result.input}. Type /help for available commands.")
                    )
                }
            }
        }
        return result
    }

    fun setupDefaultHandlers() {
        registerHandler("clear") { _, ctx ->
            val sessionId = ctx.chatHistoryManager.getCurrentSessionId()
            if (sessionId != null) {
                ctx.chatHistoryManager.clearMessages(sessionId)
            }
            ctx.activity.runOnUiThread {
                ctx.chatAdapter.clearMessages()
                ctx.chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, "Chat cleared"))
            }
        }

        registerHandler("help") { _, ctx ->
            val helpText = buildString {
                appendLine("**Available Commands**")
                appendLine()
                registry.getAll().forEach { cmd ->
                    append("• `${cmd.usage}`")
                    if (cmd.hasArgs) append(" ${cmd.argHint}")
                    append(" — ${cmd.description}")
                    appendLine()
                }
            }
            ctx.activity.runOnUiThread {
                ctx.chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, helpText))
            }
        }

        registerHandler("model") { args, ctx ->
            if (args.isNotEmpty()) {
                Toast.makeText(ctx.activity, "Switch model to: ${args[0]}", Toast.LENGTH_SHORT).show()
            }
        }

        registerHandler("provider") { args, ctx ->
            if (args.isNotEmpty()) {
                Toast.makeText(ctx.activity, "Switch provider to: ${args[0]}", Toast.LENGTH_SHORT).show()
            }
        }

        registerHandler("session") { args, ctx ->
            if (args.isNotEmpty() && args[0] == "new") {
                val session = ctx.chatHistoryManager.createSession()
                ctx.chatHistoryManager.setCurrentSessionId(session.id)
                ctx.activity.runOnUiThread {
                    ctx.chatAdapter.clearMessages()
                    ctx.chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, "New session created: ${session.title}"))
                }
            } else {
                Toast.makeText(ctx.activity, "Usage: /session new", Toast.LENGTH_SHORT).show()
            }
        }

        registerHandler("export") { _, ctx ->
            val sessionId = ctx.chatHistoryManager.getCurrentSessionId()
            if (sessionId != null) {
                val messages = ctx.chatHistoryManager.getMessages(sessionId)
                val exportText = buildString {
                    messages.forEach { msg ->
                        appendLine("[${msg.role.name}] ${msg.content}")
                    }
                }
                ctx.activity.runOnUiThread {
                    ctx.chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, "📋 **Export (${messages.size} messages):**\n```\n$exportText\n```"))
                }
            }
        }
    }
}
