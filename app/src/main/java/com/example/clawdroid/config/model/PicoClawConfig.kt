package com.example.clawdroid.config.model

data class PicoClawConfig(
    val binaryPath: String = "",
    val configDir: String = "/data/data/com.example.clawdroid/files/picoclaw",
    val serverPort: Int = 8080,
    val autoStart: Boolean = false,
    val logLevel: LogLevel = LogLevel.INFO
)

enum class LogLevel(val displayName: String) {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR")
}
