package com.example.clawdroid.acceptance.scenarios

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.MainActivity
import com.example.clawdroid.acceptance.stages.AppStage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLaunchScenario {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun givenAppLaunches_whenWelcomeScreen_thenWelcomeTextIsDisplayed() {
        AppStage.welcomeTextIsDisplayed()
    }

    @Test
    fun givenAppLaunches_whenSettingsButtonClicked_thenConfigScreenOpens() {
        AppStage.welcomeTextIsDisplayed()
        AppStage.settingsButtonIsDisplayed()
        AppStage.clickSettings()
        AppStage.configScreenIsDisplayed()
    }
}
