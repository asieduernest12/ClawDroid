package com.example.clawdroid.terminal

import android.content.Context
import android.util.Log
import com.example.clawdroid.App
import com.example.clawdroid.config.ConfigRepository
import com.example.clawdroid.terminal.model.TerminalType

class TerminalManager(private val context: Context) {

    fun detectTerminal(): TerminalType {
        val app = context.applicationContext as App
        return if (app.bootstrapState.value is TermuxBootstrapState.Ready) {
            TerminalType.EMBEDDED
        } else {
            TerminalType.NONE
        }
    }

    fun launchPicoClaw(): Boolean {
        val app = context.applicationContext as App
        val state = app.bootstrapState.value

        if (state !is TermuxBootstrapState.Ready) {
            Log.w(TAG, "Cannot launch PicoClaw: Termux not ready (state=$state)")
            return false
        }

        val binaryPath = app.getPicoClawBinaryPath()
        if (binaryPath == null) {
            Log.e(TAG, "No PicoClaw binary found for this architecture")
            return false
        }

        val workDir = binaryPath.substringBeforeLast("/")
        val configFile = java.io.File(workDir, "config.json")

        // Bootstrap: run onboard if no config exists
        if (!configFile.exists()) {
            Log.d(TAG, "No config found, running picoclaw onboard...")
            val onboardSession = app.createPicoClawSession()
            onboardSession.start(
                command = binaryPath,
                args = listOf("onboard"),
                workingDir = workDir
            )
            // Wait briefly for onboard to complete (it's fast)
            Thread.sleep(3000)
            onboardSession.stop()
        }

        // Start gateway as persistent process
        val session = app.createPicoClawSession()
        val launched = session.start(
            command = binaryPath,
            args = listOf("gateway", "--allow-empty"),
            workingDir = workDir
        )

        if (launched) {
            Log.d(TAG, "Launched PicoClaw gateway: $binaryPath")
        } else {
            Log.w(TAG, "Failed to launch PicoClaw session")
        }

        return launched
    }

    fun stopPicoClaw(): Boolean {
        val app = context.applicationContext as App
        val session = app.getPicoClawSession() ?: return false
        session.stop()
        Log.d(TAG, "PicoClaw session stopped")
        return true
    }

    fun isPicoClawRunning(): Boolean {
        val app = context.applicationContext as App
        return app.getPicoClawSession()?.isRunning?.value ?: false
    }

    fun getBootstrapState(): TermuxBootstrapState {
        val app = context.applicationContext as App
        return app.bootstrapState.value
    }

    fun getLogFile() = java.io.File(context.filesDir, "picoclaw.log")

    companion object {
        private const val TAG = "TerminalManager"
    }
}
