package com.example.clawdroid.server

import com.example.clawdroid.server.model.ServerStatus
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ServerManagerTest {

    @Test
    fun serverStatusHasDefaults() {
        val status = ServerStatus()
        assert(status.status == "stopped")
        assert(!status.nanoClawRunning)
        assert(status.serverPort == 8080)
    }

    @Test
    fun serverStatusReflectsRunningState() {
        val status = ServerStatus(
            status = "running",
            uptimeSeconds = 120,
            nanoClawRunning = true,
            serverPort = 9090
        )
        assert(status.status == "running")
        assert(status.nanoClawRunning)
        assert(status.uptimeSeconds == 120L)
        assert(status.serverPort == 9090)
    }
}
