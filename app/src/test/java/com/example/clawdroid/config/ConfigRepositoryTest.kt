package com.example.clawdroid.config

import android.content.Context
import android.content.SharedPreferences
import com.example.clawdroid.config.model.ConfigValidationResult
import com.example.clawdroid.config.model.LogLevel
import com.example.clawdroid.config.model.PicoClawConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var repository: ConfigRepository

    @Before
    fun setUp() {
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putInt(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.clear()).thenReturn(mockEditor)

        repository = ConfigRepository(mockContext)
    }

    @Test
    fun saveConfig_validConfig_returnsSuccess() {
        val config = PicoClawConfig(
            binaryPath = "/data/test/picoclaw",
            configDir = "/data/test/picoclaw",
            serverPort = 9090,
            autoStart = true,
            logLevel = LogLevel.DEBUG
        )

        val result = repository.saveConfig(config)

        assert(result is ConfigValidationResult.Success)
        verify(mockEditor).apply()
    }

    @Test
    fun saveConfig_emptyBinaryPath_returnsError() {
        val config = PicoClawConfig(binaryPath = "")
        val result = repository.saveConfig(config)
        assert(result is ConfigValidationResult.Error)
        assert((result as ConfigValidationResult.Error).fieldErrors.containsKey("binaryPath"))
    }

    @Test
    fun saveConfig_invalidPort_returnsError() {
        val config = PicoClawConfig(serverPort = 80)
        val result = repository.saveConfig(config)
        assert(result is ConfigValidationResult.Error)
        assert((result as ConfigValidationResult.Error).fieldErrors.containsKey("serverPort"))
    }

    @Test
    fun saveConfig_emptyConfigDir_returnsError() {
        val config = PicoClawConfig(configDir = "")
        val result = repository.saveConfig(config)
        assert(result is ConfigValidationResult.Error)
        assert((result as ConfigValidationResult.Error).fieldErrors.containsKey("configDir"))
    }

    @Test
    fun resetToDefaults_returnsDefaultConfig() {
        val config = repository.resetToDefaults()
        verify(mockEditor).clear()
        assert(config.serverPort == 8080)
        assert(!config.autoStart)
    }
}
