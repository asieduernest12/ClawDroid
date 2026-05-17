package com.example.clawdroid.server.model

data class ServerStatus(
    val status: String = "stopped",
    val uptimeSeconds: Long = 0,
    val nanoClawRunning: Boolean = false,
    val serverPort: Int = 8080
) {
    val port: Int get() = serverPort
}
