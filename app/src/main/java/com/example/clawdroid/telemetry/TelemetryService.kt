package com.example.clawdroid.telemetry

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.util.LinkedList

object TelemetryService {

    private const val PREFS_NAME = "telemetry"
    private const val KEY_OPT_IN = "telemetry_opt_in"
    private const val KEY_EVENTS = "telemetry_events"
    private const val MAX_EVENTS = 500

    private val events = LinkedList<TelemetryEvent>()
    private var prefs: SharedPreferences? = null
    private var enabled = false

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        enabled = prefs!!.getBoolean(KEY_OPT_IN, false)
        loadEvents()
    }

    fun isOptedIn(): Boolean = enabled

    fun setOptedIn(optIn: Boolean) {
        enabled = optIn
        prefs?.edit()?.putBoolean(KEY_OPT_IN, optIn)?.apply()
        if (!optIn) {
            events.clear()
            saveEvents()
        }
    }

    fun track(type: String, data: Map<String, Any> = emptyMap()) {
        if (!enabled) return
        synchronized(events) {
            events.add(TelemetryEvent(type = type, data = data))
            if (events.size > MAX_EVENTS) {
                events.removeFirst()
            }
        }
        saveEvents()
    }

    fun track(type: String, vararg pairs: Pair<String, Any>) {
        track(type, mapOf(*pairs))
    }

    fun getEvents(): List<TelemetryEvent> {
        synchronized(events) {
            return events.toList()
        }
    }

    fun getEventsAsJson(): JSONArray {
        val arr = JSONArray()
        synchronized(events) {
            events.forEach { arr.put(it.toJson()) }
        }
        return arr
    }

    fun clear() {
        synchronized(events) {
            events.clear()
        }
        saveEvents()
    }

    private fun saveEvents() {
        val arr = JSONArray()
        synchronized(events) {
            events.forEach { arr.put(it.toJson()) }
        }
        prefs?.edit()?.putString(KEY_EVENTS, arr.toString())?.apply()
    }

    private fun loadEvents() {
        val json = prefs?.getString(KEY_EVENTS, "[]") ?: "[]"
        val arr = JSONArray(json)
        synchronized(events) {
            events.clear()
            for (i in 0 until arr.length()) {
                events.add(TelemetryEvent.fromJson(arr.getJSONObject(i)))
            }
        }
    }
}
