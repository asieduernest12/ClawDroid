package com.example.clawdroid.acceptance.scenarios

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.acceptance.stages.ServerStage
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerScenario {

    @Test
    fun givenServerRunning_whenHealthEndpointCalled_thenReturnsOkWithUptime() {
        assertTrue("Server should be running", ServerStage.waitForServer())
        assertTrue("Health endpoint should return ok", ServerStage.healthEndpointReturnsOk())
    }

    @Test
    fun givenServerRunning_whenStatusEndpointCalled_thenReturnsFullState() {
        assertTrue("Server should be running", ServerStage.waitForServer())
        assertTrue("Status should have all fields", ServerStage.statusEndpointHasAllFields())
    }

    @Test
    fun givenServerRunning_whenRootRequested_thenDashboardHtmlServed() {
        assertTrue("Server should be running", ServerStage.waitForServer())
        assertTrue("Dashboard should be served", ServerStage.dashboardIsServed())
    }
}
