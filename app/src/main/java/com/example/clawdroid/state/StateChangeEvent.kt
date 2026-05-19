package com.example.clawdroid.state

data class StateChangeEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val slice: String,
    val previousState: Any?,
    val newState: Any?,
    val source: String = ""
)
