package com.example.clawdroid.command

sealed class CommandParseResult {
    data class Success(val command: SlashCommand, val args: List<String>, val raw: String) : CommandParseResult()
    data class Unknown(val input: String) : CommandParseResult()
    data class MissingArgs(val command: SlashCommand, val message: String) : CommandParseResult()
}
