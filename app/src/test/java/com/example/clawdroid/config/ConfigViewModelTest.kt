package com.example.clawdroid.config

import com.example.clawdroid.config.model.LogLevel
import com.example.clawdroid.config.model.PicoClawConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigViewModelTest {

    @Mock
    private lateinit var mockRepository: ConfigRepository

    private lateinit var viewModel: ConfigViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        whenever(mockRepository.loadConfig()).thenReturn(PicoClawConfig())
        viewModel = ConfigViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateBinaryPath_updatesState() {
        viewModel.updateBinaryPath("/custom/picoclaw")
        assert(viewModel.uiState.value.config.binaryPath == "/custom/picoclaw")
    }

    @Test
    fun updateServerPort_updatesState() {
        viewModel.updateServerPort(9090)
        assert(viewModel.uiState.value.config.serverPort == 9090)
    }

    @Test
    fun updateAutoStart_updatesState() {
        viewModel.updateAutoStart(true)
        assert(viewModel.uiState.value.config.autoStart)
    }

    @Test
    fun updateLogLevel_updatesState() {
        viewModel.updateLogLevel(LogLevel.DEBUG)
        assert(viewModel.uiState.value.config.logLevel == LogLevel.DEBUG)
    }
}
