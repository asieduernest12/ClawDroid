package com.example.clawdroid.model

data class PickerModel(
    val modelId: String,
    val displayName: String,
    val providerName: String,
    val providerSlug: String,
    val contextLength: Int? = null,
    val contextDisplay: String = "",
    val isPinned: Boolean = false,
    val lastUsed: Long? = null
)

enum class SortMode {
    RECENT,
    ALPHABETICAL,
    CONTEXT_LENGTH
}

val PROVIDER_COLORS = mapOf(
    "openai" to 0x1A10A37F,
    "anthropic" to 0x1ACC7B8C,
    "deepseek" to 0x1A4F46E5,
    "nvidia" to 0x1A76B900,
    "modelscope" to 0x1AFF9800,
    "google" to 0x1A4285F4,
    "meta" to 0x1A1877F2,
    "microsoft" to 0x1A00A1F1,
    "venice" to 0x1AE91E63,
    "mistral" to 0x1AFF6F00,
    "xai" to 0x1A1A1A2E,
    "cohere" to 0x1A6B7280,
    "default" to 0x1A9CA3AF
)
