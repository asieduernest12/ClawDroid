package com.example.clawdroid.config

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.clawdroid.config.model.LogLevel
import com.example.clawdroid.databinding.ActivityConfigBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConfigActivity : AppCompatActivity() {

    lateinit var binding: ActivityConfigBinding
        private set
    lateinit var viewModel: ConfigViewModel
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = ConfigRepository(applicationContext)
        viewModel = ConfigViewModel(repository)

        setupLogLevelSpinner()
        observeViewModel()
        setupListeners()
    }

    private fun setupLogLevelSpinner() {
        val levels = LogLevel.entries.map { it.displayName }.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.logLevelSpinner.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    with(binding) {
                        if (binaryPathInput.text.toString() != state.config.binaryPath) {
                            binaryPathInput.setText(state.config.binaryPath)
                        }
                        if (configDirInput.text.toString() != state.config.configDir) {
                            configDirInput.setText(state.config.configDir)
                        }
                        if (serverPortInput.text.toString() != state.config.serverPort.toString()) {
                            serverPortInput.setText(state.config.serverPort.toString())
                        }
                        autoStartSwitch.isChecked = state.config.autoStart
                        logLevelSpinner.setSelection(state.config.logLevel.ordinal)

                        binaryPathLayout.error = state.fieldErrors["binaryPath"]
                        configDirLayout.error = state.fieldErrors["configDir"]
                        serverPortLayout.error = state.fieldErrors["serverPort"]

                        savedIndicator.visibility = if (state.isSaved) {
                            android.view.View.VISIBLE
                        } else {
                            android.view.View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            saveButton.setOnClickListener {
                viewModel.save()
            }

            resetButton.setOnClickListener {
                viewModel.resetToDefaults()
                Toast.makeText(this@ConfigActivity, "Defaults restored", Toast.LENGTH_SHORT).show()
            }

            binaryPathInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.updateBinaryPath(binaryPathInput.text?.toString() ?: "")
                }
            }

            configDirInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.updateConfigDir(configDirInput.text?.toString() ?: "")
                }
            }

            serverPortInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val port = serverPortInput.text?.toString()?.toIntOrNull() ?: 8080
                    viewModel.updateServerPort(port)
                }
            }

            autoStartSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAutoStart(isChecked)
            }

            logLevelSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    viewModel.updateLogLevel(LogLevel.entries[position])
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        }
    }
}
