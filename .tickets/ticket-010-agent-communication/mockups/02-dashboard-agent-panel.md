# ClawDroid Dashboard + Agent Panel — UX Mockup

## Design Rationale

### Why a Dashboard + Agent Panel?
The current main screen (`activity_main.xml`) is a single-purpose control panel. Once PicoClaw is running, the user must leave the app to interact with the AI (currently via an external browser for Mission Control). This breaks immersion and forces context switching. A unified **Dashboard + Agent Panel** keeps all AI interaction inside the app: provider switching, chat conversations, CLI commands, and system health monitoring in a single cohesive screen.

### Why a Horizontal Provider Chip Row?
Provider switching (`openai/gpt-5.4` → `anthropic/claude-sonnet-4.6`) is a frequent action for power users testing prompt quality across backends. A horizontal scrollable chip row at the top of the screen makes this a one-tap operation. The active provider is highlighted with `primaryContainer` color; tapping a different provider triggers an immediate config reload and model list refresh.

### Why a Model Selector Dropdown?
Each provider exposes different models (e.g. `gpt-5.4`, `gpt-5.4-mini`, `claude-sonnet-4.6`, `claude-opus-4.5`). Users need to select a specific model before sending messages. Placing the dropdown directly below the provider chip row creates a natural visual flow: "Which provider?" → "Which model?" → "Start chatting."

### Why Three Tabs (Chat / CLI / Status)?
Users interact with an AI assistant in three distinct modes:
1. **Chat**: natural-language conversation with message bubbles, markdown rendering, and typing indicators — the primary use case.
2. **CLI**: direct terminal access to PicoClaw's gateway process for debugging, running raw commands, and viewing gateway stdout.
3. **Status**: at-a-glance system health for when things go wrong or the user wants to verify uptime, connections, and resource usage.

Tabs prevent screen clutter by showing only the mode the user currently needs. `TabLayout` + `ViewPager2` is the standard Android pattern users already understand.

### Why a Monospace Terminal in the CLI Tab?
The CLI tab inherits the **Minimalist Terminal** design language from ticket-008. The green-on-black monospace terminal is the signature ClawDroid aesthetic. Predefined command chips (`status`, `version`, `gateway`, `restart`) above the terminal reduce typing on mobile keyboards while keeping the terminal feel authentic.

---

## User Flow

### Cold Start → Chat
1. User opens app → sees Dashboard with provider chip row at top.
2. Default provider (`OpenAI`) is highlighted; its models populate the dropdown below.
3. **Chat tab** is selected by default.
4. Chat area shows an empty-state placeholder: "Ask PicoClaw anything…" with a blinking cursor in the input bar.
5. User types a message → sends → sees their message bubble appear on the right.
6. Typing indicator (three animated dots) appears on the left while PicoClaw processes.
7. Response appears as a left-aligned bubble with rendered markdown (headings, code blocks, lists, bold/italic).
8. User can scroll up through conversation history.

### Switching Provider Mid-Session
1. User taps `DeepSeek` chip in the horizontal scroll row.
2. Active chip highlight animates from OpenAI to DeepSeek via `MaterialContainerTransform`.
3. Model dropdown repopulates with DeepSeek models (`deepseek-chat`, `deepseek-reasoner`).
4. Existing chat history remains; new messages will use the new provider+model.
5. A subtle Snackbar: "Switched to DeepSeek • deepseek-chat".

### CLI Tab Interaction
1. User taps **CLI** tab → `ViewPager2` swipes horizontally to the terminal view.
2. Terminal canvas is green-on-black monospace, auto-scrolled to bottom.
3. Four predefined command chips sit above the terminal: `[status] [version] [gateway] [restart]`.
4. User taps `[status]` → the command `picoclaw status` is executed in the terminal session, output streams in real time.
5. User can type freeform commands in the input field below the terminal.
6. Long-pressing the terminal canvas copies selected text to clipboard.

### Status Tab — Health Monitoring
1. User taps **Status** tab.
2. **Gateway Health Card**: green "HEALTHY" chip or red "UNHEALTHY" chip with last-check timestamp.
3. **Uptime Card**: days/hours/minutes since PicoClaw started, with a subtle pulsing green dot when running.
4. **Active Connections**: number of concurrent WebSocket/HTTP connections to the gateway.
5. **CPU/Memory Gauge**: two `LinearProgressIndicator` bars (determinate) showing PicoClaw process CPU% and RSS memory usage.
6. Pull-to-refresh updates all status metrics.

### Error States
- **Gateway unreachable**: Status tab shows red "UNHEALTHY" chip with "Last seen 2m ago". A "Restart Gateway" button appears inline.
- **Model not available**: Chat input bar disables; a yellow banner appears: "Model 'gpt-5.4' not available. Select another model."
- **No providers configured**: Provider chip row shows a single `[+ Add Provider]` chip that opens the Provider Management screen from ticket-009.

---

## Component Choices

| Section | Component | Rationale |
|---------|-----------|-----------|
| Root Layout | `CoordinatorLayout` | Anchors FAB, handles keyboard inset for chat input, supports `AppBarLayout` scroll behavior |
| App Bar | `MaterialToolbar` | Title "ClawDroid", overflow menu (Settings, Logs, About) |
| Provider Row | `RecyclerView` (horizontal, `LinearLayoutManager`) | Efficient scrolling for N providers; each item is a `MaterialChip` |
| Model Dropdown | `ExposedDropdownMenuBox` + `MaterialAutoCompleteTextView` | MD3 standard for single-select from a list; filters as user types |
| Tab Host | `TabLayout` (inside `AppBarLayout` for scroll-collapse) | Standard Material Design tab bar with `tabIndicator` in `colorPrimary` |
| Tab Content | `ViewPager2` | Swipeable pages; each page is a `Fragment` (`ChatFragment`, `CliFragment`, `StatusFragment`) |
| Chat Messages | `RecyclerView` with `LinearLayoutManager` (vertical, stacked from bottom) | Efficient message list; `RecyclerView.adapter` handles left/right layouts per message role |
| Message Bubble | `MaterialCardView` (12dp radius) | User messages: `primaryContainer` background, right-aligned. Agent messages: `surfaceVariant` background, left-aligned |
| Typing Indicator | `ViewStub` → 3 `View` dots with `ObjectAnimator` alpha pulse | Familiar chat UX; inflates only when agent is processing |
| Markdown Rendering | `Markwon` library `MarkwonTextView` | Handles code fences, bold, italic, lists, links; lightweight (~200KB) |
| Chat Input Bar | `MaterialTextInputLayout` + `MaterialButton` (send icon) | Stays pinned to bottom via `ConstraintLayout`; send button enabled only when text is non-blank |
| Terminal Canvas | `ScrollView` → `TextView` (monospace, `#00FF41` on `#0A0A0A`) | Matches Minimalist Terminal mockup from ticket-008 |
| Command Chips | `MaterialChip` (choice style) inside `HorizontalScrollView` | Predefined commands; tapping one sends the string to the terminal input |
| Terminal Input | `EditText` (monospace, no background, green text) | Inline command input; Enter key submits |
| Status Cards | `MaterialCardView` (8dp radius, 2dp elevation) | Groups related health metrics into scannable cards |
| CPU/Memory Gauge | `LinearProgressIndicator` (determinate, 8dp track) | Visualizes resource usage without a custom charting library |
| Health Dot | `ImageView` with `AnimatedVectorDrawable` | Green pulsing dot for running, static red for unhealthy |
| Primary FAB | `FloatingActionButton` (bottom-end) | Quick-start PicoClaw from any tab; changes icon/color per process state |
| Pull-to-Refresh | `SwipeRefreshLayout` wrapping `ViewPager2` | Refresh status metrics in the Status tab |

---

## Layout Architecture

### Full Activity Layout Hierarchy

```
CoordinatorLayout (root)
├── AppBarLayout
│   ├── MaterialToolbar
│   │   ├── title: "ClawDroid"
│   │   └── overflowMenu: Settings, View Logs, About
│   ├── ProviderChipRow
│   │   └── RecyclerView (horizontal, orientation=HORIZONTAL)
│   │       └── MaterialChip (per provider) [OpenAI] [Anthropic] [DeepSeek] [+]
│   ├── ModelSelectorRow
│   │   └── ExposedDropdownMenuBox
│   │       └── MaterialAutoCompleteTextView (hint: "Select model…")
│   └── TabLayout
│       ├── Tab: "Chat"    (icon: chat_bubble_outline)
│       ├── Tab: "CLI"     (icon: terminal)
│       └── Tab: "Status"  (icon: monitor_heart)
├── SwipeRefreshLayout
│   └── ViewPager2
│       ├── Fragment 0: ChatFragment
│       ├── Fragment 1: CliFragment
│       └── Fragment 2: StatusFragment
└── FloatingActionButton (layout_anchor="@id/view_pager", layout_anchorGravity="bottom|end")
```

### Chat Tab (ChatFragment)

```
ConstraintLayout
├── RecyclerView (chat messages)                  id: chat_recycler
│   ├── [User Message Bubble]  (right-aligned, primaryContainer bg)
│   ├── [Agent Message Bubble] (left-aligned, surfaceVariant bg, Markwon rendered)
│   ├── [Typing Indicator]     (3 dots, alpha animation)
│   └── [Empty State]          (center, icon + "Ask PicoClaw anything…")
└── ChatInputBar (bottom, constrained to parent bottom)
    ├── MaterialTextInputLayout
    │   └── EditText (hint: "Message PicoClaw…", multiline, maxLines=5)
    └── ImageButton (send icon, 48dp, tint=colorPrimary)
```

### CLI Tab (CliFragment)

```
ConstraintLayout
├── CommandChipRow
│   └── HorizontalScrollView
│       └── LinearLayout (horizontal)
│           ├── MaterialChip: "status"
│           ├── MaterialChip: "version"
│           ├── MaterialChip: "gateway"
│           └── MaterialChip: "restart"
├── TerminalCanvas (FrameLayout, 0dp → constrained to parent)
│   └── ScrollView
│       └── TextView (id: terminal_output, monospace 11sp, #00FF41 on #0A0A0A)
└── TerminalInputBar
    ├── TextView (prompt ">_", monospace, green)
    └── EditText (id: terminal_input, monospace, singleLine, green text, no background)
```

### Status Tab (StatusFragment)

```
SwipeRefreshLayout
└── NestedScrollView
    └── LinearLayout (vertical, 16dp padding)
        ├── Gateway Health Card
        │   ├── CardHeader: "Gateway Health"
        │   ├── HealthChip: "HEALTHY" (green) or "UNHEALTHY" (red)
        │   ├── LastCheck: "Checked 5s ago"
        │   └── RestartButton (visible only when unhealthy)
        ├── Uptime Card
        │   ├── CardHeader: "Uptime"
        │   ├── UptimeText: "2d 14h 33m"
        │   └── RunningDot (green pulsing AVD)
        ├── Connections Card
        │   ├── CardHeader: "Active Connections"
        │   └── ConnectionCount: "3" (headlineLarge) + "WebSocket"
        ├── Resources Card
        │   ├── CardHeader: "Resources"
        │   ├── CpuLabel: "CPU 12%"
        │   └── CpuGauge: LinearProgressIndicator (determinate=0.12)
        │   ├── MemLabel: "Memory 48 MB"
        │   └── MemGauge: LinearProgressIndicator (determinate=0.48 of process limit)
        └── Process Info Card
            ├── CardHeader: "Process"
            ├── PidRow: "PID: 12345"
            ├── PortRow: "Port: 8080"
            └── BinaryRow: "Binary: picoclaw-arm64 v2.1.0"
```

---

## ASCII Wireframe

```
┌──────────────────────────────────────────────────────┐
│  ☰  ClawDroid                          ⋮ (overflow)  │  ← MaterialToolbar
├──────────────────────────────────────────────────────┤
│  [OpenAI] [Anthropic] [DeepSeek] [Gemini] [+]  →     │  ← ProviderChipRow (RecyclerView horizontal)
├──────────────────────────────────────────────────────┤
│  Model: [gpt-5.4                    ▼]               │  ← ExposedDropdownMenu
├──────────────────────────────────────────────────────┤
│  [ 💬 Chat ]  [ >_ CLI ]  [ ♥ Status ]               │  ← TabLayout
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌─ CHAT TAB ────────────────────────────────────┐  │
│  │                                                │  │
│  │  ┌──────────────────────────┐                  │  │
│  │  │ How do I set up a        │  (user bubble)   │  │
│  │  │ reverse proxy?           │  right, primary  │  │
│  │  └──────────────────────────┘                  │  │
│  │                                                │  │
│  │  ┌──────────────────────────┐                  │  │
│  │  │ ## Reverse Proxy Setup   │  (agent bubble)  │  │
│  │  │                          │  left, surface   │  │
│  │  │ 1. Install nginx         │  markdown        │  │
│  │  │ 2. Configure upstream    │  rendered        │  │
│  │  │ ```nginx                 │                  │  │
│  │  │ server { listen 80; ...  │  code fence      │  │
│  │  │ ```                      │                  │  │
│  │  └──────────────────────────┘                  │  │
│  │                                                │  │
│  │  ● ● ●  (typing indicator, alpha pulsing)      │  │
│  │                                                │  │
│  ├────────────────────────────────────────────────┤  │
│  │ ┌──────────────────────────────┐ [➤]           │  │  ← InputBar
│  │ │ Message PicoClaw…            │               │  │
│  │ └──────────────────────────────┘               │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  ┌─ CLI TAB ─────────────────────────────────────┐  │
│  │  [status] [version] [gateway] [restart]  →     │  │  ← CommandChipRow
│  │ ┌──────────────────────────────────────────────┐│  │
│  │ │ >_ picoclaw status                           ││  │
│  │ │                                              ││  │
│  │ │ Gateway: RUNNING                             ││  │
│  │ │ Uptime: 2d 14h 33m                           ││  │
│  │ │ Connections: 3 active                        ││  │
│  │ │ CPU: 12%  Memory: 48 MB                      ││  │
│  │ │                                              ││  │
│  │ │ >_ █                                         ││  │  ← Terminal Canvas
│  │ └──────────────────────────────────────────────┘│  │
│  │ >_ ▌                                          │  │  ← Terminal Input
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  ┌─ STATUS TAB ──────────────────────────────────┐  │
│  │                                                │  │
│  │  ┌─ Gateway Health ───────────────────────┐   │  │
│  │  │  ● HEALTHY         Checked 5s ago       │   │  │
│  │  └─────────────────────────────────────────┘   │  │
│  │                                                │  │
│  │  ┌─ Uptime ───────────────────────────────┐   │  │
│  │  │  ● 2d 14h 33m                           │   │  │
│  │  └─────────────────────────────────────────┘   │  │
│  │                                                │  │
│  │  ┌─ Active Connections ───────────────────┐   │  │
│  │  │  3  WebSocket connections               │   │  │
│  │  └─────────────────────────────────────────┘   │  │
│  │                                                │  │
│  │  ┌─ Resources ────────────────────────────┐   │  │
│  │  │  CPU 12%   ▓▓▓░░░░░░░░░░░░░░░░░░░      │   │  │
│  │  │  MEM 48MB  ▓▓▓▓░░░░░░░░░░░░░░░░░░      │   │  │
│  │  └─────────────────────────────────────────┘   │  │
│  │                                                │  │
│  │  ┌─ Process Info ─────────────────────────┐   │  │
│  │  │  PID: 12345   Port: 8080               │   │  │
│  │  │  Binary: picoclaw-arm64 v2.1.0          │   │  │
│  │  └─────────────────────────────────────────┘   │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│                                              [▶ FAB] │  ← FloatingActionButton
└──────────────────────────────────────────────────────┘
```

---

## ViewModel & Data Flow

### Single Activity, Multiple Fragments

```
DashboardActivity
├── DashboardViewModel (shared via activityViewModels)
│   ├── providers: StateFlow<List<ModelProvider>>
│   ├── activeProvider: StateFlow<ModelProvider?>
│   ├── models: StateFlow<List<String>>
│   ├── selectedModel: StateFlow<String?>
│   ├── chatMessages: StateFlow<List<ChatMessage>>
│   ├── isAgentTyping: StateFlow<Boolean>
│   ├── terminalOutput: StateFlow<String>
│   ├── serverStatus: StateFlow<ServerStatus>
│   ├── processInfo: StateFlow<PicoClawProcessMetrics>
│   ├── selectProvider(provider: ModelProvider)
│   ├── selectModel(model: String)
│   ├── sendMessage(text: String)
│   ├── executeCliCommand(command: String)
│   └── refreshStatus()
│
├── ChatFragment observes: chatMessages, isAgentTyping, selectedModel
├── CliFragment observes: terminalOutput
└── StatusFragment observes: serverStatus, processInfo
```

### Data Sources
- **Provider list**: `ProviderConfigManager` reads `config.json` → `model_list`
- **Model list**: fetched from provider API or hardcoded per provider
- **Chat messages**: WebSocket connection to PicoClaw gateway (`ws://localhost:8080/chat`)
- **CLI output**: PicoClaw process stdout piped via `ProcessMonitor`
- **Status metrics**: HTTP GET `http://localhost:8080/health` + `/proc/[pid]/stat` parsing

---

## Edge Cases

| Edge Case | Handling |
|-----------|----------|
| Provider chip row has 1 item | No horizontal scroll; chip fills available width with minWidth=120dp |
| Provider chip row has 0 items | Show placeholder chip: `[+ Add Provider]` that navigates to `ProviderListActivity` |
| Model API call fails | Dropdown shows cached model list with a yellow "Could not refresh" chip below |
| Chat history exceeds 500 messages | `RecyclerView` with `PagingDataAdapter` or capped `DiffUtil` list at 1000 items; older messages pruned from memory |
| User rotates device mid-chat | `DashboardViewModel` survives config change via `activityViewModels`; `RecyclerView` scroll position restored via `savedInstanceState` |
| Keyboard opens on Chat tab | `windowSoftInputMode="adjustResize"` pushes input bar and shrinks chat area; message list auto-scrolls to bottom |
| Terminal output exceeds 10,000 lines | `TextView` buffer capped at 2,000 lines; older lines dropped from top with a dimmed separator: `--- earlier output truncated ---` |
| Gateway crashes while on Status tab | `SwipeRefreshLayout` shows error state; health chip turns red; "Restart Gateway" button becomes visible |
| No network / local-only provider | Chat still works if provider is `lmstudio-local`; no special handling needed beyond API reachability |
| Very long agent response (10KB+) | `Markwon` renders progressively; `RecyclerView` handles large text via view recycling |
| Tab selected before PicoClaw starts | CLI and Status tabs show empty-state placeholders: "Start PicoClaw to see terminal output" / "Start PicoClaw to see system status" |
| User sends empty message | Send button is disabled when input text is blank after trimming |
| Markdown contains malicious JS/HTML | `Markwon` renders only; no WebView involved. Harmless — rendered as escaped text. |

---

## Accessibility Considerations

- **Provider chips**: `contentDescription="Select provider: OpenAI"` announces current state per chip
- **Tab navigation**: `TabLayout` tabs have `contentDescription` matching label text
- **Chat messages**: `contentDescription` on each bubble announces role + truncated preview: "You said: How do I…" / "PicoClaw said: ## Reverse Proxy Setup…"
- **Typing indicator**: `accessibilityLiveRegion="polite"` on the typing view announces "PicoClaw is typing" when visible
- **Send button**: `contentDescription="Send message"`; disabled state is properly communicated
- **Terminal output**: `contentDescription="Terminal output. Last line: Gateway RUNNING"` updates on content change
- **Command chips**: each chip has `contentDescription` matching the command it executes
- **Status gauges**: `ProgressBar` has `accessibilityLabel` with percentage text; numeric text is shown adjacent for screen-reader-agnostic users
- **Minimum touch targets**: all interactive elements (chips, buttons, tabs) are 48dp minimum

---

## Responsive Behavior

- **Phones (< 360dp)**: Single-column layout. Provider chips scroll horizontally. Chat bubbles use 85% of screen width. FAB at 56dp.
- **Phones (360–600dp)**: Same layout; chat bubbles use 75% of screen width.
- **Tablets / Foldables (sw600dp)**: Provider chips may wrap to 2 rows or remain single-row. Status tab uses 2-column grid layout (health + uptime side-by-side). FAB at 72dp.
- **Landscape**: `ViewPager2` pages adjust to fill height. Chat input bar stays pinned to bottom. Terminal canvas takes full width.
- **Multi-window / split-screen**: `ViewPager2` pages remain functional; layout adapts to constrained height.

---

## Theme & Theming

All components use Material Design 3 attributes, inheriting from `Theme.Material3.DayNight.NoActionBar`:

- **Light theme**: `surface` for card backgrounds, `surfaceVariant` for agent bubbles, `primaryContainer` for user bubbles, `onSurface` for text
- **Dark theme**: Cards shift to elevated surfaces; monospace terminal remains `#00FF41` on `#0A0A0A` regardless of theme (authentic terminal look)
- **Dynamic Color (Android 12+)**: Provider chips and FAB adapt to wallpaper-derived palette; terminal colors are intentionally fixed for authenticity
- **Status tab colors**: semantic green/red/yellow chips retain meaning in both themes via `MaterialColors` token usage

---

## Migration Notes for Developers

1. **New layout file**: `activity_dashboard.xml` (replaces `activity_main.xml` as main launcher)
2. **New Fragments**:
   - `ChatFragment.kt` + `fragment_chat.xml`
   - `CliFragment.kt` + `fragment_cli.xml`
   - `StatusFragment.kt` + `fragment_status.xml`
3. **New ViewModel**: `DashboardViewModel.kt` (shared across fragments via `activityViewModels`)
4. **New data models**:
   - `ChatMessage.kt` — data class (role: User/Agent, content: String, timestamp: Long)
   - `PicoClawProcessMetrics.kt` — data class (cpuPercent: Float, memoryKb: Long, connections: Int)
5. **New Adapter**: `ChatAdapter.kt` — `ListAdapter` with `DiffUtil.ItemCallback<ChatMessage>`
6. **New dependency**: `Markwon` for markdown rendering (`io.noties.markwon:core:4.6.2`)
7. **Modify `MainActivity.kt`**: rename to `DashboardActivity.kt` (or refactor in place); wire up `TabLayout` + `ViewPager2` + `DashboardViewModel`
8. **Modify `AndroidManifest.xml`**: update launcher activity class reference
9. **String resources**: See companion file `strings-needed.md`
10. **Color resources**: See companion file `colors-needed.md`
11. **Keep existing**: `ProviderListActivity`, `MissionControlActivity`, `LogViewerActivity`, `ConfigActivity` remain as secondary activities launched from overflow menu

---

## Files Delivered

1. `02-dashboard-agent-panel.md` — this document
2. `activity_dashboard.xml` — complete Android XML layout (to be created during implementation)
3. `fragment_chat.xml` — Chat tab layout
4. `fragment_cli.xml` — CLI tab layout
5. `fragment_status.xml` — Status tab layout
6. `strings-needed.md` — all new string resources required
7. `colors-needed.md` — color and theme additions required

---

## Open Questions for Engineering

1. Should the WebSocket chat connection persist across tab switches, or disconnect/reconnect?
2. Should we persist chat history to disk (Room/SQLite) for session restoration?
3. Should the terminal accept ANSI escape sequences for colored output, or stay pure green-on-black?
4. Should the Status tab auto-refresh on a timer (e.g., every 5s) or only on manual pull-to-refresh?
5. Should the FAB be visible on the CLI and Status tabs, or only on the Chat tab?
