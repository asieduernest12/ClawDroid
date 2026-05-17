package com.example.clawdroid.server

import android.content.Context
import android.util.Log
import com.example.clawdroid.server.model.ServerStatus
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.InputStream

class MissionControlServer(
    port: Int,
    private val context: Context
) : NanoHTTPD(port) {

    private var startTime = System.currentTimeMillis()
    private var picoClawRunning = false

    var onStartRequested: (() -> Boolean)? = null
    var onStopRequested: (() -> Boolean)? = null

    fun setPicoClawStatus(running: Boolean) {
        picoClawRunning = running
    }

    fun getCurrentStatus(): ServerStatus {
        val uptime = (System.currentTimeMillis() - startTime) / 1000
        return ServerStatus(
            status = if (isAlive()) "running" else "stopped",
            uptimeSeconds = uptime,
            nanoClawRunning = picoClawRunning,
            serverPort = listeningPort
        )
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        return when {
            uri == "/api/health" && method == Method.GET -> handleHealth()
            uri == "/api/status" && method == Method.GET -> handleStatus()
            uri == "/api/start" && method == Method.POST -> handleStart()
            uri == "/api/stop" && method == Method.POST -> handleStop()
            uri.startsWith("/api/") -> newFixedLengthResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found"
            )
            else -> serveStaticFile(uri)
        }
    }

    private fun handleHealth(): Response {
        val json = JSONObject().apply {
            put("status", if (isAlive()) "ok" else "error")
            put("uptime", (System.currentTimeMillis() - startTime) / 1000)
        }
        return jsonResponse(json)
    }

    private fun handleStatus(): Response {
        val status = getCurrentStatus()
        val json = JSONObject().apply {
            put("status", status.status)
            put("uptimeSeconds", status.uptimeSeconds)
            put("nanoClawRunning", status.nanoClawRunning)
            put("port", status.port)
        }
        return jsonResponse(json)
    }

    private fun handleStart(): Response {
        val launched = onStartRequested?.invoke() ?: false
        picoClawRunning = launched
        Log.d(TAG, "PicoClaw start requested, launched=$launched")
        val json = JSONObject().apply {
            put("success", launched)
            put("message", if (launched) "PicoClaw started" else "Failed to start PicoClaw")
        }
        return jsonResponse(json)
    }

    private fun handleStop(): Response {
        val stopped = onStopRequested?.invoke() ?: false
        picoClawRunning = !stopped
        Log.d(TAG, "PicoClaw stop requested, stopped=$stopped")
        val json = JSONObject().apply {
            put("success", stopped)
            put("message", if (stopped) "PicoClaw stopped" else "Failed to stop PicoClaw")
        }
        return jsonResponse(json)
    }

    private fun serveStaticFile(uri: String): Response {
        val path = if (uri == "/") "mission-control/index.html"
                   else "mission-control$uri"

        val mime = when {
            path.endsWith(".html") -> "text/html"
            path.endsWith(".css") -> "text/css"
            path.endsWith(".js") -> "application/javascript"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".svg") -> "image/svg+xml"
            path.endsWith(".json") -> "application/json"
            else -> MIME_PLAINTEXT
        }

        val stream: InputStream? = try {
            context.assets.open(path)
        } catch (e: Exception) {
            null
        }

        return if (stream != null) {
            newChunkedResponse(Response.Status.OK, mime, stream)
        } else {
            newFixedLengthResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                "<html><body><h1>404 Not Found</h1></body></html>"
            )
        }
    }

    private fun jsonResponse(json: JSONObject): Response {
        return newFixedLengthResponse(
            Response.Status.OK, "application/json", json.toString()
        )
    }

    companion object {
        private const val TAG = "MissionControlServer"
    }
}
