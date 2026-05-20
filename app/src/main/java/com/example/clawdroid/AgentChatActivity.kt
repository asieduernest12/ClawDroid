package com.example.clawdroid

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clawdroid.chat.ChatAdapter
import com.example.clawdroid.chat.ChatHistoryManager
import com.example.clawdroid.command.CommandContext
import com.example.clawdroid.command.CommandExecutor
import com.example.clawdroid.command.CommandParseResult
import com.example.clawdroid.command.CommandRegistry
import com.example.clawdroid.config.ProviderConfigManager
import com.example.clawdroid.model.ChatMessage
import com.example.clawdroid.model.ChatSession
import com.example.clawdroid.model.MessageRole
import com.example.clawdroid.model.ModelProvider
import com.example.clawdroid.model.PickerModel
import com.example.clawdroid.state.AppStateManager
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
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class AgentChatActivity : AppCompatActivity() {

    private data class ProviderGroup(
        val slug: String,
        val displayName: String,
        val apiKey: String,
        val apiBase: String,
        val models: List<ModelProvider>,
    ) {
        val defaultModel: ModelProvider? get() = models.firstOrNull()
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var dropdownProvider: AutoCompleteTextView
    private lateinit var selectedModel: TextView
    private lateinit var layoutModelTrigger: MaterialCardView
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
    private val chatHistoryManager by lazy { ChatHistoryManager(this) }
    private var providerGroups: List<ProviderGroup> = emptyList()
    private var activeGroup: ProviderGroup? = null
    private var activeModel: String = ""
    private val fetchedModels = mutableListOf<String>()
    private var isExecutingCliCommand = false
    private var currentSessionId: String? = null

    private val commandRegistry: CommandRegistry = CommandRegistry.defaultCommands()
    private lateinit var commandExecutor: CommandExecutor
    private var commandPopup: PopupWindow? = null
    private var commandPopupList: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent)

        toolbar = findViewById(R.id.toolbar)
        dropdownProvider = findViewById(R.id.dropdown_provider)
        selectedModel = findViewById(R.id.selected_model)
        layoutModelTrigger = findViewById(R.id.layout_model_trigger)
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
                R.id.action_sessions -> {
                    showSessionsDialog()
                    true
                }
                R.id.action_clear_chat -> {
                    clearCurrentSession()
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
        setupModelPickerTrigger()
        setupChatRecycler()
        setupTerminalRecycler()
        setupCommandChips()
        setupSendActions()
        loadProviderTerminalOutput()

        commandExecutor = CommandExecutor(
            registry = commandRegistry,
            context = CommandContext(
                activity = this,
                chatHistoryManager = chatHistoryManager,
                configManager = configManager,
                chatAdapter = chatAdapter
            )
        )
        commandExecutor.setupDefaultHandlers()
        setupSlashCommandDetection()

        initSession()
    }

    private fun initSession() {
        val savedId = chatHistoryManager.getCurrentSessionId()
        val session = if (savedId != null) {
            chatHistoryManager.getSession(savedId)
        } else null

        if (session != null) {
            currentSessionId = session.id
            val group = providerGroups.find { it.slug == session.providerId }
            activeGroup = group
            activeModel = session.modelId
            if (group != null) {
                dropdownProvider.setText(group.displayName, false)
            }
            if (activeModel.isNotBlank()) {
                selectedModel.text = activeModel.substringAfterLast("/")
            }
            val messages = chatHistoryManager.getMessages(session.id)
            chatAdapter.setMessages(messages)
            if (messages.isEmpty()) {
                addSystemMessage(getString(R.string.agent_welcome))
            }
        } else {
            createNewSession()
            addSystemMessage(getString(R.string.agent_welcome))
        }
    }

    private fun createNewSession() {
        val group = activeGroup
        val session = chatHistoryManager.createSession(
            providerId = group?.slug ?: "",
            modelId = activeModel
        )
        currentSessionId = session.id
        chatHistoryManager.setCurrentSessionId(session.id)
        chatAdapter.clearMessages()
    }

    private fun clearCurrentSession() {
        val sessionId = currentSessionId ?: return
        chatHistoryManager.clearMessages(sessionId)
        chatAdapter.clearMessages()
        addSystemMessage(getString(R.string.agent_welcome))
    }

    private fun showSessionsDialog() {
        val sessions = chatHistoryManager.getSessions()
        if (sessions.isEmpty()) {
            Toast.makeText(this, "No sessions yet", Toast.LENGTH_SHORT).show()
            return
        }

        val items = sessions.map { it.title }.toTypedArray()
        val selectedIndex = sessions.indexOfFirst { it.id == currentSessionId }.coerceAtLeast(0)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chat Sessions")
            .setSingleChoiceItems(items, selectedIndex) { dialog, which ->
                val session = sessions[which]
                switchToSession(session)
                dialog.dismiss()
            }
            .setPositiveButton("New Session") { _, _ ->
                createNewSession()
                addSystemMessage(getString(R.string.agent_welcome))
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun switchToSession(session: ChatSession) {
        currentSessionId = session.id
        chatHistoryManager.setCurrentSessionId(session.id)
        val group = providerGroups.find { it.slug == session.providerId }
        activeGroup = group
        activeModel = session.modelId
        if (group != null) {
            dropdownProvider.setText(group.displayName, false)
        }
        if (activeModel.isNotBlank()) {
            selectedModel.text = activeModel.substringAfterLast("/")
        } else {
            selectedModel.text = ""
        }
        val messages = chatHistoryManager.getMessages(session.id)
        chatAdapter.setMessages(messages)
    }

    private fun setupProviderDropdown() {
        val providers = configManager.loadProviders()
        providerGroups = groupProviders(providers)

        if (providerGroups.isEmpty()) {
            dropdownProvider.setText(getString(R.string.providers_empty_title), false)
            dropdownProvider.isEnabled = false
            inputMessage.hint = getString(R.string.agent_hint_no_provider)
            btnSend.isEnabled = false
            addSystemMessage(getString(R.string.agent_no_provider_guide))
            return
        }

        val groupNames = providerGroups.map { it.displayName }.toTypedArray()
        dropdownProvider.setAdapter(ArrayAdapter(this,
            android.R.layout.simple_dropdown_item_1line, groupNames))

        dropdownProvider.setOnItemClickListener { _, _, position, _ ->
            val group = providerGroups[position]
            activeGroup = group
            activeModel = group.defaultModel?.model ?: ""
            selectedModel.text = activeModel.substringAfterLast("/")
            fetchedModels.clear()
            selectedModel.hint = getString(R.string.agent_model_hint)
            addSystemMessage("Switched provider to ${group.displayName}")
            currentSessionId?.let { sid ->
                chatHistoryManager.getSession(sid)?.let { session ->
                    val updated = session.copy(providerId = group.slug)
                    chatHistoryManager.updateSession(updated)
                }
            }
            fetchModelsFromProvider()
        }

        activeGroup = providerGroups.firstOrNull()
        activeGroup?.let { group ->
            dropdownProvider.setText(group.displayName, false)
            activeModel = group.defaultModel?.model ?: ""
            if (activeModel.isNotBlank()) {
                selectedModel.text = activeModel.substringAfterLast("/")
            }
        }

        if (activeGroup?.apiBase?.isNotBlank() == true) {
            fetchModelsFromProvider()
        }
    }

    private fun groupProviders(providers: List<ModelProvider>): List<ProviderGroup> {
        val grouped = mutableMapOf<String, MutableList<ModelProvider>>()
        for (p in providers) {
            val key = p.provider.takeIf { it.isNotBlank() } ?: p.modelName
            grouped.getOrPut(key) { mutableListOf() }.add(p)
        }
        return grouped.map { (slug, models) ->
            val first = models.first()
            ProviderGroup(
                slug = slug,
                displayName = slug.replaceFirstChar { it.uppercase() },
                apiKey = first.apiKey,
                apiBase = first.apiBase,
                models = models
            )
        }.sortedBy { it.displayName }
    }

    private fun setupModelPickerTrigger() {
        layoutModelTrigger.setOnClickListener {
            val models = buildPickerModels()
            val sheet = ModelPickerBottomSheet(
                providerName = activeGroup?.displayName ?: "Unknown",
                models = models,
                onModelSelected = { modelId ->
                    activeModel = modelId
                    selectedModel.text = modelId.substringAfterLast("/")
                    addSystemMessage("Model set to $activeModel")
                    currentSessionId?.let { sid ->
                        chatHistoryManager.getSession(sid)?.let { session ->
                            val updated = session.copy(modelId = activeModel)
                            chatHistoryManager.updateSession(updated)
                        }
                    }
                }
            )
            sheet.show(supportFragmentManager, "model_picker")
        }
    }

    private fun buildPickerModels(): List<PickerModel> {
        val group = activeGroup ?: return emptyList()
        val modelIds: List<String> = if (fetchedModels.isNotEmpty()) {
            fetchedModels
        } else {
            group.models.map { it.modelName }.ifEmpty {
                val fallback = group.defaultModel?.model?.substringAfter("/")
                if (!fallback.isNullOrBlank()) listOf(fallback) else emptyList()
            }
        }
        return modelIds.map { id ->
            val ctxLen = extractContextLength(id)
            PickerModel(
                modelId = id,
                displayName = id,
                providerName = group.displayName,
                providerSlug = group.slug,
                contextLength = ctxLen,
                contextDisplay = if (ctxLen != null) formatContextLength(ctxLen) else ""
            )
        }
    }

    private fun extractContextLength(modelId: String): Int? = null

    private fun formatContextLength(length: Int): String {
        return when {
            length >= 1_000_000 -> "${length / 1_000_000}M"
            length >= 1_000 -> "${length / 1_000}K"
            else -> length.toString()
        }
    }

    private fun fetchModelsFromProvider() {
        val group = activeGroup ?: return
        if (group.apiBase.isBlank()) {
            Toast.makeText(this, "No API endpoint configured for this provider", Toast.LENGTH_SHORT).show()
            return
        }

        cardTyping.isVisible = true
        addSystemMessage("Fetching models from ${group.displayName}...")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${group.apiBase}/models")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer ${group.apiKey}")
                conn.connectTimeout = 15000
                conn.readTimeout = 15000

                val responseCode = conn.responseCode
                val response = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "No error body"
                    conn.disconnect()
                    withContext(Dispatchers.Main) {
                        cardTyping.isVisible = false
                        addSystemMessage("Model fetch failed: HTTP $responseCode — using ${group.displayName} default")
                    }
                    return@launch
                }
                conn.disconnect()

                val json = JSONObject(response)
                val data = json.optJSONArray("data") ?: JSONArray()
                val models = mutableListOf<String>()
                for (i in 0 until data.length()) {
                    val id = data.getJSONObject(i).optString("id", "")
                    if (id.isNotBlank()) models.add(id)
                }

                withContext(Dispatchers.Main) {
                    fetchedModels.clear()
                    if (models.isNotEmpty()) {
                        fetchedModels.addAll(models)
                        addSystemMessage("Loaded ${models.size} models from ${group.displayName}. Tap model picker to browse.")
                    } else {
                        addSystemMessage("No models returned from ${group.displayName}")
                    }
                    cardTyping.isVisible = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    cardTyping.isVisible = false
                    addSystemMessage("Could not fetch models: ${e.message} — using ${group.displayName} default")
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

    private fun setupSlashCommandDetection() {
        inputMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                if (text.startsWith("/") && text.length > 1) {
                    showCommandPopup(text)
                } else {
                    dismissCommandPopup()
                }
            }
        })
    }

    private fun showCommandPopup(query: String) {
        val suggestions = commandRegistry.search(query)
        if (suggestions.isEmpty()) {
            dismissCommandPopup()
            return
        }

        if (commandPopup == null) {
            val view = LayoutInflater.from(this).inflate(R.layout.popup_slash_commands, null)
            commandPopupList = view.findViewById(R.id.list_commands)
            commandPopupList?.setOnItemClickListener { _, _, position, _ ->
                val cmd = suggestions[position]
                val cmdText = if (cmd.hasArgs) "/${cmd.name} " else "/${cmd.name}"
                inputMessage.setText(cmdText)
                inputMessage.setSelection(cmdText.length)
                dismissCommandPopup()
            }
            commandPopup = PopupWindow(
                view,
                resources.getDimensionPixelSize(R.dimen.popup_width),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                isOutsideTouchable = true
                elevation = 8f
            }
        }

        val names = suggestions.map { cmd ->
            if (cmd.hasArgs) "${cmd.usage} ${cmd.argHint}" else cmd.usage
        }
        commandPopupList?.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            names
        )
        commandPopupList?.let { list ->
            val listHeight = minOf(
                suggestions.size * resources.getDimensionPixelSize(R.dimen.popup_item_height),
                resources.getDimensionPixelSize(R.dimen.popup_max_height)
            )
            val popupHeight = listHeight +
                (commandPopup?.contentView?.paddingTop ?: 0) +
                (commandPopup?.contentView?.paddingBottom ?: 0)
            commandPopup?.height = popupHeight
        }
        commandPopup?.showAsDropDown(inputMessage, 0, 0)
    }

    private fun dismissCommandPopup() {
        commandPopup?.dismiss()
    }

    private fun truncateMessages(messages: List<ChatMessage>, maxTokens: Int = 128000): List<ChatMessage> {
        if (messages.isEmpty()) return messages
        fun estimateTokens(text: String): Int = (text.length / 4).coerceAtLeast(1)
        val systemPrompt = "You are a helpful AI assistant running inside ClawDroid on Android."
        var runningTokens = estimateTokens(systemPrompt)
        val result = mutableListOf<ChatMessage>()
        for (msg in messages.reversed()) {
            val tokens = estimateTokens(msg.content)
            if (runningTokens + tokens > maxTokens && result.isNotEmpty()) break
            result.add(0, msg)
            runningTokens += tokens
        }
        return result
    }

    private fun sendChatMessage() {
        val text = inputMessage.text.toString().trim()
        if (text.isBlank()) return

        // Intercept slash commands
        if (text.startsWith("/")) {
            val result = commandExecutor.execute(text)
            inputMessage.text?.clear()
            if (result is CommandParseResult.Success) {
                when (result.command.name) {
                    "clear" -> clearCurrentSession()
                    "help" -> { /* handled by executor */ }
                    "model" -> if (result.args.isNotEmpty()) {
                        activeModel = result.args[0]
                        selectedModel.text = result.args[0]
                        result.args.forEach { arg ->
                            AppStateManager.updateChat({ it.copy(currentModel = arg) }, source = "slash:model")
                        }
                    }
                    "provider" -> if (result.args.isNotEmpty()) {
                        val match = providerGroups.find {
                            it.slug.equals(result.args[0], ignoreCase = true) ||
                            it.displayName.equals(result.args[0], ignoreCase = true)
                        }
                        if (match != null) {
                            activeGroup = match
                            dropdownProvider.setText(match.displayName, false)
                            activeModel = match.defaultModel?.model ?: ""
                            selectedModel.text = activeModel.substringAfterLast("/")
                            fetchedModels.clear()
                            fetchModelsFromProvider()
                        } else {
                            addSystemMessage("Provider '${result.args[0]}' not found")
                        }
                    }
                    "session" -> if (result.args.firstOrNull() == "new") {
                        createNewSession()
                        addSystemMessage(getString(R.string.agent_welcome))
                    }
                    "export" -> { /* handled by executor */ }
                }
            }
            return
        }

        val group = activeGroup ?: run {
            Toast.makeText(this, "No provider selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (group.apiKey.isBlank()) {
            Toast.makeText(this, "API key not set for ${group.displayName}", Toast.LENGTH_LONG).show()
            addSystemMessage("Error: API key not configured for '${group.displayName}'. Edit the provider to add an API key.")
            return
        }

        val sessionId = currentSessionId ?: run {
            createNewSession()
            currentSessionId!!
        }

        // Persist user message
        val userMsg = ChatMessage(MessageRole.USER, text)
        chatHistoryManager.addMessage(sessionId, userMsg)
        chatAdapter.addMessage(userMsg)
        recyclerChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
        inputMessage.text?.clear()
        cardTyping.isVisible = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val rawModel = if (activeModel.isNotBlank()) activeModel else group.defaultModel?.model ?: ""
                val model = resolveModelName(rawModel, group.apiBase)
                val baseUrl = group.apiBase.ifBlank { "https://api.openai.com/v1" }
                val url = URL("$baseUrl/chat/completions")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${group.apiKey}")
                if (baseUrl.contains("openrouter", ignoreCase = true)) {
                    conn.setRequestProperty("HTTP-Referer", "https://clawdroid.app")
                    conn.setRequestProperty("X-Title", "ClawDroid")
                }
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 60000

                // Build full conversation history with truncation
                val historyMessages = truncateMessages(chatHistoryManager.getMessages(sessionId))
                val messagesJson = JSONArray().apply {
                    // Add system prompt if first message
                    if (historyMessages.none { it.role == MessageRole.USER }) {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are a helpful AI assistant running inside ClawDroid on Android.")
                        })
                    }
                    historyMessages.forEach { msg ->
                        when (msg.role) {
                            MessageRole.USER -> put(JSONObject().apply {
                                put("role", "user")
                                put("content", msg.content)
                            })
                            MessageRole.AGENT -> put(JSONObject().apply {
                                put("role", "assistant")
                                put("content", msg.content)
                            })
                            MessageRole.TOOL_CALL -> {
                                val tcId = msg.toolName ?: msg.id
                                put(JSONObject().apply {
                                    put("role", "tool")
                                    put("tool_call_id", tcId)
                                    put("content", msg.toolResult ?: "")
                                })
                            }
                            else -> { /* skip system/thinking display messages */ }
                        }
                    }
                }
                val responseCode = conn.responseCode
                val response = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "No error body"
                    conn.disconnect()
                    withContext(Dispatchers.Main) {
                        cardTyping.isVisible = false
                        addSystemMessage("Error: HTTP $responseCode — $errorBody")
                    }
                    return@launch
                }
                conn.disconnect()

                val json = JSONObject(response)
                val choices = json.optJSONArray("choices")
                val firstChoice = choices?.optJSONObject(0)
                val messageObj = firstChoice?.optJSONObject("message")

                // Parse thinking / reasoning content
                var thinkingContent: String? = null
                messageObj?.optString("reasoning_content")?.let {
                    if (it.isNotBlank()) thinkingContent = it
                }
                if (thinkingContent == null) {
                    firstChoice?.optString("reasoning")?.let {
                        if (it.isNotBlank()) thinkingContent = it
                    }
                }

                // Parse tool calls
                val toolCallsJson = messageObj?.optJSONArray("tool_calls")
                val toolCalls = mutableListOf<Triple<String, String, String>>()
                if (toolCallsJson != null) {
                    for (i in 0 until toolCallsJson.length()) {
                        val tc = toolCallsJson.getJSONObject(i)
                        val func = tc.getJSONObject("function")
                        toolCalls.add(Triple(
                            func.optString("name", "unknown"),
                            func.optString("arguments", "{}"),
                            ""
                        ))
                    }
                }

                val reply = messageObj?.optString("content", "")
                    ?: json.optString("error", "No response")

                withContext(Dispatchers.Main) {
                    cardTyping.isVisible = false

                    // Show thinking if present
                    thinkingContent?.let { thinking ->
                        val thinkingMsg = ChatMessage(
                            role = MessageRole.THINKING,
                            content = thinking,
                            thinkingContent = thinking
                        )
                        chatHistoryManager.addMessage(sessionId, thinkingMsg)
                        chatAdapter.addMessage(thinkingMsg)
                    }

                    // Show tool calls if present
                    toolCalls.forEach { (name, args, _) ->
                        val toolMsg = ChatMessage(
                            role = MessageRole.TOOL_CALL,
                            content = "Tool: $name",
                            toolName = name,
                            toolArguments = args,
                            toolResult = ""
                        )
                        chatHistoryManager.addMessage(sessionId, toolMsg)
                        chatAdapter.addMessage(toolMsg)
                    }

                    // Show agent reply
                    val agentMsg = ChatMessage(MessageRole.AGENT, reply)
                    chatHistoryManager.addMessage(sessionId, agentMsg)
                    chatAdapter.addMessage(agentMsg)
                    recyclerChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    cardTyping.isVisible = false
                    addSystemMessage("Error: ${e.message}")
                }
            }
        }
    }

    private fun resolveModelName(rawModel: String, apiBase: String): String {
        if (apiBase.contains("openrouter", ignoreCase = true)) {
            val stripped = rawModel.removePrefix("openrouter/")
            return stripped
        }
        return rawModel
    }

    private fun sendTerminalCommand(command: String) {
        if (command.isBlank()) return
        terminalLines.add("> $command")
        terminalAdapter.notifyItemInserted(terminalLines.size - 1)
        recyclerTerminal.smoothScrollToPosition(terminalLines.size - 1)

        executeCliCommand(command)
    }

    private fun executeCliCommand(command: String) {
        val app = application as App
        val binaryPath = app.getPicoClawBinaryPath()
        if (binaryPath == null) {
            appendTerminalLine("Error: PicoClaw binary not found")
            return
        }

        val workDir = binaryPath.substringBeforeLast("/")
        val args = command.trim().split(Regex("\\s+"))

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                isExecutingCliCommand = true

                val pb = buildCliProcess(binaryPath, args)
                pb.directory(java.io.File(workDir))
                pb.environment().putAll(app.bootstrapManager.getEnv())
                pb.environment()["PICOCLAW_HOME"] = workDir
                pb.redirectErrorStream(true)

                val process = pb.start()
                val exited = process.waitFor(10, TimeUnit.SECONDS)

                if (!exited) {
                    process.destroy()
                    process.waitFor(1, TimeUnit.SECONDS)
                    withContext(Dispatchers.Main) {
                        appendTerminalLine("Error: Command timed out after 10s")
                        isExecutingCliCommand = false
                    }
                    return@launch
                }

                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.exitValue()

                withContext(Dispatchers.Main) {
                    if (output.isBlank()) {
                        appendTerminalLine("(exit code: $exitCode)")
                    } else {
                        output.trimEnd().lines().forEach { appendTerminalLine(it) }
                    }
                    isExecutingCliCommand = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendTerminalLine("Error: ${e.message}")
                    isExecutingCliCommand = false
                }
            }
        }
    }

    private fun buildCliProcess(binaryPath: String, args: List<String>): ProcessBuilder {
        return if (Build.VERSION.SDK_INT >= 29 && binaryPath.startsWith("/data/data/")) {
            val is64Bit = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
            val linker = if (is64Bit) "/system/bin/linker64" else "/system/bin/linker"
            ProcessBuilder(listOf(linker, binaryPath) + args)
        } else {
            ProcessBuilder(listOf(binaryPath) + args)
        }
    }

    private fun appendTerminalLine(text: String) {
        terminalLines.add(text)
        terminalAdapter.notifyItemInserted(terminalLines.size - 1)
        recyclerTerminal.smoothScrollToPosition(terminalLines.size - 1)
    }

    private fun addSystemMessage(text: String) {
        chatAdapter.addMessage(ChatMessage(MessageRole.SYSTEM, text))
        recyclerChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
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
                if (!isExecutingCliCommand) {
                    terminalLines.clear()
                    terminalLines.addAll(lines)
                    terminalAdapter.notifyDataSetChanged()
                }
                if (terminalLines.isNotEmpty()) {
                    recyclerTerminal.smoothScrollToPosition(terminalLines.size - 1)
                }
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
}
