package com.example.clawdroid.terminal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ProcessMonitorTest {

    @Test
    fun processMonitor_initialState_isStopped() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val monitor = ProcessMonitor(scope)
        assert(!monitor.processState.value.running)
        assert(monitor.processState.value.pid == -1)
    }

    @Test
    fun processMonitor_markRunning_updatesState() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val monitor = ProcessMonitor(scope)
        monitor.markRunning()
        assert(monitor.processState.value.running)
    }

    @Test
    fun processMonitor_markStopped_resetsState() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val monitor = ProcessMonitor(scope)
        monitor.markRunning()
        monitor.markStopped()
        assert(!monitor.processState.value.running)
        assert(monitor.processState.value.pid == -1)
    }
}
