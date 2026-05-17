# Single-Page Command Center — UX Mockup

## Layout: `activity_command_center.xml`

### Full-Screen ASCII Wireframe

```
┌──────────────────────────────────────────────────────┐
│  ClawDroid                          [?]  [⋮]         │  ← Toolbar: title, help/about overflow menu
│  Active: OpenAI / gpt-5.4           [▼]  (●)        │  ← Subtitle row: provider/model dropdown + gateway dot
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────── Scrollable Content ────────────┐  │
│  │                                                  │  │
│  │  [Start Agent] [Gateway] [Restart] [Logs] [Prov] │  │  ← Quick-Action Chips (horizontal scroll)
│  │                                                  │  │
│  │  ┌─ Agent Chat ──────────────────────────────┐  │  │
│  │  │                                            │  │  │
│  │  │  ┌──────────────────────────┐              │  │  │
│  │  │  │ Agent: How can I help?   │              │  │  │  ← Agent message (aligned left)
│  │  │  └──────────────────────────┘              │  │  │
│  │  │                    ┌─────────────────────┐ │  │  │
│  │  │                    │ User: show me logs  │ │  │  │  ← User message (aligned right)
│  │  │                    └─────────────────────┘ │  │  │
│  │  │  ┌──────────────────────────┐              │  │  │
│  │  │  │ Agent: Here are recent.. │              │  │  │
│  │  │  └──────────────────────────┘              │  │  │
│  │  │                    ┌─────────────────────┐ │  │  │
│  │  │                    │ User: thanks        │ │  │  │
│  │  │                    └─────────────────────┘ │  │  │
│  │  │  ┌──────────────────────────┐              │  │  │
│  │  │  │ Agent: Happy to help!    │              │  │  │
│  │  │  └──────────────────────────┘              │  │  │
│  │  │  ● ● ●  (typing indicator)                 │  │  │  ← Only when agent is processing
│  │  │                                            │  │  │
│  │  │  ┌──────────────────────────────────────┐  │  │  │
│  │  │  │        ↑ Expand to Full Chat          │  │  │  │  ← Opens bottom sheet
│  │  │  └──────────────────────────────────────┘  │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  │                                                  │  │
│  │  ┌─ CLI Quick Commands ───────────────────────┐  │  │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │  │
│  │  │  │  status  │  │ gateway  │  │ version  │  │  │  │
│  │  │  └──────────┘  └──────────┘  └──────────┘  │  │  │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │  │
│  │  │  │model list│  │ restart  │  │   logs   │  │  │  │
│  │  │  └──────────┘  └──────────┘  └──────────┘  │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                  │  │
│  │  ┌─ Provider / Model ─────────────────────────┐  │  │
│  │  │                                            │  │  │
│  │  │  Active Provider                           │  │  │
│  │  │  ┌──────────────────────────────────────┐  │  │  │
│  │  │  │ OpenAI / gpt-5.4                [✏]  │  │  │  │  ← Tap → edit provider dialog
│  │  │  └──────────────────────────────────────┘  │  │  │
│  │  │                                            │  │  │
│  │  │  Available Models                          │  │  │
│  │  │  ┌──────────────────────────────────────┐  │  │  │
│  │  │  │ Select a model...               [▼]  │  │  │  │  ← ExposedDropdownMenu
│  │  │  └──────────────────────────────────────┘  │  │  │
│  │  │                                            │  │  │
│  │  │  ┌──────────────────────────────────────┐  │  │  │
│  │  │  │         🔄 Fetch Models               │  │  │  │  ← Calls provider /v1/models
│  │  │  └──────────────────────────────────────┘  │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  │                                                  │  │
│  └──────────────────────────────────────────────────┘  │
│                                                      │
│                                                  ┌──┐ │
│                                                  │▶│ │  ← FAB: Start Agent (play) / New Chat (chat)
│                                                  └──┘ │
├──────────────────────────────────────────────────────┤
│  ┌─ Full Chat (Bottom Sheet, expanded) ───────────┐  │
│  │                                                 │  │
│  │  ┌──────────────────────────┐                    │  │
│  │  │ Agent: How can I help?   │                    │  │
│  │  └──────────────────────────┘                    │  │
│  │                  ┌─────────────────────┐         │  │
│  │                  │ User: show me logs  │         │  │
│  │                  └─────────────────────┘         │  │
│  │  ┌──────────────────────────┐                    │  │
│  │  │ Agent: Here are recent.. │                    │  │
│  │  └──────────────────────────┘                    │  │
│  │  ... (scrollable message list) ...               │  │
│  │  ... (all messages in conversation) ...          │  │
│  │                                                 │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │ Type a message...                 [SEND ▶] │  │  │  ← Input bar: TextInputLayout + IconButton
│  │  └────────────────────────────────────────────┘  │  │
│  └─────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

---

## Widget Hierarchy (Material Design 3)

```
CoordinatorLayout
├── AppBarLayout
│   └── MaterialToolbar (id: toolbar)
│       ├── [title]   "ClawDroid" (TextView, programmatic via toolbar.title)
│       ├── [subtitle] Custom ViewGroup (programmatic via toolbar.addView)
│       │   ├── TextView                "Active: {provider_name} / {model_name}"
│       │   ├── IconButton (▼)          Tap → show PopupMenu with provider list
│       │   └── View (●/○ 8dp circle)   Green = gateway connected, Red = disconnected
│       └── [menu items]
│           ├── R.id.action_refresh      Refresh gateway status
│           ├── R.id.action_help         Show help dialog
│           └── R.id.action_about        Show about dialog
│
├── NestedScrollView (id: scroll_content)
│   └── LinearLayout (vertical, id: content_container)
│
│       ├── HorizontalScrollView (id: chips_scroll)
│       │   └── ChipGroup (id: chip_group_actions, singleSelection = false)
│       │       ├── InputChip  "Start Agent"     icon: ▶ / ■ (toggle based on agent state)
│       │       ├── AssisChip  "Gateway Status"  icon: wifi / wifi_off
│       │       ├── AssisChip  "Restart"         icon: refresh
│       │       ├── AssisChip  "View Logs"       icon: description
│       │       └── AssisChip  "Providers"       icon: settings
│       │
│       ├── MaterialCardView (id: card_agent_chat)
│       │   └── LinearLayout (vertical)
│       │       ├── TextView             "Agent Chat"
│       │       ├── MaterialDivider
│       │       ├── RecyclerView (id: rv_chat_preview, maxHeight = 300dp)
│       │       ├── View (id: typing_indicator_container, visible = GONE)
│       │       │   ├── ProgressBar (small, indeterminate)
│       │       │   └── TextView         "Agent is thinking..."
│       │       └── MaterialButton       "Expand to Full Chat" (↑ icon, text button)
│       │           android:onClick     → expandBottomSheet()
│       │
│       ├── MaterialCardView (id: card_cli_commands)
│       │   └── LinearLayout (vertical)
│       │       ├── TextView             "CLI Quick Commands"
│       │       ├── MaterialDivider
│       │       └── GridLayout (2 columns, 3 rows)
│       │           ├── MaterialButton "status"      style: outlined, compact
│       │           ├── MaterialButton "gateway"     style: outlined, compact
│       │           ├── MaterialButton "version"     style: outlined, compact
│       │           ├── MaterialButton "model list"  style: outlined, compact
│       │           ├── MaterialButton "restart"     style: outlined, compact
│       │           └── MaterialButton "logs"        style: outlined, compact
│       │
│       └── MaterialCardView (id: card_provider_model)
│           └── LinearLayout (vertical)
│               ├── TextView                       "Provider / Model"
│               ├── MaterialDivider
│               ├── TextView                       "Active Provider"
│               ├── LinearLayout (horizontal)
│               │   ├── TextView (id: tv_active_provider) "OpenAI / gpt-5.4"
│               │   └── IconButton (✏)            onClick → openProviderEdit()
│               ├── TextView                       "Available Models"
│               ├── ExposedDropdownMenu (id: dropdown_models)
│               │   └── TextInputLayout (style: outlined_exposed_dropdown)
│               │       └── AutoCompleteTextView  "Select a model..."
│               └── MaterialButton                 "Fetch Models" (🔄 icon)
│                   android:onClick               → fetchModelsFromProvider()
│
├── FloatingActionButton (id: fab_primary)
│   layout_gravity = bottom|end
│   srcCompat       = ▶ (agent stopped) or 💬 (agent running)
│   backgroundTint  = @color/status_running (green) or @color/primary
│   onClick         → toggleStartAgent / createNewChat
│
└── BottomSheetBehavior (id: bottom_sheet_chat, behavior_peekHeight = 0dp)
    └── ConstraintLayout (id: sheet_chat_container, elevation = 16dp)
        ├── View (id: drag_handle)                 Handle bar (4dp x 32dp, gray, centered top)
        ├── RecyclerView (id: rv_full_chat)        Full message list
        ├── LinearLayout (id: input_bar, layout_constraintBottom_toBottomOf)
        │   ├── TextInputLayout
        │   │   └── TextInputEditText (id: et_message)  "Type a message..."
        │   └── IconButton (id: btn_send)          "Send" ▶ icon
        └── ViewStub (id: stub_empty_chat)          Shown when no messages
```

---

## Data Flow

### ViewModels

| ViewModel | Responsibility | Key StateFlows |
|-----------|---------------|----------------|
| `CommandCenterViewModel` | Screen orchestrator, owns UI state | `uiState: StateFlow<CommandCenterUiState>` |
| `AgentChatViewModel` | Chat messages, send/receive | `messages: StateFlow<List<ChatMessage>>`, `isProcessing: StateFlow<Boolean>` |
| `ProviderModelViewModel` | Provider selection, model fetching | `activeProvider: StateFlow<ModelProvider?>`, `availableModels: StateFlow<List<String>>` |
| `GatewayStatusViewModel` | Gateway connectivity monitoring | `isGatewayConnected: StateFlow<Boolean>`, `gatewayPort: StateFlow<Int>` |

### Data Sources

```
┌─────────────────────────────────────────────────────────────┐
│                     DATA FLOW DIAGRAM                        │
│                                                              │
│  ┌──────────────┐    ┌──────────────────┐    ┌───────────┐  │
│  │ ProviderConfig│───▶│ProviderModelVM   │───▶│ UI: Card  │  │
│  │ Manager       │    │                  │    │ Provider  │  │
│  └──────────────┘    └────────┬─────────┘    └───────────┘  │
│                               │                              │
│                    fetchModels│ (HTTP GET /v1/models)        │
│                               ▼                              │
│  ┌──────────────┐    ┌──────────────────┐                   │
│  │ Provider API │───▶│availableModels   │──▶ dropdown       │
│  │ (OpenAI/etc) │    │list StateFlow    │                   │
│  └──────────────┘    └──────────────────┘                   │
│                                                              │
│  ┌──────────────┐    ┌──────────────────┐    ┌───────────┐  │
│  │ ServerManager│───▶│GatewayStatusVM   │───▶│ UI: Dot   │  │
│  │ (NanoHTTPD)  │    │                  │    │ + Chips   │  │
│  └──────────────┘    └──────────────────┘    └───────────┘  │
│                                                              │
│  ┌──────────────┐    ┌──────────────────┐    ┌───────────┐  │
│  │ Terminal     │───▶│AgentChatViewModel│───▶│ UI: Chat  │  │
│  │ Manager      │    │                  │    │ Recycler  │  │
│  │ (PicoClaw    │    │  collect output  │    │ + Preview │  │
│  │  PTY stdin/  │    │  parse messages  │    │ + Bottom  │  │
│  │  stdout)     │    │  update list     │    │ Sheet     │  │
│  └──────────────┘    └──────────────────┘    └───────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Chat Message Model

```kotlin
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,        // USER, AGENT, SYSTEM, ERROR
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false   // true while agent is still generating
)

enum class MessageRole { USER, AGENT, SYSTEM, ERROR }
```

### Command Center UI State

```kotlin
data class CommandCenterUiState(
    val isGatewayConnected: Boolean = false,
    val gatewayPort: Int = 0,
    val isAgentRunning: Boolean = false,
    val activeProvider: ModelProvider? = null,
    val availableModels: List<String> = emptyList(),
    val selectedModel: String? = null,
    val isFetchingModels: Boolean = false,
    val chatPreviewMessages: List<ChatMessage> = emptyList(),
    val isAgentProcessing: Boolean = false,
    val isBottomSheetExpanded: Boolean = false
)
```

---

## Edge Cases & State Management

### Gateway Status Dot (Toolbar)

| State | Dot Color | Subtitle Text | Chips Enabled? |
|-------|-----------|---------------|----------------|
| **Connected** | ● Green (`colorPrimary`) | "Active: {provider} / {model}" | All chips enabled |
| **Disconnected** | ● Red (`colorError`) | "Gateway Offline" | Gateway-dependant chips disabled (Agent, Gateway, Restart) |
| **Connecting** | ● Amber (`colorTertiary`) | "Connecting..." | All chips disabled |
| **Unknown** | ● Gray (`colorOutline`) | "Status Unknown" | All chips disabled |

### Agent Chat Card States

| State | Card Content | Typing Indicator | Expand Button |
|-------|-------------|------------------|---------------|
| **No Messages** | Empty-state illustration + "No messages yet. Start a conversation!" | Hidden | Hidden |
| **Has Messages** | Last 5 chat bubbles (RecyclerView) | Hidden | Visible |
| **Agent Processing** | Last 5 bubbles + typing indicator | Visible (animated dots) | Visible |
| **Error** | Error banner + last messages + "Retry" button | Hidden | Visible if messages exist |
| **Chat Loading** | Shimmer placeholder (4 skeleton bubble shapes) | Hidden | Hidden |

### CLI Quick Commands Card States

| State | Behavior |
|-------|----------|
| **Agent not running** | All buttons disabled + tooltip "Start agent to run commands" |
| **Agent running, not processing** | All buttons enabled, tap sends command to PTY stdin |
| **Agent processing** | All buttons disabled + tooltip "Wait for agent to finish" |
| **Command sent** | Brief flash animation on button + Snackbar "{command} sent" |

### Provider/Model Card States

| State | Content |
|-------|---------|
| **No provider configured** | "No provider configured" + "Add Provider" button → launches ProviderListActivity |
| **Provider configured, no models fetched** | Shows active provider name, empty model dropdown, "Fetch Models" button enabled |
| **Fetching models** | "Fetch Models" button shows ProgressBar (indeterminate, inline), dropdown disabled |
| **Models fetched (success)** | Dropdown populated with model list, first model auto-selected if no prior selection |
| **Models fetch (error)** | Dropdown disabled, error text "Failed to fetch: {reason}", "Retry" action button |
| **Model selected** | Selected model shown in dropdown, "Apply" button appears → restarts agent with new model |

### FAB State Transitions

```
  ┌────────────────────┐
  │  AGENT STOPPED      │   FAB = ▶ "Start Agent" (green)
  │                     │
  │  user taps FAB ─────┼──▶ agent starts loading
  └────────────────────┘
         │
         ▼
  ┌────────────────────┐
  │  AGENT RUNNING      │   FAB = 💬 "New Chat" (primary color)
  │  (idle)             │
  │                     │
  │  user taps FAB ─────┼──▶ scrolls to chat input, focuses keyboard
  └────────────────────┘
         │
         ▼
  ┌────────────────────┐
  │  AGENT PROCESSING   │   FAB = ■ "Stop" (red)
  │  (streaming)        │
  │                     │
  │  user taps FAB ─────┼──▶ sends SIGTERM to agent process
  └────────────────────┘
```

### Bottom Sheet States

| State | Behavior |
|-------|----------|
| **Collapsed** (peekHeight=0) | Invisible, drag handle hidden. Expand via: "Expand to Full Chat" button OR tap on chat card |
| **Half-Expanded** (50% screen) | Shows message list, input bar visible. Drag handle visible at top |
| **Fully Expanded** (95% screen) | Shows all messages, input bar pinned to bottom. Keyboard-aware (adjustResize) |
| **Hidden** (dismissable) | User swipes down or taps scrim to dismiss. State preserved on re-open |

---

## Interaction Details

### Quick-Action Chips Behavior

1. **"Start Agent"** — toggles agent process. When agent is running, changes to "Stop Agent" with ■ icon. When stopped, shows "Start Agent" with ▶ icon.
2. **"Gateway Status"** — opens a bottom sheet dialog showing gateway details: port, uptime, connected clients, request count. Includes "Restart Gateway" and "Test Connection" buttons.
3. **"Restart"** — sends SIGTERM to agent, waits 500ms, re-launches with same config. Shows Snackbar "Agent restarted successfully".
4. **"View Logs"** — launches `LogViewerActivity` with agent log file. If no log exists, shows Toast "No logs yet".
5. **"Providers"** — launches `ProviderListActivity`. On return (ActivityResult), refreshes active provider.

### Chat Message Bubble Rendering

```kotlin
// item_chat_message_user.xml (right-aligned, primary container)
┌──────────────────────────────┐
│  <message content>           │  ← backgroundTint = ?attr/colorPrimaryContainer
│  12:34 PM                    │  ← timestamp, textAppearanceBodySmall
└──────────────────────────────┘

// item_chat_message_agent.xml (left-aligned, surface variant)
┌──────────────────────────────┐
│  <message content>           │  ← backgroundTint = ?attr/colorSurfaceVariant
│  12:34 PM                    │
└──────────────────────────────┘
```

### Typing Indicator Animation

```
┌────────────────────────────────────┐
│  ●  ○  ○  ──▶  ○  ●  ○  ──▶  ○  ○  ●  │  ← 3 dots, sequential pulse animation
│  Agent is thinking...               │  ← optional text label
└────────────────────────────────────┘
```

Implementation: ValueAnimator cycling opacity of 3 small circles (8dp each) with 300ms stagger.

### CLI Command Execution Flow

1. User taps e.g. "status" button
2. Button briefly shows ripple + color change to indicate action sent
3. `terminalManager.writeToPty("status\n")` sends command to PicoClaw's PTY
4. AgentChatViewModel collects stdout, parses response
5. Response appears as new ChatMessage(role=AGENT) in the chat preview
6. "Expand to Full Chat" becomes visible (if it wasn't)

---

## Navigation Graph

```
CommandCenterActivity
│
├── [Expand Chat] ───▶ BottomSheetDialogFragment (inline, no navigation, handled by BottomSheetBehavior)
│
├── [Providers Chip/Card] ───▶ ProviderListActivity
│   │                           │
│   │                           ├── [EDIT] ───▶ ProviderEditDialog (BottomSheetDialogFragment)
│   │                           └── [ADD]  ───▶ ProviderEditDialog
│   │
│   └── [ActivityResult] ◀──── ProviderListActivity (returns updated provider)
│       ↪ refresh active provider in CommandCenterViewModel
│
├── [View Logs Chip] ───▶ LogViewerActivity (intent + log file URI extra)
│
├── [Settings (menu)] ───▶ ConfigActivity (existing)
│
└── [Gateway Status Chip] ───▶ GatewayStatusBottomSheet (inline BottomSheetDialogFragment)
```

---

## Theming (MD3 Color Roles)

| Element | MD3 Token |
|---------|-----------|
| App bar background | `?attr/colorSurface` |
| App bar title | `?attr/colorOnSurface` |
| Subtitle text | `?attr/colorOnSurfaceVariant` |
| Card backgrounds | `?attr/colorSurfaceContainerLow` |
| Card stroke | `@color/outlineVariant` (1dp) |
| User message bubble | `?attr/colorPrimaryContainer` |
| User message text | `?attr/colorOnPrimaryContainer` |
| Agent message bubble | `?attr/colorSurfaceVariant` |
| Agent message text | `?attr/colorOnSurfaceVariant` |
| FAB (start) | `?attr/colorPrimaryContainer` |
| FAB (stop) | `?attr/colorErrorContainer` |
| Gateway dot (connected) | `@color/status_running` (#4CAF50) |
| Gateway dot (disconnected) | `@color/status_error` (#F44336) |
| Typing indicator | `?attr/colorPrimary` at 60% alpha |
| Chips (normal) | `?attr/colorSecondaryContainer` |
| Chips (selected) | `?attr/colorPrimary` |

---

## Accessibility

- All image-only buttons have `android:contentDescription` set
- Chat messages announce role + content for TalkBack: "Agent says: How can I help?"
- Gateway dot has contentDescription: "Gateway connected" or "Gateway offline"
- FAB announces current action: "Start agent" or "Stop agent" or "New chat"
- Typing indicator announces: "Agent is thinking"
- All touch targets are minimum 48dp x 48dp
- Keyboard navigation supports d-pad + tab through all interactive elements

---

## Responsive Layout Notes

- **Phone portrait (default)**: Full stack layout as shown in wireframe. Cards stack vertically. NestedScrollView for full content.
- **Phone landscape**: Content width constrained to 600dp max, centered. Cards use 2-column grid where appropriate (e.g., CLI Commands stays 2x3). Chat preview uses wider bubbles.
- **Tablet/Foldable (>=600dp width)**: Two-pane layout — chat preview occupies left pane (60% width), CLI commands + Provider card occupy right pane (40%). Bottom sheet still overlays full screen when expanded.

---

## Files to Create

| File | Purpose |
|------|---------|
| `activity_command_center.xml` | Main layout — CoordinatorLayout with all sections |
| `item_chat_message_user.xml` | RecyclerView item — user message bubble (right-aligned) |
| `item_chat_message_agent.xml` | RecyclerView item — agent message bubble (left-aligned) |
| `item_chat_typing_indicator.xml` | View — animated typing indicator (3 dots) |
| `item_chat_empty_state.xml` | View — empty state illustration + "No messages yet" text |
| `layout_bottom_sheet_chat.xml` | BottomSheet content — full RecyclerView + input bar |
| `CommandCenterActivity.kt` | Activity — binds views, observes ViewModels, handles events |
| `CommandCenterViewModel.kt` | ViewModel — aggregates UI state from sub-ViewModels |
| `ChatAdapter.kt` | RecyclerView.Adapter — multi-viewtype (user/agent/typing/empty) |
| `model/ChatMessage.kt` | Data class — message model (id, role, content, timestamp, isStreaming) |
| `model/CommandCenterUiState.kt` | Data class — aggregated UI state |

## Files to Modify

| File | Change |
|------|--------|
| `AndroidManifest.xml` | Declare `CommandCenterActivity` as new LAUNCHER activity |
| `MainActivity.kt` | Redirect to `CommandCenterActivity` or deprecate (transitional) |
| `strings.xml` | Add strings for command center, chat, CLI commands, chip labels |
| `themes.xml` | Ensure MD3 color roles above are defined in the theme |

---

## Implementation Sequence

1. Create data models: `ChatMessage.kt`, `CommandCenterUiState.kt`
2. Create ViewModels: `CommandCenterViewModel.kt` (aggregates chat + provider + gateway state)
3. Create layout: `activity_command_center.xml` + all item/component layouts
4. Create `ChatAdapter.kt` with multi-viewtype support
5. Create `CommandCenterActivity.kt` wireup
6. Wire navigation from `MainActivity` (transitional launch target)
7. Add strings to `strings.xml`
8. Declare in `AndroidManifest.xml`
9. Instrumented tests: `CommandCenterActivityTest.kt`
