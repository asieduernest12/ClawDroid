package com.example.clawdroid.config

import android.content.Context
import android.util.Log
import com.example.clawdroid.model.ModelProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProviderConfigManager(private val context: Context) {

    private val configFile: File
        get() = File(context.filesDir, "picoclaw/config.json")

    fun loadProviders(): List<ModelProvider> {
        val config = loadConfigJson() ?: return emptyList()
        val modelList = config.optJSONArray("model_list") ?: return emptyList()
        val providers = mutableListOf<ModelProvider>()
        for (i in 0 until modelList.length()) {
            val entry = modelList.optJSONObject(i) ?: continue
            providers.add(ModelProvider.fromJson(entry))
        }
        return providers
    }

    fun saveProviders(providers: List<ModelProvider>) {
        val config = loadConfigJson() ?: JSONObject()
        val modelList = JSONArray()
        for (provider in providers) {
            modelList.put(provider.toJson())
        }
        config.put("model_list", modelList)
        saveConfigJson(config)
    }

    fun addProvider(provider: ModelProvider) {
        val providers = loadProviders().toMutableList()
        providers.add(provider)
        saveProviders(providers)
    }

    fun updateProvider(index: Int, provider: ModelProvider) {
        val providers = loadProviders().toMutableList()
        if (index in providers.indices) {
            providers[index] = provider
            saveProviders(providers)
        }
    }

    fun deleteProvider(index: Int) {
        val providers = loadProviders().toMutableList()
        if (index in providers.indices) {
            providers.removeAt(index)
            saveProviders(providers)
        }
    }

    fun ensureConfigExists(defaultProviders: List<ModelProvider> = emptyList()) {
        if (!configFile.exists()) {
            Log.d(TAG, "Creating default config.json with ${defaultProviders.size} providers")
            configFile.parentFile?.mkdirs()
            if (defaultProviders.isNotEmpty()) {
                saveProviders(defaultProviders)
            } else {
                saveConfigJson(JSONObject().apply {
                    put("model_list", JSONArray())
                })
            }
        }
    }

    private fun loadConfigJson(): JSONObject? {
        return try {
            if (!configFile.exists()) return null
            JSONObject(configFile.readText())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read config.json", e)
            null
        }
    }

    private fun saveConfigJson(config: JSONObject) {
        try {
            configFile.parentFile?.mkdirs()
            configFile.writeText(config.toString(2))
            Log.d(TAG, "Saved config.json")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write config.json", e)
        }
    }

    companion object {
        private const val TAG = "ProviderConfigManager"
    }
}
