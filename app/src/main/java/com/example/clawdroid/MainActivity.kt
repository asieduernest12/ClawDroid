package com.example.clawdroid

import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.clawdroid.config.ConfigActivity
import com.example.clawdroid.terminal.TermuxBootstrapState
import com.example.clawdroid.server.model.ServerStatus
import com.example.clawdroid.server.ServerManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private lateinit var terminalStatusText: TextView
    private lateinit var picoclawStatusText: TextView
    private lateinit var serverInfoText: TextView
    private lateinit var bootstrapProgressBar: ProgressBar
    private lateinit var btnStart: MaterialButton
    private lateinit var btnStop: MaterialButton
    private lateinit var btnRestart: MaterialButton
    private lateinit var btnMissionControl: MaterialButton
    private lateinit var btnViewLogs: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        observeBootstrapState()
        observeServerStatus()
        updateControlButtons(false)
    }

    private fun initializeViews() {
        terminalStatusText = findViewById(R.id.terminal_status_text)
        picoclawStatusText = findViewById(R.id.picoclaw_status_text)
        serverInfoText = findViewById(R.id.server_info_text)
        bootstrapProgressBar = findViewById(R.id.bootstrap_progress_bar)
        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        btnRestart = findViewById(R.id.btn_restart)
        btnMissionControl = findViewById(R.id.btn_mission_control)
        btnViewLogs = findViewById(R.id.btn_view_logs)

        // Settings button (existing functionality)
        findViewById<MaterialButton>(R.id.settings_button)
            .setOnClickListener {
                val intent = Intent(this, ConfigActivity::class.java)
                startActivity(intent)
            }
    }

    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            startPicoClaw()
        }

        btnStop.setOnClickListener {
            stopPicoClaw()
        }

        btnRestart.setOnClickListener {
            restartPicoClaw()
        }

        btnMissionControl.setOnClickListener {
            openMissionControl()
        }

        btnViewLogs.setOnClickListener {
            viewLogs()
        }
    }

    private fun observeBootstrapState() {
        val app = application as App
        lifecycleScope.launch {
            app.bootstrapState.collect { state ->
                when (state) {
                    is TermuxBootstrapState.Uninitialized -> {
                        terminalStatusText.text = getString(R.string.bootstrap_initializing)
                        bootstrapProgressBar.isIndeterminate = true
                        bootstrapProgressBar.visibility = android.view.View.VISIBLE
                    }
                    is TermuxBootstrapState.Checking -> {
                        terminalStatusText.text = getString(R.string.bootstrap_checking)
                        bootstrapProgressBar.isIndeterminate = true
                        bootstrapProgressBar.visibility = android.view.View.VISIBLE
                    }
                    is TermuxBootstrapState.Extracting -> {
                        terminalStatusText.text = getString(R.string.bootstrap_extracting)
                        bootstrapProgressBar.isIndeterminate = true
                        bootstrapProgressBar.visibility = android.view.View.VISIBLE
                    }
                    is TermuxBootstrapState.Ready -> {
                        terminalStatusText.text = getString(R.string.bootstrap_ready)
                        bootstrapProgressBar.visibility = android.view.View.GONE
                    }
                    is TermuxBootstrapState.Error -> {
                        terminalStatusText.text = getString(R.string.bootstrap_error, state.message)
                        bootstrapProgressBar.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    private fun observeServerStatus() {
        val app = application as App
        
        // Observe PicoClaw process status
        lifecycleScope.launch {
            app.processMonitor.processState.collect { state ->
                updatePicoclawStatus(state.running)
            }
        }

        // Observe server status
        lifecycleScope.launch {
            app.serverManager.status.collect { status ->
                updateServerInfo(status.serverPort)
            }
        }
    }

    private fun updatePicoclawStatus(isRunning: Boolean) {
        if (isRunning) {
            picoclawStatusText.text = getString(R.string.picoclaw_status_running)
            picoclawStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
            btnStart.isEnabled = false
            btnStop.isEnabled = true
            btnRestart.isEnabled = true
        } else {
            picoclawStatusText.text = getString(R.string.picoclaw_status_stopped)
            picoclawStatusText.setTextColor(getColor(android.R.color.holo_red_dark))
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            btnRestart.isEnabled = false
        }
    }

    private fun updateControlButtons(isRunning: Boolean) {
        updatePicoclawStatus(isRunning)
    }

    private fun updateServerInfo(port: Int) {
        serverInfoText.text = getString(R.string.server_status, port)
    }

    private fun startPicoClaw() {
        val app = application as App
        if (app.bootstrapState.value !is TermuxBootstrapState.Ready) {
            Toast.makeText(this, "Termux environment not ready", Toast.LENGTH_SHORT).show()
            return
        }

        picoclawStatusText.text = getString(R.string.picoclaw_status_starting)
        btnStart.isEnabled = false
        btnStop.isEnabled = false
        btnRestart.isEnabled = false

        lifecycleScope.launch {
            val launched = app.terminalManager.launchPicoClaw()
            if (launched) {
                Toast.makeText(this@MainActivity, "PicoClaw started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to start PicoClaw", Toast.LENGTH_SHORT).show()
                updatePicoclawStatus(false)
            }
        }
    }

    private fun stopPicoClaw() {
        picoclawStatusText.text = getString(R.string.picoclaw_status_stopping)
        btnStart.isEnabled = false
        btnStop.isEnabled = false
        btnRestart.isEnabled = false

        lifecycleScope.launch {
            val app = application as App
            app.terminalManager.stopPicoClaw()
            Toast.makeText(this@MainActivity, "PicoClaw stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restartPicoClaw() {
        Toast.makeText(this, "Restarting PicoClaw...", Toast.LENGTH_SHORT).show()
        stopPicoClaw()
        // Add a small delay before starting again
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startPicoClaw()
        }, 1000)
    }

    private fun openMissionControl() {
        val app = application as App
        val status = app.serverManager.getStatus()
        val url = "http://localhost:${status.serverPort}"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // If no browser is found, try to open with a chooser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val chooser = Intent.createChooser(intent, getString(R.string.mission_control_hint))
            startActivity(chooser)
        }
    }

    private fun viewLogs() {
        val app = application as App
        val logFile = app.terminalManager.getLogFile()
        
        if (logFile.exists()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(logFile), "text/plain")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open logs: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "No logs available", Toast.LENGTH_SHORT).show()
        }
    }
}
