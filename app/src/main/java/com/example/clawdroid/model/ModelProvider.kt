package com.example.clawdroid.model

import org.json.JSONObject

data class ModelProvider(
    val modelName: String,
    val model: String,
    val provider: String = "",
    val apiKey: String = "",
    val apiBase: String = ""
) {
    val hasKey: Boolean get() = apiKey.isNotBlank()

    val displayName: String
        get() = provider.takeIf { it.isNotBlank() }
            ?: modelName.substringBeforeLast("-").replaceFirstChar { it.uppercase() }
            ?: modelName

    fun toJson(): JSONObject = JSONObject().apply {
        put("model_name", modelName)
        put("model", model)
        if (provider.isNotBlank()) put("provider", provider)
        if (apiKey.isNotBlank()) put("api_key", apiKey)
        if (apiBase.isNotBlank()) put("api_base", apiBase)
    }

    companion object {
        fun fromJson(json: JSONObject): ModelProvider = ModelProvider(
            modelName = json.optString("model_name", ""),
            model = json.optString("model", ""),
            provider = json.optString("provider", ""),
            apiKey = json.optString("api_key", ""),
            apiBase = json.optString("api_base", "")
        )
    }
}
