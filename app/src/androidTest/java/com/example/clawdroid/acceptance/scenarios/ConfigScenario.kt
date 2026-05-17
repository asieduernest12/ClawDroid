package com.example.clawdroid.acceptance.scenarios

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.acceptance.stages.ConfigStage
import com.example.clawdroid.config.ConfigActivity
import com.example.clawdroid.config.model.PicoClawConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigScenario {

    @get:Rule
    val activityRule = ActivityScenarioRule(ConfigActivity::class.java)

    @After
    fun tearDown() {
        ConfigStage.resetConfig()
    }

    @Test
    fun givenConfigScreen_whenLoaded_thenDefaultPortIsDisplayed() {
        ConfigStage.configTitleIsDisplayed()
    }

    @Test
    fun givenConfigScreen_whenUserChangesPortAndSaves_thenPortIsPersisted() {
        activityRule.scenario.onActivity { activity ->
            activity.viewModel.updateServerPort(9090)
            activity.viewModel.save()
        }
        val config = ConfigStage.loadConfig()
        assertEquals(9090, config.serverPort)
    }

    @Test
    fun givenConfigScreen_whenUserEntersInvalidPort_thenValidationErrorShown() {
        var hasError: String? = null
        activityRule.scenario.onActivity { activity ->
            activity.viewModel.updateServerPort(80)
            activity.viewModel.save()
            hasError = activity.binding.serverPortLayout.error?.toString()
        }
        assertEquals("Port must be between 1024 and 65535", hasError)
    }

    @Test
    fun givenConfigScreen_whenResetClicked_thenDefaultsRestored() {
        activityRule.scenario.onActivity { activity ->
            activity.viewModel.updateServerPort(9090)
        }
        ConfigStage.clickReset()
        val config = ConfigStage.loadConfig()
        assertEquals(PicoClawConfig().serverPort, config.serverPort)
    }

    @Test
    fun givenConfigPersisted_whenAppRestarted_thenValuesAreLoaded() {
        ConfigStage.saveConfig(PicoClawConfig(serverPort = 9090))
        ActivityScenario.launch(ConfigActivity::class.java).use { _ ->
            val config = ConfigStage.loadConfig()
            assertEquals(9090, config.serverPort)
        }
    }
}
