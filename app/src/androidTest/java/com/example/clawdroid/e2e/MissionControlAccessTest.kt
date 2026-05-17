package com.example.clawdroid.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.config.ConfigRepository
import com.example.clawdroid.config.model.PicoClawConfig
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@RunWith(AndroidJUnit4::class)
class MissionControlAccessTest {

    @Test
    fun serverHealthEndpointReturnsOk() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = ConfigRepository(context).loadConfig()
        val port = config.serverPort

        val url = URL("http://127.0.0.1:$port/api/health")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000

        assertEquals(200, conn.responseCode)

        val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        val json = JSONObject(body)
        assertEquals("ok", json.getString("status"))
        assertTrue(json.getLong("uptime") >= 0)
    }

    @Test
    fun serverStatusEndpointReturnsData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = ConfigRepository(context).loadConfig()
        val port = config.serverPort

        val url = URL("http://127.0.0.1:$port/api/status")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000

        assertEquals(200, conn.responseCode)

        val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        val json = JSONObject(body)
        assertTrue(json.has("status"))
        assertTrue(json.has("uptimeSeconds"))
        assertTrue(json.has("nanoClawRunning"))
        assertTrue(json.has("port"))
    }

    @Test
    fun serverServesDashboardHtml() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = ConfigRepository(context).loadConfig()
        val port = config.serverPort

        val url = URL("http://127.0.0.1:$port/")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000

        assertEquals(200, conn.responseCode)

        val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        assertTrue(body.contains("Mission Control") || body.contains("PicoClaw"))
    }
}
