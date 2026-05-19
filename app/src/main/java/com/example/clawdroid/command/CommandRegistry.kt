package com.example.clawdroid.command

class CommandRegistry {
    private val commands = mutableMapOf<String, SlashCommand>()

    fun register(command: SlashCommand) {
        commands[command.name] = command
    }

    fun find(input: String): SlashCommand? {
        val trimmed = input.removePrefix("/")
        val cmd = commands[trimmed]
        if (cmd != null) return cmd
        return commands.values.find { trimmed in it.aliases }
    }

    fun search(query: String): List<SlashCommand> {
        val trimmed = query.removePrefix("/").lowercase()
        if (trimmed.isBlank()) return getAll()
        return commands.values.filter {
            it.name.startsWith(trimmed, ignoreCase = true) ||
            it.aliases.any { a -> a.startsWith(trimmed, ignoreCase = true) }
        }
    }

    fun getAll(): List<SlashCommand> = commands.values.sortedBy { it.name }

    companion object {
        fun defaultCommands(): CommandRegistry {
            val registry = CommandRegistry()
            registry.register(SlashCommand(
                name = "clear",
                description = "Clear the current chat conversation",
                usage = "/clear"
            ))
            registry.register(SlashCommand(
                name = "model",
                description = "Switch the active AI model",
                usage = "/model <model name>",
                hasArgs = true,
                argHint = "<model name>"
            ))
            registry.register(SlashCommand(
                name = "provider",
                description = "Switch the AI provider",
                usage = "/provider <provider name>",
                hasArgs = true,
                argHint = "<provider name>"
            ))
            registry.register(SlashCommand(
                name = "session",
                description = "Manage chat sessions",
                usage = "/session new | list | switch <id>",
                hasArgs = true,
                argHint = "new | list | switch <id>"
            ))
            registry.register(SlashCommand(
                name = "help",
                aliases = listOf("h", "?"),
                description = "Show available commands and usage",
                usage = "/help"
            ))
            registry.register(SlashCommand(
                name = "export",
                description = "Export the current conversation",
                usage = "/export"
            ))
            return registry
        }
    }
}
