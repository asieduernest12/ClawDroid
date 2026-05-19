package com.example.clawdroid.command

object CommandParser {
    fun parse(input: String, registry: CommandRegistry): CommandParseResult {
        if (!input.startsWith("/")) return CommandParseResult.Unknown(input)
        val tokens = input.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return CommandParseResult.Unknown(input)
        val cmdName = tokens[0]
        val cmd = registry.find(cmdName)
        if (cmd == null) return CommandParseResult.Unknown(input)
        val args = tokens.drop(1)
        if (cmd.hasArgs && args.isEmpty()) {
            return CommandParseResult.MissingArgs(cmd, "Usage: ${cmd.usage}")
        }
        return CommandParseResult.Success(cmd, args, input)
    }
}
