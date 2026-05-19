package com.example.clawdroid.telemetry

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.json.JSONObject

class TelemetryEventTest {

    @Test
    fun toJson_containsAllFields() {
        val event = TelemetryEvent(
            type = "test_event",
            data = mapOf("key1" to "value1", "count" to 42)
        )
        val json = event.toJson()
        assertEquals("test_event", json.getString("type"))
        assertTrue(json.getLong("timestamp") > 0)
        val data = json.getJSONObject("data")
        assertEquals("value1", data.getString("key1"))
        assertEquals(42, data.getInt("count"))
    }

    @Test
    fun fromJson_roundTrip() {
        val original = TelemetryEvent(
            type = "roundtrip",
            data = mapOf("foo" to "bar", "num" to 99)
        )
        val json = original.toJson()
        val restored = TelemetryEvent.fromJson(json)
        assertEquals(original.type, restored.type)
        assertEquals(original.data["foo"], restored.data["foo"])
        assertEquals(original.data["num"], restored.data["num"])
    }

    @Test
    fun fromJson_emptyData_returnsEmptyMap() {
        val json = JSONObject().apply {
            put("type", "empty")
            put("timestamp", 1000L)
        }
        val event = TelemetryEvent.fromJson(json)
        assertEquals("empty", event.type)
        assertTrue(event.data.isEmpty())
    }
}
