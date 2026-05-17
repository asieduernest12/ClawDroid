package com.example.clawdroid

import android.content.Intent
import android.content.ActivityNotFoundException
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.clawdroid.config.ConfigActivity
import com.example.clawdroid.terminal.TermuxBootstrapState
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.TextView
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chipBootstrap: Chip
    private lateinit var chipPicoclaw: Chip
    private lateinit var chipServer: Chip
    private lateinit var bootstrapProgress: ProgressBar
    private lateinit var errorContainer: MaterialCardView
    private lateinit var errorText: TextView
    private lateinit var btnRetry: MaterialButton
    private lateinit var fabAction: FloatingActionButton
    private lateinit var btnMissionControl: MaterialButton
    private lateinit var btnViewLogs: MaterialButton
    private lateinit var btnProviders: MaterialButton
    private lateinit var btnRestart: MaterialButton
    private lateinit var btnSettings: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupToolbar()
        setupClickListeners()
        observeBootstrapState()
        observeServerStatus()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        chipBootstrap = findViewById(R.id.chip_bootstrap)
        chipPicoclaw = findViewById(R.id.chip_picoclaw)
        chipServer = findViewById(R.id.chip_server)
        bootstrapProgress = findViewById(R.id.bootstrap_progress)
        errorContainer = findViewById(R.id.error_container)
        errorText = findViewById(R.id.error_text)
        btnRetry = findViewById(R.id.btn_retry)
        fabAction = findViewById(R.id.fab_action)
        btnMissionControl = findViewById(R.id.btn_mission_control)
        btnViewLogs = findViewById(R.id.btn_view_logs)
        btnProviders = findViewById(R.id.btn_providers)
        btnRestart = findViewById(R.id.btn_restart)
        btnSettings = findViewById(R.id.btn_settings)
    }

    private fun setupToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_help -> {
                    showHelpDialog()
                    true
                }
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        fabAction.setOnClickListener {
            val app = application as App
            if (app.processMonitor.processState.value.running) {
                stopPicoClaw()
            } else {
                startPicoClaw()
            }
        }

        btnRetry.setOnClickListener {
            retryBootstrap()
        }

        btnMissionControl.setOnClickListener {
            openMissionControl()
        }

        btnViewLogs.setOnClickListener {
            viewLogs()
        }

        btnProviders.setOnClickListener {
            val intent = Intent(this, ProviderListActivity::class.java)
            startActivity(intent)
        }

        btnRestart.setOnClickListener {
            restartPicoClaw()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeBootstrapState() {
        val app = application as App
        lifecycleScope.launch {
            app.bootstrapState.collect { state ->
                when (state) {
                    is TermuxBootstrapState.Uninitialized -> {
                        updateBootstrapChip(getString(R.string.status_bootstrap_pending), R.color.status_offline)
                        bootstrapProgress.isVisible = true
                        hideError()
                    }
                    is TermuxBootstrapState.Checking -> {
                        updateBootstrapChip(getString(R.string.status_bootstrap_checking), R.color.status_loading)
                        bootstrapProgress.isVisible = true
                        hideError()
                    }
                    is TermuxBootstrapState.Extracting -> {
                        updateBootstrapChip(getString(R.string.status_bootstrap_extracting), R.color.status_loading)
                        bootstrapProgress.isVisible = true
                        hideError()
                    }
                    is TermuxBootstrapState.Ready -> {
                        updateBootstrapChip(getString(R.string.status_bootstrap_ready), R.color.status_running)
                        bootstrapProgress.isVisible = false
                        hideError()
                    }
                    is TermuxBootstrapState.Error -> {
                        updateBootstrapChip(getString(R.string.status_bootstrap_error), R.color.status_error)
                        bootstrapProgress.isVisible = false
                        showError(getString(R.string.error_bootstrap_failed))
                    }
                }
            }
        }
    }

    private fun observeServerStatus() {
        val app = application as App
        lifecycleScope.launch {
            app.processMonitor.processState.collect { state ->
                updatePicoclawChip(state.running)
                updateFab(state.running)
            }
        }

        lifecycleScope.launch {
            app.serverManager.status.collect { status ->
                updateServerChip(status.serverPort)
            }
        }
    }

    private fun updateBootstrapChip(text: String, colorRes: Int) {
        chipBootstrap.text = text
        chipBootstrap.setChipBackgroundColorResource(colorRes)
        chipBootstrap.setTextColor(getChipTextColor(colorRes))
    }

    private fun updatePicoclawChip(isRunning: Boolean) {
        if (isRunning) {
            chipPicoclaw.text = getString(R.string.status_picoclaw_running)
            chipPicoclaw.setChipBackgroundColorResource(R.color.status_running)
            chipPicoclaw.setTextColor(Color.WHITE)
        } else {
            chipPicoclaw.text = getString(R.string.status_picoclaw_stopped)
            chipPicoclaw.setChipBackgroundColorResource(R.color.status_stopped)
            chipPicoclaw.setTextColor(Color.WHITE)
        }
    }

    private fun updateServerChip(port: Int) {
        if (port > 0) {
            chipServer.text = "Port $port"
            chipServer.setChipBackgroundColorResource(R.color.status_online)
            chipServer.setTextColor(Color.WHITE)
        } else {
            chipServer.text = getString(R.string.status_server_offline)
            chipServer.setChipBackgroundColorResource(R.color.status_offline)
            chipServer.setTextColor(Color.WHITE)
        }
    }

    private fun updateFab(isRunning: Boolean) {
        if (isRunning) {
            fabAction.setImageResource(android.R.drawable.ic_media_pause)
            fabAction.contentDescription = getString(R.string.fab_stop_desc)
            fabAction.backgroundTintList = getColorStateList(R.color.status_stopped)
        } else {
            fabAction.setImageResource(android.R.drawable.ic_media_play)
            fabAction.contentDescription = getString(R.string.fab_start_desc)
            fabAction.backgroundTintList = getColorStateList(R.color.status_running)
        }
    }

    private fun getChipTextColor(colorRes: Int): Int {
        return when (colorRes) {
            R.color.status_running, R.color.status_stopped, R.color.status_error, R.color.status_online -> Color.WHITE
            else -> getColor(android.R.color.black)
        }
    }

    private fun showError(message: String) {
        errorText.text = message
        errorContainer.isVisible = true
    }

    private fun hideError() {
        errorContainer.isVisible = false
    }

    private fun startPicoClaw() {
        val app = application as App
        if (app.bootstrapState.value !is TermuxBootstrapState.Ready) {
            showError(getString(R.string.error_picoclaw_not_ready))
            return
        }

        updatePicoclawChip(true)
        chipPicoclaw.text = getString(R.string.status_picoclaw_starting)
        chipPicoclaw.setChipBackgroundColorResource(R.color.status_loading)

        lifecycleScope.launch {
            val launched = app.terminalManager.launchPicoClaw()
            if (!launched) {
                Toast.makeText(this@MainActivity, "Failed to start PicoClaw", Toast.LENGTH_SHORT).show()
                updatePicoclawChip(false)
            }
        }
    }

    private fun stopPicoClaw() {
        chipPicoclaw.text = getString(R.string.status_picoclaw_stopping)
        chipPicoclaw.setChipBackgroundColorResource(R.color.status_loading)

        lifecycleScope.launch {
            val app = application as App
            app.terminalManager.stopPicoClaw()
            Toast.makeText(this@MainActivity, "PicoClaw stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restartPicoClaw() {
        Toast.makeText(this, "Restarting PicoClaw...", Toast.LENGTH_SHORT).show()
        stopPicoClaw()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startPicoClaw()
        }, 1000)
    }

    private fun retryBootstrap() {
        val app = application as App
        lifecycleScope.launch {
            app.initializeTermux()
        }
    }

    private fun openMissionControl() {
        val app = application as App
        val status = app.serverManager.getStatus()
        if (status.serverPort <= 0) {
            showError(getString(R.string.error_mission_control_offline))
            return
        }

        val intent = Intent(this, MissionControlActivity::class.java)
        intent.putExtra("port", status.serverPort)
        startActivity(intent)
    }

    private fun viewLogs() {
        val app = application as App
        val logFile = app.terminalManager.getLogFile()
        if (logFile.exists()) {
            val intent = Intent(this, LogViewerActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.error_logs_empty), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_welcome_title)
            .setMessage(R.string.dialog_welcome_message)
            .setPositiveButton(R.string.dialog_welcome_button, null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setMessage("Version 0.1.0\n\nClawDroid runs PicoClaw — a private AI assistant on your device.")
            .setPositiveButton("OK", null)
            .show()
    }
}
