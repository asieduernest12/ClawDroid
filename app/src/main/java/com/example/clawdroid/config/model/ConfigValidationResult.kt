package com.example.clawdroid.config.model

sealed class ConfigValidationResult {
    data object Success : ConfigValidationResult()
    data class Error(val fieldErrors: Map<String, String>) : ConfigValidationResult()
}
