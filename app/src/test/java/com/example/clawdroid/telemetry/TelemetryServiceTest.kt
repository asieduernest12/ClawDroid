package com.example.clawdroid.telemetry

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class TelemetryServiceTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockPrefs.getBoolean("telemetry_opt_in", false)).thenReturn(true)
        whenever(mockPrefs.getString("telemetry_events", "[]")).thenReturn("[]")

        TelemetryService.init(mockContext)
    }

    @Test
    fun init_readsOptInState() {
        verify(mockPrefs).getBoolean("telemetry_opt_in", false)
    }

    @Test
    fun isOptedIn_defaultIsFalse() {
        whenever(mockPrefs.getBoolean("telemetry_opt_in", false)).thenReturn(false)
        TelemetryService.init(mockContext)
        assert(!TelemetryService.isOptedIn())
    }

    @Test
    fun setOptedIn_true_enablesTelemetry() {
        TelemetryService.setOptedIn(true)
        verify(mockEditor).putBoolean("telemetry_opt_in", true)
        verify(mockEditor).apply()
    }

    @Test
    fun track_optedIn_storesEvent() {
        TelemetryService.track("test_event", mapOf("key" to "value"))
        val events = TelemetryService.getEvents()
        assert(events.size == 1)
        assert(events[0].type == "test_event")
    }

    @Test
    fun track_optedOut_ignoresEvent() {
        TelemetryService.setOptedIn(false)
        TelemetryService.track("should_not_appear")
        assert(TelemetryService.getEvents().isEmpty())
    }

    @Test
    fun clear_removesAllEvents() {
        TelemetryService.track("event1")
        TelemetryService.track("event2")
        TelemetryService.clear()
        assert(TelemetryService.getEvents().isEmpty())
    }

    @Test
    fun getEventsAsJson_returnsValidJson() {
        TelemetryService.track("json_test")
        val json = TelemetryService.getEventsAsJson()
        assert(json.length() == 1)
        assert(json.getJSONObject(0).getString("type") == "json_test")
    }
}
