package com.example.clawdroid.config

import android.content.Context
import android.content.SharedPreferences
import com.example.clawdroid.config.model.ConfigValidationResult
import com.example.clawdroid.config.model.LogLevel
import com.example.clawdroid.config.model.PicoClawConfig

class ConfigRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): PicoClawConfig {
        return PicoClawConfig(
            binaryPath = prefs.getString(KEY_BINARY_PATH, PicoClawConfig().binaryPath)!!,
            configDir = prefs.getString(KEY_CONFIG_DIR, PicoClawConfig().configDir)!!,
            serverPort = prefs.getInt(KEY_SERVER_PORT, PicoClawConfig().serverPort),
            autoStart = prefs.getBoolean(KEY_AUTO_START, PicoClawConfig().autoStart),
            logLevel = LogLevel.valueOf(
                prefs.getString(KEY_LOG_LEVEL, PicoClawConfig().logLevel.name)!!
            )
        )
    }

    fun saveConfig(config: PicoClawConfig): ConfigValidationResult {
        val validation = validateConfig(config)
        if (validation is ConfigValidationResult.Error) {
            return validation
        }

        prefs.edit()
            .putString(KEY_BINARY_PATH, config.binaryPath)
            .putString(KEY_CONFIG_DIR, config.configDir)
            .putInt(KEY_SERVER_PORT, config.serverPort)
            .putBoolean(KEY_AUTO_START, config.autoStart)
            .putString(KEY_LOG_LEVEL, config.logLevel.name)
            .apply()

        return ConfigValidationResult.Success
    }

    fun resetToDefaults(): PicoClawConfig {
        prefs.edit().clear().apply()
        return PicoClawConfig()
    }

    private fun validateConfig(config: PicoClawConfig): ConfigValidationResult {
        val errors = mutableMapOf<String, String>()

        if (config.binaryPath.isBlank()) {
            errors["binaryPath"] = "Binary path cannot be empty"
        }
        if (config.configDir.isBlank()) {
            errors["configDir"] = "Config directory cannot be empty"
        }
        if (config.serverPort < 1024 || config.serverPort > 65535) {
            errors["serverPort"] = "Port must be between 1024 and 65535"
        }

        return if (errors.isEmpty()) {
            ConfigValidationResult.Success
        } else {
            ConfigValidationResult.Error(errors)
        }
    }

    companion object {
        private const val PREFS_NAME = "clawdroid_config"
        private const val KEY_BINARY_PATH = "picoclaw_binary_path"
        private const val KEY_CONFIG_DIR = "picoclaw_config_dir"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_LOG_LEVEL = "log_level"
    }
}
