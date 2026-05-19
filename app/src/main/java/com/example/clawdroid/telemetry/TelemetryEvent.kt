package com.example.clawdroid.telemetry

import org.json.JSONObject

data class TelemetryEvent(
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, Any> = emptyMap()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("type", type)
        put("timestamp", timestamp)
        val dataObj = JSONObject()
        data.forEach { (k, v) ->
            when (v) {
                is String -> dataObj.put(k, v)
                is Number -> dataObj.put(k, v)
                is Boolean -> dataObj.put(k, v)
                else -> dataObj.put(k, v.toString())
            }
        }
        put("data", dataObj)
    }

    companion object {
        fun fromJson(json: JSONObject): TelemetryEvent {
            val dataObj = json.optJSONObject("data") ?: JSONObject()
            val data = mutableMapOf<String, Any>()
            dataObj.keys().forEach { key ->
                val value = dataObj.get(key)
                data[key] = value
            }
            return TelemetryEvent(
                type = json.getString("type"),
                timestamp = json.getLong("timestamp"),
                data = data
            )
        }
    }
}
