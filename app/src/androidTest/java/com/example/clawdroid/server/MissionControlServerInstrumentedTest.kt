package com.example.clawdroid.server

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@RunWith(AndroidJUnit4::class)
class MissionControlServerInstrumentedTest {

    private lateinit var server: MissionControlServer
    private lateinit var context: Context
    private var port = 0

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        port = 8765
        server = MissionControlServer(port, context)

        server.onStartRequested = { true }
        server.onStopRequested = { true }

        server.start()
        Thread.sleep(500)
    }

    @After
    fun tearDown() {
        server.stop()
    }

    @Test
    fun healthEndpointReturnsOk() {
        val response = httpGet("/api/health")
        assertTrue(response.contains("ok"))
        assertTrue(response.contains("uptime"))
    }

    @Test
    fun statusEndpointReturnsStatus() {
        val response = httpGet("/api/status")
        assertTrue(response.contains("running"))
        assertTrue(response.contains("uptimeSeconds"))
    }

    @Test
    fun postStartReturnsSuccess() {
        val response = httpPost("/api/start")
        assertTrue(response.contains("true"))
    }

    @Test
    fun postStopReturnsSuccess() {
        val response = httpPost("/api/stop")
        assertTrue(response.contains("true"))
    }

    @Test
    fun serverServesIndexHtml() {
        val response = httpGet("/")
        assertTrue(response.contains("Mission Control"))
    }

    private fun httpGet(path: String): String {
        val url = URL("http://127.0.0.1:$port$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 2000
        conn.readTimeout = 2000
        assertEquals(200, conn.responseCode)

        return BufferedReader(InputStreamReader(conn.inputStream))
            .readText()
    }

    private fun httpPost(path: String): String {
        val url = URL("http://127.0.0.1:$port$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 2000
        conn.readTimeout = 2000
        conn.doOutput = true
        assertEquals(200, conn.responseCode)

        return BufferedReader(InputStreamReader(conn.inputStream))
            .readText()
    }
}
