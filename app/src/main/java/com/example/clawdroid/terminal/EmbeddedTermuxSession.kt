package com.example.clawdroid.terminal

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.File

class EmbeddedTermuxSession(
    val name: String,
    private val env: Map<String, String>,
    private val scope: CoroutineScope,
    private val logFile: File? = null
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _exitCode = MutableStateFlow<Int?>(null)
    val exitCode: StateFlow<Int?> = _exitCode.asStateFlow()

    private val _outputLines = MutableStateFlow<List<String>>(emptyList())
    val outputLines: StateFlow<List<String>> = _outputLines.asStateFlow()

    private var processJob: Job? = null
    private var process: Process? = null
    private var stdinWriter: OutputStreamWriter? = null

    fun start(
        command: String,
        args: List<String> = emptyList(),
        workingDir: String? = null
    ): Boolean {
        if (_isRunning.value) {
            Log.d(TAG, "Session '$name' already running")
            return false
        }

        val fullCommand = if (args.isEmpty()) command else "$command ${args.joinToString(" ")}"

        Log.d(TAG, "Starting session '$name': $fullCommand")
        _isRunning.value = true
        _exitCode.value = null
        _outputLines.value = emptyList()

        processJob = scope.launch(Dispatchers.IO) {
            try {
                val pb = ProcessBuilder("/system/bin/sh", "-c", fullCommand)
                pb.environment().putAll(env)
                pb.redirectErrorStream(true)
                if (workingDir != null) {
                    pb.directory(File(workingDir))
                }

                process = pb.start()
                stdinWriter = OutputStreamWriter(process!!.outputStream)
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))

                var line: String?
                while (reader.readLine().also { line = it } != null && isActive) {
                    _outputLines.value = _outputLines.value + line!!
                    logFile?.appendText("${line!!}\n")
                }

                val code = process!!.waitFor()
                _exitCode.value = code
                Log.d(TAG, "Session '$name' exited with code $code")
            } catch (e: Exception) {
                Log.e(TAG, "Session '$name' error", e)
            } finally {
                _isRunning.value = false
                process?.destroy()
                process = null
            }
        }

        return true
    }

    fun stop() {
        Log.d(TAG, "Stopping session '$name'")
        stdinWriter?.close()
        stdinWriter = null
        process?.destroy()
        processJob?.cancel()
        processJob = null
        process = null
        _isRunning.value = false
    }

    fun sendInput(input: String) {
        try {
            stdinWriter?.write("$input\n")
            stdinWriter?.flush()
            Log.d(TAG, "Sent input to session '$name': $input")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send input to session '$name'", e)
        }
    }

    companion object {
        private const val TAG = "EmbeddedTermuxSession"
    }
}
