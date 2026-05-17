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

        val config = ConfigRepository(context).loadConfig()
        val binaryPath = config.binaryPath
        val workDir = binaryPath.substringBeforeLast("/")

        val session = app.createPicoClawSession()
        val launched = session.start(
            command = binaryPath,
            args = listOf("onboard"),
            workingDir = workDir
        )

        if (launched) {
            Log.d(TAG, "Launched PicoClaw in embedded Termux")
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
