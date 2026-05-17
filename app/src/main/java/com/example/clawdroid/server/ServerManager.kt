package com.example.clawdroid.server

import android.content.Context
import android.util.Log
import com.example.clawdroid.App
import com.example.clawdroid.config.ConfigRepository
import com.example.clawdroid.server.model.ServerStatus
import com.example.clawdroid.terminal.TermuxBootstrapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class ServerManager(private val context: Context) {

    private var server: MissionControlServer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _status = MutableStateFlow(ServerStatus())
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    companion object {
        private const val TAG = "ServerManager"
    }

    val isRunning: Boolean get() = server?.isAlive == true

    fun start() {
        if (isRunning) {
            Log.d(TAG, "Server already running")
            return
        }

        scope.launch {
            try {
                val picoclawConfig = ConfigRepository(context).loadConfig()
                val configuredPort = picoclawConfig.serverPort

                val srv = MissionControlServer(configuredPort, context)
                srv.onStartRequested = ::handleStartRequest
                srv.onStopRequested = ::handleStopRequest
                server = srv
                srv.start()
                Log.d(TAG, "Server started on configured port $configuredPort")
                _status.update { it.copy(serverPort = configuredPort) }
            } catch (e: IOException) {
                Log.w(TAG, "Port conflict on configured port. Trying fallback...")
                try {
                    val fallbackPort = 0
                    val srv = MissionControlServer(fallbackPort, context)
                    srv.onStartRequested = ::handleStartRequest
                    srv.onStopRequested = ::handleStopRequest
                    server = srv
                    srv.start()
                    Log.i(TAG, "Server started on fallback port ${srv.listeningPort}")
                    _status.update { it.copy(serverPort = srv.listeningPort) }
                } catch (fallbackError: Exception) {
                    Log.e(TAG, "Failed to start server even on fallback port: ${fallbackError.message}", fallbackError)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server: ${e.message}", e)
            }
        }
    }

    private fun handleStartRequest(): Boolean {
        val app = context.applicationContext as App

        if (app.bootstrapState.value !is TermuxBootstrapState.Ready) {
            Log.w(TAG, "Cannot start PicoClaw: Termux not ready")
            return false
        }

        val launched = app.terminalManager.launchPicoClaw()
        if (launched) {
            app.processMonitor.markRunning()
            server?.setPicoClawStatus(true)
            _status.update { it.copy(nanoClawRunning = true) }
        }
        return launched
    }

    private fun handleStopRequest(): Boolean {
        val app = context.applicationContext as App
        app.terminalManager.stopPicoClaw()
        app.processMonitor.markStopped()
        server?.setPicoClawStatus(false)
        _status.update { it.copy(nanoClawRunning = false) }
        return true
    }

    fun stop() {
        if (!isRunning) return

        scope.launch {
            try {
                server?.stop()
                server = null
                Log.d(TAG, "Server stopped")
                _status.update { ServerStatus() }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server: ${e.message}", e)
            }
        }
    }

    fun getStatus(): ServerStatus {
        val serverStatus = server?.getCurrentStatus() ?: ServerStatus()
        val app = context.applicationContext as App
        val processState = app.processMonitor.processState.value
        return serverStatus.copy(nanoClawRunning = processState.running)
    }

    fun setPicoClawStatus(running: Boolean) {
        server?.setPicoClawStatus(running)
    }

    fun destroy() {
        stop()
        scope.cancel()
    }
}
