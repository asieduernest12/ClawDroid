package com.example.clawdroid.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clawdroid.config.model.ConfigValidationResult
import com.example.clawdroid.config.model.LogLevel
import com.example.clawdroid.config.model.PicoClawConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfigUiState(
    val config: PicoClawConfig = PicoClawConfig(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaved: Boolean = false
)

class ConfigViewModel(
    private val repository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            val config = repository.loadConfig()
            _uiState.value = _uiState.value.copy(config = config)
        }
    }

    fun updateBinaryPath(path: String) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(binaryPath = path),
            fieldErrors = _uiState.value.fieldErrors - "binaryPath",
            isSaved = false
        )
    }

    fun updateConfigDir(path: String) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(configDir = path),
            fieldErrors = _uiState.value.fieldErrors - "configDir",
            isSaved = false
        )
    }

    fun updateServerPort(port: Int) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(serverPort = port),
            fieldErrors = _uiState.value.fieldErrors - "serverPort",
            isSaved = false
        )
    }

    fun updateAutoStart(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(autoStart = enabled),
            isSaved = false
        )
    }

    fun updateLogLevel(level: LogLevel) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(logLevel = level),
            isSaved = false
        )
    }

    fun save() {
        viewModelScope.launch {
            val result = repository.saveConfig(_uiState.value.config)
            when (result) {
                is ConfigValidationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaved = true,
                        fieldErrors = emptyMap()
                    )
                }
                is ConfigValidationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        fieldErrors = result.fieldErrors,
                        isSaved = false
                    )
                }
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            val defaultConfig = repository.resetToDefaults()
            _uiState.value = ConfigUiState(config = defaultConfig, isSaved = true)
        }
    }
}
