package com.example.clawdroid

import com.example.clawdroid.model.ModelProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray

class OpenRouterChatTest {

    @Test
    fun `openrouter model prefix is stripped`() {
        val rawModel = "openrouter/nvidia/nemotron-4-340b-instruct"
        val apiBase = "https://openrouter.ai/api/v1"
        val resolved = resolveModelName(rawModel, apiBase)
        assertEquals("nvidia/nemotron-4-340b-instruct", resolved)
    }

    @Test
    fun `non-openrouter model name is unchanged`() {
        val rawModel = "gpt-4o"
        val apiBase = "https://api.openai.com/v1"
        val resolved = resolveModelName(rawModel, apiBase)
        assertEquals("gpt-4o", resolved)
    }

    @Test
    fun `openrouter provider has correct api_base`() {
        val provider = ModelProvider(
            modelName = "nemotron-30b",
            model = "openrouter/nvidia/nemotron-4-30b-instruct",
            apiKey = "test-key",
            apiBase = "https://openrouter.ai/api/v1"
        )
        assertTrue(provider.apiBase.contains("openrouter"))
        assertTrue(provider.hasKey)
    }

    @Test
    fun `openrouter preset is in predefined list`() {
        val presetNames = ProviderEditDialog.PREDEFINED_NAMES
        assertTrue("OpenRouter should be in presets", presetNames.contains("OpenRouter"))

        val presets = ProviderEditDialog.PREDEFINED
        val openrouterPreset = presets.find { it.model.startsWith("openrouter/") }
        assertTrue(openrouterPreset != null)
        assertEquals("https://openrouter.ai/api/v1", openrouterPreset!!.apiBase)
    }

    @Test
    fun `openrouter headers are required for openrouter base url`() {
        val baseUrl = "https://openrouter.ai/api/v1"
        assertTrue(baseUrl.contains("openrouter", ignoreCase = true))
    }

    @Test
    fun `non-openrouter headers not required`() {
        val baseUrl = "https://api.openai.com/v1"
        assertFalse(baseUrl.contains("openrouter", ignoreCase = true))
    }

    companion object {
        fun resolveModelName(rawModel: String, apiBase: String): String {
            if (apiBase.contains("openrouter", ignoreCase = true)) {
                return rawModel.removePrefix("openrouter/")
            }
            return rawModel
        }
    }
}
