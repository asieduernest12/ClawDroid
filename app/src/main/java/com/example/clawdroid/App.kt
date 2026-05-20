package com.example.clawdroid

import android.app.Application
import android.util.Log
import com.example.clawdroid.server.ServerManager
import com.example.clawdroid.telemetry.TelemetryService
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
        TelemetryService.init(this)

        // Seed default providers on first launch (includes OpenRouter dev key)
        seedDefaultProviders()

        initializeTermux()
        serverManager.start()
    }

    private fun seedDefaultProviders() {
        val configManager = com.example.clawdroid.config.ProviderConfigManager(this)
        configManager.ensureConfigExists(defaultProviders = listOf(
            com.example.clawdroid.model.ModelProvider(
                modelName = "OpenRouter Nemotron",
                model = "openrouter/nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
                provider = "openrouter",
                apiKey = BuildConfig.OPENROUTER_API_KEY,
                apiBase = "https://openrouter.ai/api/v1"
            ),
        ))
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
        val arch = detectDeviceArch()
        val assetName = "picoclaw/picoclaw-$arch"
        val targetDir = File(filesDir, "picoclaw")
        val targetFile = File(targetDir, "picoclaw-$arch")

        if (targetFile.exists()) {
            Log.d(TAG, "PicoClaw binary already extracted for $arch")
            return
        }

        // Clean up old binaries for different architectures
        targetDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("picoclaw-") && file.name != targetFile.name) {
                file.delete()
                Log.d(TAG, "Cleaned up old binary: ${file.name}")
            }
        }

        try {
            targetDir.mkdirs()
            assets.open(assetName).use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.setExecutable(true)
            Log.d(TAG, "Extracted PicoClaw binary ($arch) to ${targetFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract PicoClaw binary for architecture $arch", e)
        }
    }

    fun getPicoClawBinaryPath(): String? {
        val arch = detectDeviceArch()
        val file = File(filesDir, "picoclaw/picoclaw-$arch")
        return if (file.exists()) file.absolutePath else null
    }

    private fun detectDeviceArch(): String {
        val supportedAbis = android.os.Build.SUPPORTED_ABIS
        Log.d(TAG, "Device ABIs: ${supportedAbis.joinToString()}")

        // Check for exact matches first
        for (abi in supportedAbis) {
            when {
                abi.equals("arm64-v8a", ignoreCase = true) -> return "arm64"
                abi.equals("x86_64", ignoreCase = true) -> return "x86_64"
                abi.equals("armeabi-v7a", ignoreCase = true) -> return "arm"
                abi.equals("x86", ignoreCase = true) -> return "x86"
            }
        }

        // Fallback to os.arch property
        val arch = System.getProperty("os.arch")?.lowercase() ?: "aarch64"
        return when {
            arch.contains("aarch64") || arch.contains("arm64") -> "arm64"
            arch.contains("x86_64") || arch.contains("amd64") -> "x86_64"
            arch.contains("arm") -> "arm"
            arch.contains("x86") || arch.contains("i686") -> "x86"
            else -> "arm64"
        }
    }

    fun createPicoClawSession(): EmbeddedTermuxSession {
        picoclawSession?.stop()
        val env = bootstrapManager.getEnv().toMutableMap()
        // Point PicoClaw home to the binary's directory so config.json is found
        env["PICOCLAW_HOME"] = File(filesDir, "picoclaw").absolutePath
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
