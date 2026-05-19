package com.example.clawdroid.command

data class SlashCommand(
    val name: String,
    val aliases: List<String> = emptyList(),
    val description: String,
    val usage: String,
    val hasArgs: Boolean = false,
    val argHint: String = ""
)
