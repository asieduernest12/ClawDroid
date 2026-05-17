package com.example.clawdroid.model

import org.json.JSONObject

data class ModelProvider(
    val modelName: String,
    val model: String,
    val apiKey: String = "",
    val apiBase: String = ""
) {
    val hasKey: Boolean get() = apiKey.isNotBlank()

    fun toJson(): JSONObject = JSONObject().apply {
        put("model_name", modelName)
        put("model", model)
        if (apiKey.isNotBlank()) put("api_key", apiKey)
        if (apiBase.isNotBlank()) put("api_base", apiBase)
    }

    companion object {
        fun fromJson(json: JSONObject): ModelProvider = ModelProvider(
            modelName = json.optString("model_name", ""),
            model = json.optString("model", ""),
            apiKey = json.optString("api_key", ""),
            apiBase = json.optString("api_base", "")
        )
    }
}
