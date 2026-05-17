package com.example.clawdroid

import android.app.Application
import android.util.Log
import com.example.clawdroid.server.ServerManager
import com.example.clawdroid.terminal.EmbeddedTermuxSession
import com.example.clawdroid.terminal.ProcessMonitor
import com.example.clawdroid.terminal.TerminalManager
import com.example.clawdroid.terminal.TermuxBootstrapManager
import com.example.clawdroid.terminal.TermuxBootstrapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class App : Application() {

    lateinit var serverManager: ServerManager
        private set
    lateinit var terminalManager: TerminalManager
        private set
    lateinit var processMonitor: ProcessMonitor
        private set

    lateinit var bootstrapManager: TermuxBootstrapManager
        private set

    private val _bootstrapState = MutableStateFlow<TermuxBootstrapState>(TermuxBootstrapState.Uninitialized)
    val bootstrapState: StateFlow<TermuxBootstrapState> = _bootstrapState.asStateFlow()

    private var picoclawSession: EmbeddedTermuxSession? = null

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        bootstrapManager = TermuxBootstrapManager(this)
        terminalManager = TerminalManager(this)
        processMonitor = ProcessMonitor(appScope)
        serverManager = ServerManager(this)

        initializeTermux()
        serverManager.start()
    }

    fun initializeTermux() {
        appScope.launch {
            _bootstrapState.value = TermuxBootstrapState.Checking

            bootstrapManager.install().collect { progress ->
                _bootstrapState.value = when (progress) {
                    is TermuxBootstrapManager.InstallProgress.Extracting ->
                        TermuxBootstrapState.Extracting
                    is TermuxBootstrapManager.InstallProgress.Completed -> {
                        extractPicoClawBinary()
                        TermuxBootstrapState.Ready
                    }
                    is TermuxBootstrapManager.InstallProgress.Failed ->
                        TermuxBootstrapState.Error(progress.error)
                }
            }
        }
    }

    private fun extractPicoClawBinary() {
        val targetDir = File(filesDir, "picoclaw")
        val targetFile = File(targetDir, "picoclaw-arm64")

        if (targetFile.exists()) {
            Log.d(TAG, "PicoClaw binary already extracted")
            return
        }

        try {
            targetDir.mkdirs()
            assets.open("picoclaw/picoclaw-arm64").use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.setExecutable(true)
            Log.d(TAG, "Extracted PicoClaw binary to ${targetFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract PicoClaw binary", e)
        }
    }

    fun createPicoClawSession(): EmbeddedTermuxSession {
        picoclawSession?.stop()
        val env = bootstrapManager.getEnv()
        val logFile = File(filesDir, "picoclaw.log")
        val session = EmbeddedTermuxSession("picoclaw", env, appScope, logFile)
        picoclawSession = session
        return session
    }

    fun getPicoClawSession(): EmbeddedTermuxSession? = picoclawSession

    override fun onTerminate() {
        super.onTerminate()
        picoclawSession?.stop()
        if (::serverManager.isInitialized) serverManager.destroy()
        if (::processMonitor.isInitialized) processMonitor.stopPolling()
    }

    companion object {
        lateinit var instance: App
            private set
        private const val TAG = "App"
    }
}
