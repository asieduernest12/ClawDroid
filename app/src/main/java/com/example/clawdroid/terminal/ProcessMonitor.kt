package com.example.clawdroid.terminal

import android.util.Log
import com.example.clawdroid.App
import com.example.clawdroid.terminal.model.PicoClawProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ProcessMonitor(private val scope: CoroutineScope) {

    private val _processState = MutableStateFlow(PicoClawProcess())
    val processState: StateFlow<PicoClawProcess> = _processState.asStateFlow()

    private var pollingJob: Job? = null

    fun markRunning() {
        _processState.value = PicoClawProcess(pid = 0, running = true)
        startPolling()
    }

    fun markStopped() {
        _processState.value = PicoClawProcess()
        stopPolling()
    }

    fun startPolling(intervalMs: Long = 2000) {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                val current = _processState.value
                if (current.running) {
                    val running = checkSessionAlive()
                    if (!running) {
                        Log.d(TAG, "PicoClaw process no longer alive")
                        _processState.value = current.copy(running = false)
                        stopPolling()
                    }
                }
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun checkSessionAlive(): Boolean {
        return try {
            val app = App.instance
            app.getPicoClawSession()?.isRunning?.value ?: false
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "ProcessMonitor"
    }
}
