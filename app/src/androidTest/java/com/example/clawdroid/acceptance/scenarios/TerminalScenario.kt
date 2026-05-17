package com.example.clawdroid.acceptance.scenarios

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.MainActivity
import com.example.clawdroid.acceptance.stages.AppStage
import com.example.clawdroid.acceptance.stages.ServerStage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TerminalScenario {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun givenFirstLaunch_whenBootstrapCompletes_thenTermuxEnvironmentReadyDisplayed() {
        val ready = AppStage.waitForBootstrap()
        assertTrue("Bootstrap should complete within timeout", ready)
        onView(withText("Termux environment ready"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun givenServerRunning_whenStartApiCalled_thenPicoClawLaunches() {
        assertTrue("Server should be running", ServerStage.waitForServer())
        val started = ServerStage.postStartPicoClaw()
        assertTrue("Start API should return success", started)
    }

    @Test
    fun givenPicoClawStarted_whenStopApiCalled_thenPicoClawTerminates() {
        assertTrue("Server should be running", ServerStage.waitForServer())
        ServerStage.postStartPicoClaw()
        val stopped = ServerStage.postStopPicoClaw()
        assertTrue("Stop API should return success", stopped)
    }

    @Test
    fun givenPicoClawStarted_whenStatusChecked_thenReflectsRunningState() {
        assertTrue("Server should be running", ServerStage.waitForServer())
        ServerStage.postStartPicoClaw()
        assertTrue("PicoClaw should be running", ServerStage.isPicoClawRunning())
    }

    @Test
    fun givenSecondLaunch_whenBootstrapAlreadyCached_thenReadyAppearsImmediately() {
        AppStage.waitForBootstrap()
        activityRule.scenario.close()
        ActivityScenario.launch(MainActivity::class.java).use {
            val ready = AppStage.waitForBootstrap(30000)
            assertTrue("Cached bootstrap should be ready quickly", ready)
            onView(withText("Termux environment ready"))
                .check(matches(isDisplayed()))
        }
    }
}
