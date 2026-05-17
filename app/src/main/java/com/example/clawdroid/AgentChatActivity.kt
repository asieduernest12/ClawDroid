package com.example.clawdroid

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clawdroid.config.ProviderConfigManager
import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.MessageRole
import com.example.clawdroid.model.ModelProvider
import com.example.clawdroid.terminal.TermuxBootstrapState
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AgentChatActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var dropdownProvider: AutoCompleteTextView
    private lateinit var dropdownModel: AutoCompleteTextView
    private lateinit var recyclerChat: RecyclerView
    private lateinit var inputMessage: TextInputEditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var cardTyping: MaterialCardView
    private lateinit var recyclerTerminal: RecyclerView
    private lateinit var inputTerminal: EditText
    private lateinit var bottomSheet: LinearLayout
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var btnToggleTerminal: com.google.android.material.button.MaterialButton
    private lateinit var chipStatus: Chip
    private lateinit var chipVersion: Chip
    private lateinit var chipGateway: Chip
    private lateinit var chipModels: Chip
    private lateinit var chipRestart: Chip

    private val chatAdapter = ChatAdapter()
    private val terminalAdapter = TerminalAdapter()
    private val terminalLines = mutableListOf<String>()
    private val configManager by lazy { ProviderConfigManager(this) }
    private var activeProvider: ModelProvider? = null
    private var activeModel: String = ""
    private val fetchedModels = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent)

        toolbar = findViewById(R.id.toolbar)
        dropdownProvider = findViewById(R.id.dropdown_provider)
        dropdownModel = findViewById(R.id.dropdown_model)
        recyclerChat = findViewById(R.id.recycler_chat)
        inputMessage = findViewById(R.id.input_message)
        btnSend = findViewById(R.id.btn_send)
        cardTyping = findViewById(R.id.card_typing)
        recyclerTerminal = findViewById(R.id.recycler_terminal)
        inputTerminal = findViewById(R.id.input_terminal)
        bottomSheet = findViewById(R.id.bottom_sheet_terminal)

        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        btnToggleTerminal = findViewById(R.id.btn_toggle_terminal)
        btnToggleTerminal.setOnClickListener {
            if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                btnToggleTerminal.text = "▼"
            } else {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                btnToggleTerminal.text = "▲"
            }
        }

        toolbar.setNavigationOnClickListener { finish() }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_clear_chat -> {
                    chatAdapter.clearMessages()
                    true
                }
                R.id.action_fetch_models -> {
                    fetchModelsFromProvider()
                    true
                }
                else -> false
            }
        }

        setupProviderDropdown()
        setupChatRecycler()
        setupTerminalRecycler()
        setupCommandChips()
        setupSendActions()
        loadProviderTerminalOutput()

        addSystemMessage(getString(R.string.agent_welcome))
    }

    private fun setupProviderDropdown() {
        val providers = configManager.loadProviders()
        if (providers.isEmpty()) {
            Toast.makeText(this, R.string.providers_empty_title, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val providerNames = providers.map { it.modelName }.toTypedArray()
        dropdownProvider.setAdapter(ArrayAdapter(this,
            android.R.layout.simple_dropdown_item_1line, providerNames))

        dropdownProvider.setOnItemClickListener { _, _, position, _ ->
            activeProvider = providers[position]
            activeModel = ""
            dropdownModel.setText("")
            fetchedModels.clear()
            updateModelDropdown()
            addSystemMessage("Switched provider to ${activeProvider?.modelName}")
        }

        if (providers.isNotEmpty()) {
            activeProvider = providers[0]
            dropdownProvider.setText(activeProvider?.modelName, false)
        }
    }

    private fun updateModelDropdown() {
        val models = if (fetchedModels.isNotEmpty()) {
            fetchedModels.toTypedArray()
        } else {
            arrayOf(activeProvider?.model?.substringAfter("/") ?: "default")
        }
        dropdownModel.setAdapter(ArrayAdapter(this,
            android.R.layout.simple_dropdown_item_1line, models))

        dropdownModel.setOnItemClickListener { _, _, position, _ ->
            activeModel = models[position]
            addSystemMessage("Model set to $activeModel")
        }

        if (models.isNotEmpty()) {
            activeModel = models[0]
            dropdownModel.setText(activeModel, false)
        }
    }

    private fun fetchModelsFromProvider() {
        val provider = activeProvider ?: return
        if (provider.apiBase.isBlank() && provider.apiKey.isBlank()) {
            Toast.makeText(this, "No API endpoint to fetch models from", Toast.LENGTH_SHORT).show()
            return
        }

        cardTyping.isVisible = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val baseUrl = provider.apiBase.ifBlank { "https://api.openai.com/v1" }
                val url = URL("$baseUrl/models")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val json = JSONObject(response)
                val data = json.optJSONArray("data") ?: JSONArray()
                val models = mutableListOf<String>()
                for (i in 0 until data.length()) {
                    data.getJSONObject(i).optString("id")?.let { models.add(it) }
                }

                withContext(Dispatchers.Main) {
                    fetchedModels.clear()
                    fetchedModels.addAll(models.take(20))
                    updateModelDropdown()
                    cardTyping.isVisible = false
                    Toast.makeText(this@AgentChatActivity,
                        "Fetched ${fetchedModels.size} models", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    cardTyping.isVisible = false
                    Toast.makeText(this@AgentChatActivity,
                        "Failed to fetch models: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupChatRecycler() {
        recyclerChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerChat.adapter = chatAdapter
    }

    private fun setupTerminalRecycler() {
        recyclerTerminal.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerTerminal.adapter = terminalAdapter
    }

    private fun setupCommandChips() {
        val chipGroup = findViewById<LinearLayout>(R.id.chip_group_commands)
        chipStatus = chipGroup.getChildAt(0) as Chip
        chipVersion = chipGroup.getChildAt(1) as Chip
        chipGateway = chipGroup.getChildAt(2) as Chip
        chipModels = chipGroup.getChildAt(3) as Chip
        chipRestart = chipGroup.getChildAt(4) as Chip

        chipStatus.setOnClickListener { sendTerminalCommand("status") }
        chipVersion.setOnClickListener { sendTerminalCommand("version") }
        chipGateway.setOnClickListener { sendTerminalCommand("gateway") }
        chipModels.setOnClickListener { sendTerminalCommand("model list") }
        chipRestart.setOnClickListener {
            sendTerminalCommand("restart")
            restartPicoClaw()
        }
    }

    private fun setupSendActions() {
        btnSend.setOnClickListener { sendChatMessage() }

        inputMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendChatMessage()
                true
            } else false
        }

        inputTerminal.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTerminalCommand(inputTerminal.text.toString())
                inputTerminal.text.clear()
                true
            } else false
        }
    }

    private fun sendChatMessage() {
        val text = inputMessage.text.toString().trim()
        if (text.isBlank()) return

        val provider = activeProvider ?: run {
            Toast.makeText(this, "No provider selected", Toast.LENGTH_SHORT).show()
            return
        }

        chatAdapter.addMessage(ChatMessage(MessageRole.USER, text))
        inputMessage.text?.clear()
        cardTyping.isVisible = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val model = activeModel.ifBlank { provider.model.substringAfter("/") }
                val baseUrl = provider.apiBase.ifBlank { "https://api.openai.com/v1" }
                val url = URL("$baseUrl/chat/completions")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 60000

                val body = JSONObject().apply {
                    put("model", if (activeModel.isNotBlank()) activeModel else provider.model)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", text)
                        })
                    })
                }

                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val json = JSONObject(response)
                val choices = json.optJSONArray("choices")
                val reply = if (choices != null && choices.length() > 0) {
                    choices.getJSONObject(0).getJSONObject("message").optString("content", "")
                } else {
                    json.optString("error", "No response")
                }

                withContext(Dispatchers.Main) {
                    cardTyping.isVisible = false
                    chatAdapter.addMessage(ChatMessage(MessageRole.AGENT, reply))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    cardTyping.isVisible = false
                    addSystemMessage("Error: ${e.message}")
                }
            }
        }
    }

    private fun sendTerminalCommand(command: String) {
        if (command.isBlank()) return
        terminalLines.add("> $command")
        terminalAdapter.notifyItemInserted(terminalLines.size - 1)
        recyclerTerminal.smoothScrollToPosition(terminalLines.size - 1)
    }

    private fun addSystemMessage(text: String) {
        chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, text))
    }

    private fun restartPicoClaw() {
        val app = application as App
        if (app.bootstrapState.value !is TermuxBootstrapState.Ready) return
        lifecycleScope.launch {
            app.terminalManager.stopPicoClaw()
            kotlinx.coroutines.delay(1000)
            app.terminalManager.launchPicoClaw()
        }
    }

    private fun loadProviderTerminalOutput() {
        val app = application as App
        lifecycleScope.launch {
            app.getPicoClawSession()?.outputLines?.collect { lines ->
                terminalLines.clear()
                terminalLines.addAll(lines)
                terminalAdapter.notifyDataSetChanged()
                if (terminalLines.isNotEmpty()) {
                    recyclerTerminal.smoothScrollToPosition(terminalLines.size - 1)
                }
            }
        }
    }

    private inner class ChatAdapter :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        private val messages = mutableListOf<ChatMessage>()

        fun addMessage(msg: ChatMessage) {
            messages.add(msg)
            notifyItemInserted(messages.size - 1)
            recyclerChat.smoothScrollToPosition(messages.size - 1)
        }

        fun clearMessages() {
            val count = messages.size
            messages.clear()
            notifyItemRangeRemoved(0, count)
        }

        override fun getItemViewType(position: Int): Int = when (messages[position].role) {
            MessageRole.USER -> 0
            MessageRole.AGENT -> 1
            MessageRole.SYSTEM -> 2
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = when (viewType) {
                0, 1 -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_message, parent, false)
                else -> {
                    val tv = TextView(parent.context).apply {
                        setPadding(24, 12, 24, 12)
                        setTextColor(Color.GRAY)
                        textSize = 13f
                        gravity = android.view.Gravity.CENTER
                    }
                    return ViewHolder(tv, null, null, null, null)
                }
            }
            return ViewHolder(view,
                view.findViewById(R.id.bubble_agent),
                view.findViewById(R.id.text_agent),
                view.findViewById(R.id.bubble_user),
                view.findViewById(R.id.text_user))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val msg = messages[position]
            when (msg.role) {
                MessageRole.USER -> holder.bindUser(msg.content)
                MessageRole.AGENT -> holder.bindAgent(msg.content)
                MessageRole.SYSTEM -> (holder.itemView as? TextView)?.text = msg.content
            }
        }

        override fun getItemCount() = messages.size

        inner class ViewHolder(
            itemView: View,
            private val bubbleAgent: MaterialCardView?,
            private val textAgent: TextView?,
            private val bubbleUser: MaterialCardView?,
            private val textUser: TextView?
        ) : RecyclerView.ViewHolder(itemView) {

            fun bindAgent(text: String) {
                bubbleUser?.isVisible = false
                bubbleAgent?.isVisible = true
                textAgent?.text = renderMarkdown(text)
            }

            fun bindUser(text: String) {
                bubbleAgent?.isVisible = false
                bubbleUser?.isVisible = true
                textUser?.text = text
            }
        }
    }

    private inner class TerminalAdapter :
        RecyclerView.Adapter<TerminalAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                setTextColor(Color.parseColor("#4ADE80"))
                setBackgroundColor(Color.parseColor("#0D1117"))
                textSize = 12f
                setTypeface(Typeface.MONOSPACE)
                setPadding(16, 2, 16, 2)
                setLineSpacing(0f, 1.2f)
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = terminalLines[position]
        }

        override fun getItemCount() = terminalLines.size

        inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }

    private fun renderMarkdown(text: String): CharSequence {
        val sb = SpannableStringBuilder()
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(text.substring(i + 2, end))
                        sb.setSpan(StyleSpan(Typeface.BOLD), start, sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = end + 2
                    } else { sb.append(text[i]); i++ }
                }
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(text.substring(i + 1, end))
                        sb.setSpan(StyleSpan(Typeface.ITALIC), start, sb.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = end + 1
                    } else { sb.append(text[i]); i++ }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(text.substring(i + 1, end))
                        sb.setSpan(ForegroundColorSpan(Color.parseColor("#4ADE80")),
                            start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sb.setSpan(BackgroundColorSpan(Color.parseColor("#1A2332")),
                            start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = end + 1
                    } else { sb.append(text[i]); i++ }
                }
                text.startsWith("```", i) -> {
                    val end = text.indexOf("```", i + 3)
                    if (end != -1) {
                        val start = sb.length
                        sb.append("\n")
                        sb.append(text.substring(i + 3, end).trim())
                        sb.append("\n")
                        sb.setSpan(ForegroundColorSpan(Color.parseColor("#4ADE80")),
                            start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sb.setSpan(BackgroundColorSpan(Color.parseColor("#1A2332")),
                            start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = end + 3
                    } else { sb.append(text[i]); i++ }
                }
                text.startsWith("\n- ", i) || (i == 0 && text.startsWith("- ", i)) -> {
                    sb.append("\n  • ")
                    i += if (text.startsWith("\n- ", i)) 3 else 2
                }
                text.startsWith("\n", i) -> {
                    sb.append("\n")
                    i++
                }
                else -> { sb.append(text[i]); i++ }
            }
        }
        return sb
    }
}
