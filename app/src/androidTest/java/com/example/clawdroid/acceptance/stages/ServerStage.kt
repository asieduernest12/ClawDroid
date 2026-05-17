package com.example.clawdroid.acceptance.stages

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.clawdroid.config.ConfigRepository
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ServerStage {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val config = ConfigRepository(context).loadConfig()
    private val baseUrl: String get() = "http://127.0.0.1:${config.serverPort}"

    fun serverIsRunning(): Boolean {
        return try {
            val response = httpGet("/api/health")
            response.contains("ok")
        } catch (e: Exception) {
            false
        }
    }

    fun waitForServer(timeoutMs: Long = 5000): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (serverIsRunning()) return true
            Thread.sleep(300)
        }
        return serverIsRunning()
    }

    fun healthEndpointReturnsOk(): Boolean {
        val response = httpGet("/api/health")
        val json = JSONObject(response)
        return json.getString("status") == "ok" && json.has("uptime")
    }

    fun statusEndpointHasAllFields(): Boolean {
        val response = httpGet("/api/status")
        val json = JSONObject(response)
        return json.has("status") &&
                json.has("uptimeSeconds") &&
                json.has("nanoClawRunning") &&
                json.has("port")
    }

    fun dashboardIsServed(): Boolean {
        val response = httpGet("/")
        return response.contains("Mission Control")
    }

    fun postStartPicoClaw(): Boolean {
        val response = httpPost("/api/start")
        val json = JSONObject(response)
        return json.getBoolean("success")
    }

    fun postStopPicoClaw(): Boolean {
        val response = httpPost("/api/stop")
        val json = JSONObject(response)
        return json.getBoolean("success")
    }

    fun isPicoClawRunning(): Boolean {
        val response = httpGet("/api/status")
        val json = JSONObject(response)
        return json.getBoolean("nanoClawRunning")
    }

    private fun httpGet(path: String): String {
        val url = URL("$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 2000
        conn.readTimeout = 2000
        conn.inputStream.use { input ->
            return BufferedReader(InputStreamReader(input)).readText()
        }
    }

    private fun httpPost(path: String): String {
        val url = URL("$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 2000
        conn.readTimeout = 2000
        conn.doOutput = true
        conn.inputStream.use { input ->
            return BufferedReader(InputStreamReader(input)).readText()
        }
    }
}
