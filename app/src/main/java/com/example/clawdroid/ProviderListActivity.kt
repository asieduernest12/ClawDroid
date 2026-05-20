package com.example.clawdroid

import android.graphics.Color
import android.os.Bundle
import com.example.clawdroid.BuildConfig
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clawdroid.config.ProviderConfigManager
import com.example.clawdroid.model.ModelProvider
import com.example.clawdroid.terminal.TermuxBootstrapState
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProviderListActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: View
    private lateinit var configManager: ProviderConfigManager
    private lateinit var adapter: ProviderAdapter

    private var providers = listOf<ModelProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_providers)

        toolbar = findViewById(R.id.toolbar)
        recycler = findViewById(R.id.recycler_providers)
        emptyState = findViewById(R.id.empty_state)
        configManager = ProviderConfigManager(this)
        configManager.ensureConfigExists(defaultProviders = listOf(
            ModelProvider(
                modelName = "OpenRouter Auto",
                model = "openrouter/auto",
                provider = "openrouter",
                apiKey = BuildConfig.OPENROUTER_API_KEY,
                apiBase = "https://openrouter.ai/api/v1"
            ),
            ModelProvider(
                modelName = "GPT-5.4",
                model = "openai/gpt-5.4",
                provider = "openai",
                apiBase = "https://api.openai.com/v1"
            ),
            ModelProvider(
                modelName = "Claude Sonnet 4.6",
                model = "anthropic/claude-sonnet-4.6",
                provider = "anthropic",
                apiBase = "https://api.anthropic.com/v1"
            ),
            ModelProvider(
                modelName = "DeepSeek Chat",
                model = "deepseek/deepseek-chat",
                provider = "deepseek",
                apiBase = "https://api.deepseek.com/v1"
            ),
        ))

        toolbar.setNavigationOnClickListener { finish() }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_restart_picoclaw -> {
                    restartPicoClaw()
                    true
                }
                else -> false
            }
        }

        adapter = ProviderAdapter(
            onEdit = { provider, index -> showEditDialog(provider, index) },
            onDelete = { _, index -> confirmDelete(index) }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fab_add_provider
        ).setOnClickListener {
            showAddDialog()
        }

        loadProviders()
    }

    private fun loadProviders() {
        providers = configManager.loadProviders()
        adapter.submitList(providers)
        emptyState.isVisible = providers.isEmpty()
    }

    private fun showAddDialog() {
        val dialog = ProviderEditDialog { provider ->
            configManager.addProvider(provider)
            loadProviders()
            showRestartSnackbar()
        }
        dialog.show(supportFragmentManager, "add_provider")
    }

    private fun showEditDialog(provider: ModelProvider, index: Int) {
        val dialog = ProviderEditDialog(existingProvider = provider) { updated ->
            configManager.updateProvider(index, updated)
            loadProviders()
            showRestartSnackbar()
        }
        dialog.show(supportFragmentManager, "edit_provider")
    }

    private fun confirmDelete(index: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.providers_delete_title)
            .setMessage(R.string.providers_delete_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.providers_btn_delete) { _, _ ->
                configManager.deleteProvider(index)
                loadProviders()
                showRestartSnackbar()
            }
            .show()
    }

    private fun showRestartSnackbar() {
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            recycler,
            getString(R.string.providers_saved_restart),
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.setAction(R.string.providers_btn_restart_now) {
            restartPicoClaw()
        }
        snackbar.show()
    }

    private fun restartPicoClaw() {
        val app = application as App
        if (app.bootstrapState.value !is TermuxBootstrapState.Ready) {
            Toast.makeText(this, R.string.error_picoclaw_not_ready, Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, R.string.providers_restarting, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            app.terminalManager.stopPicoClaw()
            kotlinx.coroutines.delay(1000)
            app.terminalManager.launchPicoClaw()
            runOnUiThread {
                Toast.makeText(this@ProviderListActivity,
                    R.string.providers_restarted, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class ProviderAdapter(
        private val onEdit: (ModelProvider, Int) -> Unit,
        private val onDelete: (ModelProvider, Int) -> Unit
    ) : RecyclerView.Adapter<ProviderAdapter.ViewHolder>() {

        private var items = listOf<ModelProvider>()

        fun submitList(list: List<ModelProvider>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_provider, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val name: TextView = itemView.findViewById(R.id.provider_name)
            private val model: TextView = itemView.findViewById(R.id.provider_model)
            private val url: TextView = itemView.findViewById(R.id.provider_url)
            private val chipKey: Chip = itemView.findViewById(R.id.chip_key_status)
            private val btnEdit: com.google.android.material.button.MaterialButton =
                itemView.findViewById(R.id.btn_edit)
            private val btnDelete: com.google.android.material.button.MaterialButton =
                itemView.findViewById(R.id.btn_delete)

            fun bind(provider: ModelProvider, index: Int) {
                name.text = provider.modelName
                model.text = provider.model

                if (provider.apiBase.isNotBlank()) {
                    url.text = provider.apiBase
                    url.isVisible = true
                } else {
                    url.isVisible = false
                }

                if (provider.hasKey) {
                    chipKey.text = itemView.context.getString(R.string.providers_key_set)
                    chipKey.chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                        itemView.context, R.color.status_running)
                    chipKey.setTextColor(Color.WHITE)
                } else {
                    chipKey.text = itemView.context.getString(R.string.providers_key_none)
                    chipKey.chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                        itemView.context, R.color.status_offline)
                    chipKey.setTextColor(Color.WHITE)
                }

                btnEdit.setOnClickListener { onEdit(provider, index) }
                btnDelete.setOnClickListener { onDelete(provider, index) }
            }
        }
    }
}
