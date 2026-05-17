package com.example.clawdroid

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class LogViewerActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_viewer)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        logTextView = findViewById(R.id.log_text)
        startLogPolling()
    }

    private fun startLogPolling() {
        lifecycleScope.launch {
            while (isActive) {
                updateLogs()
                delay(1000)
            }
        }
    }

    private fun updateLogs() {
        val app = application as App
        val logFile = app.terminalManager.getLogFile()
        if (logFile.exists()) {
            val logs = logFile.readText().takeLast(5000)
            logTextView.text = logs
        } else {
            logTextView.text = getString(R.string.error_logs_empty)
        }
    }
}
