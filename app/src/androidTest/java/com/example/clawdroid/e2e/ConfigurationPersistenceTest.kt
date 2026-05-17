package com.example.clawdroid.e2e

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clawdroid.MainActivity
import com.example.clawdroid.config.ConfigRepository
import com.example.clawdroid.config.model.PicoClawConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigurationPersistenceTest {

    @Test
    fun configPersistsAcrossAppRestarts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ConfigRepository(context)

        val modifiedConfig = PicoClawConfig(serverPort = 9090)
        repo.saveConfig(modifiedConfig)

        ActivityScenario.launch(MainActivity::class.java).close()

        val loadedConfig = repo.loadConfig()
        assertEquals(9090, loadedConfig.serverPort)

        repo.saveConfig(PicoClawConfig())
    }
}
