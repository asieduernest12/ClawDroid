# Chat-First Terminal — UX Mockup

## Design Rationale

### Problem Statement
The current `MainActivity` is a status dashboard (control center style). Users see health indicators and status chips but have **no way to interact with the AI agent directly** from the main screen. To chat with PicoClaw, they must open Mission Control in a WebView — a secondary navigation hop that breaks the native feel. There is no unified view for chat + terminal inspection.

### Design Direction: "Chat-First Terminal"
A single-screen architecture where **chat is the hero** (70% of screen) and the **PicoClaw gateway terminal is a collapsible inspector** (30% peek). Inspired by WhatsApp/Telegram for chat, and Android Studio's bottom Logcat for the terminal. The user can switch providers/models from the top bar without leaving the conversation. This screen becomes the default landing after bootstrap completes.

---

## ASCII Wireframe

```
┌──────────────────────────────────────────────────────┐
│  [▼ OpenAI        ]  [▼ gpt-5.4          ]  [⋮ menu]│  ← AppBarLayout > MaterialToolbar
│  ────────────────────────────────────────────────────│    provider dropdown + model dropdown + overflow
│                                                       │
│  ┌──────────────────────────────────────────────────┐ │
│  │                                                  │ │
│  │           ▲ Chat Area (70% of screen)            │ │  ← CoordinatorLayout main content
│  │           │  RecyclerView                        │ │
│  │                                                  │ │
│  │  ┌─────────────────────────────┐                │ │
│  │  │  Hello! How can I help     │                │ │  ← Agent message (left, avatar)
│  │  │  you today?                │                │ │    MaterialCardView, 12dp radius
│  │  └─────────────────────────────┘                │ │    surfaceVariant bg
│  │                                   ┌───────────┐ │ │
│  │                                   │ What is   │ │ │  ← User message (right, no avatar)
│  │                                   │ PicoClaw? │ │ │    primary color bg, white text
│  │                                   └───────────┘ │ │
│  │  ┌─────────────────────────────┐                │ │
│  │  │  PicoClaw is a lightweight  │                │ │  ← Agent reply with markdown
│  │  │  AI assistant that runs     │                │ │    (bold, code blocks, lists)
│  │  │  entirely on your device.   │                │ │
│  │  │                             │                │ │
│  │  │  Features:                  │                │ │
│  │  │  - <10MB RAM               │                │ │     markdown-bullet rendering
│  │  │  - No cloud required       │                │ │
│  │  │  - Private by design       │                │ │
│  │  └─────────────────────────────┘                │ │
│  │                                                  │ │
│  │  ┌──────────────────────────────────────────────┐│ │
│  │  │  Type your message...              [▶ SEND]  ││ │  ← TextInputLayout + IconButton
│  │  └──────────────────────────────────────────────┘│ │    anchored at bottom of chat area
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ ⬡ Terminal (PicoClaw Gateway) ─────── [▲ hide] ─┐│  ← BottomSheet peek (chevron toggle)
│  │                                                    ││    drag handle (4dp x 32dp) centered
│  │  [gateway] 2026-05-17 10:23:01 INFO  Starting...  ││  ← Read-only terminal output
│  │  [gateway] 2026-05-17 10:23:02 INFO  Listening... ││    monospace, 12sp, green-on-black
│  │  [gateway] 2026-05-17 10:23:05 DEBUG Tool call... ││    RecyclerView, auto-scroll
│  │  [gateway] 2026-05-17 10:23:06 INFO  Response ... ││
│  │  ──────────────────────────────────────────────── ││
│  │  ┌──────────────────────────────────────────────┐││
│  │  │ > _                                          │││  ← Raw command input (optional)
│  │  └──────────────────────────────────────────────┘││    send raw commands to PicoClaw stdin
│  └────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────┘
```

---

## Widget Hierarchy (Material Design 3)

```
CoordinatorLayout                                 ← root; manages AppBar scroll + BottomSheet
├── AppBarLayout                                   ← collapses toolbar on scroll for more chat space
│   └── MaterialToolbar
│       ├── ExposedDropdownMenuBox (provider)      ← filtered from config.json model_list
│       │   └── TextInputLayout                     ← editable, filterable by display name
│       │       └── ExposedDropdownMenu              ← "OpenAI", "Anthropic", "DeepSeek", ...
│       ├── ExposedDropdownMenuBox (model)          ← fetched dynamically from provider API
│       │   └── TextInputLayout                     ← "gpt-5.4", "claude-sonnet-4.6", ...
│       │       └── ExposedDropdownMenu
│       └── Overflow Menu (3-dot ⋮)
│           ├── "Start Agent" / "Stop Agent"        ← toggled based on agent running state
│           ├── "Clear Chat"
│           ├── "Export Chat"                       ← shares as .txt / .md file
│           └── "Settings"                          ← opens ConfigActivity
│
├── LinearLayout (main content, weight=0.7)         ← or ConstraintLayout with layout_constraintHeight_percent
│   ├── RecyclerView                                ← chat messages; reverseLayout=true (newest at bottom)
│   │   ├── item_message_agent.xml                  ← left-aligned, avatar + bubble + timestamp
│   │   │   ├── CircleImageView (24dp avatar)       ← provider icon or letter-avatar (e.g. "O" for OpenAI)
│   │   │   ├── MaterialCardView (bubble)           ← surfaceVariant bg, rounded corners (12dp)
│   │   │   │   └── Markwon TextView               ← renders markdown: bold, italic, code, lists, links
│   │   │   └── TextView (timestamp)                ← 10sp, onSurfaceVariant, "10:23 AM"
│   │   └── item_message_user.xml                  ← right-aligned, bubble + timestamp, no avatar
│   │       ├── MaterialCardView (bubble)           ← primary color bg, white text, rounded (12dp)
│   │       │   └── TextView                        ← plain text (user messages don't need markdown)
│   │       └── TextView (timestamp)
│   │
│   └── LinearLayout (input bar)                    ← pinned below RecyclerView
│       ├── IconButton (attach)                      ← optional file/context attachment (future)
│       ├── TextInputLayout                          ← filled style, endIconMode = send
│       │   └── TextInputEditText                    ← "Type your message..."
│       └── IconButton (send)                        ← primary color, `ic_send`
│
└── FrameLayout (bottom_sheet, id="@id/terminal_sheet")  ← BottomSheetBehavior attached
    └── LinearLayout
        ├── View (drag handle)                       ← 4dp x 32dp, centered, rounded, surfaceVariant
        ├── LinearLayout (header)
        │   ├── TextView "Terminal (PicoClaw Gateway)"
        │   ├── Chip "RUNNING" / "STOPPED"           ← status indicator
        │   └── IconButton (chevron toggle)           ← ▲ hides sheet to peek=0; ▼ shows sheet to peek=30%
        ├── RecyclerView (log lines)                  ← monospace, read-only, auto-scroll
        │   └── item_terminal_line.xml
        │       └── TextView (monospace 12sp, #00FF00 on #000000)
        ├── View (divider 1dp)
        └── TextInputLayout (command input)
            └── TextInputEditText                     ← "> send raw command..." (max 2 lines)
                -> sends to PicoClaw process stdin via ProcessMonitor.writeStdin()
```

### Layout File Plan
- `activity_chat_terminal.xml` — root CoordinatorLayout
- `item_message_agent.xml` — agent chat bubble layout
- `item_message_user.xml` — user chat bubble layout
- `item_terminal_line.xml` — single terminal log line

---

## Component Choices

| UI Element | Component | Rationale |
|------------|-----------|-----------|
| Root layout | `CoordinatorLayout` | Manages AppBar scroll-behavior + BottomSheet simultaneously |
| Top bar | `AppBarLayout` + `MaterialToolbar` | Collapses on scroll-up for more chat space; houses dropdown spinners |
| Provider dropdown | `ExposedDropdownMenuBox` + `TextInputLayout` | Material 3 standard; filterable; backed by live data from ProviderConfigManager |
| Model dropdown | `ExposedDropdownMenuBox` + `TextInputLayout` | Dynamic content populated after provider selection via API call |
| Chat messages | `RecyclerView` with `LinearLayoutManager` (reverseLayout=true) | WhatsApp-style bottom-anchored scrolling; ViewHolder recycling for large histories |
| Message bubbles | `MaterialCardView` (12dp radius) | Elevation=1dp for subtle depth; tinted per sender |
| Markdown rendering | `Markwon` (io.noties.markwon) | Lightweight (<200KB), supports GitHub-flavored markdown, code blocks, tables |
| Input bar | `TextInputLayout` (filled) + `IconButton` | Standard send-message pattern; `ic_send` from Material Icons |
| Terminal panel | `BottomSheetBehavior<FrameLayout>` | Peek height = 30% of screen; expands to full-screen on drag; standard Android pattern |
| Terminal log | `RecyclerView` with `LinearLayoutManager` (stacked from top) | Efficient for large logs; auto-scrolls to bottom on new lines |
| Terminal style | `TextView` monospace 12sp, `#00FF00` on `#1E1E1E` | Classic terminal aesthetic; dark background prevents burn-in on OLED |
| Drag handle | `View` 4dp x 32dp with rounded corners | Material 3 bottom sheet handle pattern |
| Send raw command | `TextInputEditText` → `ProcessMonitor.writeStdin()` | Writes to PicoClaw process stdin; same mechanism as embedded Termux session |
| Avatars | `CircleImageView` (24dp) or `MaterialCardView` circle with letter | Provider letter-avatar (first char of display name) |

---

## Data Flow

### 1. Provider List Loading

```
ProviderConfigManager.loadProviders()
  → reads files/picoclaw/config.json
  → parses model_list array
  → returns List<ModelProvider>
  → populates provider dropdown (unique modelNames)
  → selects first as default
```

### 2. Model List Fetching (Dynamic)

```
User selects provider dropdown item
  → reads ModelProvider.apiBase + ModelProvider.apiKey
  → sends GET {apiBase}/models with Authorization: Bearer {apiKey}
  → parses JSON response (OpenAI-compatible /v1/models format)
  → extracts model IDs: ["gpt-5.4", "gpt-5.4-turbo", ...]
  → populates model dropdown
  → selects first model as default (or remembers last-selected per provider)

If fetch fails:
  → fallback: show raw model identifier from model_list entry
  → show Toast: "Could not fetch models. Using configured default."
  → retry on dropdown re-open
```

**API response format (expected)**:
```json
{
  "object": "list",
  "data": [
    { "id": "gpt-5.4", "object": "model", ... },
    { "id": "gpt-5.4-turbo", "object": "model", ... }
  ]
}
```

Providers that do NOT support the `/v1/models` endpoint (or don't expose it) fall back to the single `ModelProvider.model` identifier string. The dropdown shows the single entry as read-only.

### 3. Chat Message Flow

```
User types message → taps Send (or keyboard actionDone)
  → creates ChatMessage(role=SELF, content=text, timestamp=now)
  → adds to ChatViewModel.messages LiveData
  → RecyclerView scrolls to bottom

  → sends POST http://localhost:{picoClawPort}/v1/chat/completions
      {
        "model": "<selected model ID>",
        "messages": [
          ...history (last N messages for context),
          { "role": "user", "content": "<user text>" }
        ],
        "stream": true
      }

  → reads SSE (Server-Sent Events) stream
  → each chunk: { "choices": [{ "delta": { "content": "..." } }] }
  → appends delta to current ChatMessage(role=AGENT, content=accumulated, isStreaming=true)
  → RecyclerView updates in-place (no flicker)

  → on stream end (data: [DONE]):
      marks ChatMessage.isStreaming = false
      adds timestamp

If HTTP call fails:
  → creates ChatMessage(role=SYSTEM, content="Error: ...")
  → shows in chat as centered system message (gray, italic)
```

### 4. Terminal Log Flow

```
PicoClaw gateway process writes to stdout/stderr
  → ProcessMonitor reads lines from process InputStream
  → appends to LogLine(timestamp, level, message)
  → TerminalViewModel.logLines LiveData updated
  → Terminal RecyclerView auto-scrolls to bottom
  
If user sends raw command in terminal input:
  → ProcessMonitor.writeStdin(command + "\n")
  → command line appears in terminal output (prefixed with "> ")
  → process response appears as subsequent log lines
```

### 5. Provider/Model Selection Flow

```
ProviderChanged or ModelChanged
  → updates ChatViewModel.selectedProvider / selectedModel
  → if agent was running and conversation exists:
      shows Snackbar: "Switch provider to finish this conversation?"
      "Switch" action → resets conversation context, sends next message with new provider
      "Cancel" action → reverts dropdown to previous selection
  → if no active conversation:
      silently updates selection for next message
```

### 6. Agent Lifecycle (Start/Stop)

```
Menu: "Start Agent"
  → TerminalManager.launchPicoClaw()
  → if already running: no-op
  → if not running: starts gateway process
  → terminal panel shows startup logs
  → menu item toggles to "Stop Agent"
  → send button enables

Menu: "Stop Agent"
  → TerminalManager.stopPicoClaw()
  → terminal panel shows shutdown logs
  → menu item toggles to "Start Agent"
  → send button disables (shown as gray with tooltip "Agent is stopped")
  → if messages exist, shows "Agent stopped" system message in chat
```

### 7. Export Chat Flow

```
Menu: "Export Chat"
  → iterates ChatViewModel.messages
  → formats as markdown:
      # ClawDroid Chat — 2026-05-17
      **Provider:** OpenAI | **Model:** gpt-5.4
      
      ## User — 10:23 AM
      What is PicoClaw?
      
      ## Agent (OpenAI / gpt-5.4) — 10:23 AM
      PicoClaw is a lightweight AI assistant...

  → writes to cache file: chat-export-{timestamp}.md
  → launches Intent.ACTION_SEND with text/plain MIME
  → shows share sheet
```

---

## State Management

| State | Provider Dropdown | Model Dropdown | Send Button | Terminal | Agent Status |
|-------|------------------|----------------|-------------|----------|-------------|
| **Bootstrap in progress** | Disabled (gray) | Disabled (gray) | Disabled | Hidden | "Setting up..." |
| **Agent stopped, no messages** | Enabled | Enabled | Disabled | Hidden (peek=0) | "Agent stopped" |
| **Agent stopped, has messages** | Enabled | Enabled | Disabled | Visible (peek=30%) | "Agent stopped" |
| **Agent running, idle** | Enabled | Enabled | Enabled | Visible (peek=30%) | "Ready" |
| **Agent running, streaming** | Disabled* | Disabled* | Shows stop icon | Visible | "Generating..." |
| **Model fetch in progress** | Enabled | Shows spinner | — | — | — |
| **Model fetch error** | Enabled | Shows fallback | — | — | Toast error |
| **Network unavailable** | Enabled | Shows fallback | — | — | "Offline" chip |

\* Provider/model disabled during active streaming to prevent mid-response context switching.

---

## Edge Cases

### Empty States
| Scenario | Display |
|----------|---------|
| **No providers configured** (config.json model_list = []) | Full-screen empty state: icon + "No AI providers configured. Add a provider to start chatting." + "Add Provider" button → opens ProviderListActivity |
| **No messages yet** | Centered illustration + "Send a message to start chatting with your AI" + subtle "Try: Explain quantum computing" as hint |
| **Provider selected but no API key** | Model dropdown disabled with chip "API Key Required". Tapping chip opens ProviderListActivity for that provider. |
| **Terminal empty (no logs)** | Single line in terminal: "(no output yet)" in dimmed monospace |

### Loading States
| Scenario | Display |
|----------|---------|
| **Model list loading** | Model dropdown shows circular spinner (16dp `CircularProgressIndicator` inline). Dropdown items appear progressively as API responds. |
| **Message sending (waiting for response)** | User bubble appears immediately. Agent shows typing indicator (3 bouncing dots in agent-colored bubble). Disappears on first SSE chunk. |
| **Terminal buffer initializing** | Terminal log area shows "Starting PicoClaw gateway..." with animated ellipsis `...` |

### Error States
| Scenario | Display |
|----------|---------|
| **Model fetch fails (HTTP 401/403)** | Snackbar: "Invalid API key for OpenAI. Update in Providers." with "Fix" action → opens ProviderListActivity |
| **Model fetch fails (timeout)** | Snackbar: "Could not reach api.openai.com. Check network." Fallback to configured model string. |
| **Message send fails (agent not running)** | Toast: "Agent is not running. Start agent from menu." Chips turn amber. |
| **Message send fails (HTTP error from gateway)** | System message in chat: "Failed to send message: [error details]. Agent may need restart." |
| **PicoClaw process crashes mid-conversation** | Terminal shows exit code. Chat shows system message "Agent disconnected unexpectedly. Restart?" + "Restart Agent" inline button. |
| **Terminal buffer full (>10,000 lines)** | Trim to last 5,000 lines. Show info chip: "Showing last 5,000 lines (older logs truncated)." |

### Configuration Edge Cases
| Scenario | Behavior |
|----------|----------|
| **Provider has custom apiBase (e.g., LM Studio localhost)** | Model fetch URL = custom apiBase + "/models". If fetch fails (expected for local), use display name directly. |
| **Provider changed while agent was mid-response** | NOT allowed. Dropdowns disabled during streaming. Change queues and applies after `[DONE]` received. |
| **Screen rotation** | ViewModel survives config change. Chat scroll position restored via `savedInstanceState`. Terminal peek height restored. |
| **App backgrounded, agent still running** | ProcessMonitor continues. Chat history survives in ViewModel (unless process death — then reload from saved state or restart clean). |

---

## Accessibility

- Provider/model dropdowns have `android:contentDescription` = "Select AI provider" / "Select model"
- Send button: contentDescription = "Send message"
- Chat message bubbles are focusable for screen readers: announce "Agent says: ..." / "You said: ..."
- Terminal chevron toggle: "Show terminal" / "Hide terminal" with state announcement
- Drag handle: "Terminal panel. Double-tap and hold to expand."
- Typing indicator: announces "Agent is typing" to TalkBack
- All status chips (RUNNING, STOPPED, GENERATING) have content descriptions
- Minimum touch targets ≥ 48dp for all interactive elements

---

## Theme & Typography

| Element | Typography | Color |
|---------|-----------|-------|
| Provider/model labels | `titleSmall` (14sp, medium) | `onSurface` (#1C1B1F) |
| Chat agent bubble | `bodyLarge` (16sp, regular) | `onSurface` on `surfaceVariant` (#E7E0EC) |
| Chat user bubble | `bodyLarge` (16sp, regular) | `onPrimary` (white) on `primary` (#6750A4) |
| Timestamps | `labelSmall` (10sp) | `onSurfaceVariant` (#49454F) |
| Terminal log | Monospace 12sp | `#00FF00` on `#1E1E1E` |
| Terminal header | `titleSmall` (14sp, medium) | `onSurface` |
| System messages | `bodyMedium` (14sp, italic) | `onSurfaceVariant` centered |
| Code blocks in markdown | Monospace 13sp | `#1E1E1E` bg, `#00FF00` text in agent bubble |
| Send button | Icon 24dp | `primary` (#6750A4) when enabled, `onSurface` 38% alpha when disabled |

---

## Dependencies (New Libraries to Consider)

| Library | Purpose | Size Impact |
|---------|---------|-------------|
| `io.noties:markwon:4.6.2` | GitHub-flavored markdown rendering in TextView | ~200KB |
| `io.noties:markwon-ext-strikethrough` | Strikethrough support | ~20KB |
| `io.noties:markwon-syntax-highlight` | Code block syntax highlighting | ~100KB |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Already in project. Used for SSE streaming + API calls. | 0 (existing) |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | Already in project. ChatViewModel + TerminalViewModel. | 0 (existing) |
| OkHttp or `java.net.HttpURLConnection` | Model list fetching. Prefer lightweight HttpURLConnection to avoid adding OkHttp dependency. | 0 (stdlib) |

---

## Open Questions for Engineering

1. **Chat history persistence**: Should chat messages survive process death? Options: (a) in-memory only (ViewModel) — lost on process death; (b) Room/SQLite — survives; (c) save to `files/picoclaw/chat-history.json` — survives, no DB dependency. Recommendation: JSON file for MVP, Room later.
2. **PicoClaw streaming protocol**: Does PicoClaw gateway support SSE? If not, does it use chunked transfer? Need to confirm the `/v1/chat/completions` endpoint behavior.
3. **Model fetch endpoint**: Does the provider dropdown fetch models from PicoClaw's own model-list endpoint, or directly from the upstream provider API? PicoClaw might proxy this. Needs confirmation.
4. **Markdown rendering**: Can Markwon handle live streaming (partial markdown)? Or should we render plain text during streaming, then re-render markdown on `[DONE]`?
5. **Termux session vs new process**: Should the terminal panel show the raw PicoClaw gateway process stdout, or should we embed a full Termux session for the terminal? For MVP, stdout capture is simpler.

---

## Files to Create/Modify

| File | Action | Purpose |
|------|--------|---------|
| `activity_chat_terminal.xml` | NEW | Root layout (CoordinatorLayout + AppBar + RecyclerView + BottomSheet) |
| `item_message_agent.xml` | NEW | Agent chat bubble layout (avatar + bubble + timestamp) |
| `item_message_user.xml` | NEW | User chat bubble layout (bubble + timestamp) |
| `item_terminal_line.xml` | NEW | Single terminal log line (monospace text) |
| `ChatActivity.kt` | NEW | Main activity hosting chat + terminal |
| `ChatViewModel.kt` | NEW | ViewModel: messages list, provider/model selection, streaming state |
| `TerminalViewModel.kt` | NEW | ViewModel: terminal log lines, process stdin writer |
| `ChatAdapter.kt` | NEW | RecyclerView adapter for chat messages (multi-viewtype: agent/user/system) |
| `TerminalAdapter.kt` | NEW | RecyclerView adapter for terminal log lines |
| `model/ChatMessage.kt` | NEW | Data class: role (USER/AGENT/SYSTEM), content, timestamp, isStreaming |
| `model/LogLine.kt` | NEW | Data class: timestamp, level, message |
| `provider/ModelFetcher.kt` | NEW | HTTP client to fetch model list from provider API |
| `strings.xml` | MODIFY | Add chat + terminal strings (empty states, labels, errors) |
| `themes.xml` | MODIFY | Add terminal background color, markdown code block colors |
| `AndroidManifest.xml` | MODIFY | Declare ChatActivity |
