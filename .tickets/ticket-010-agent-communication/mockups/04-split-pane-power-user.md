# Split-Pane Power User — ClawDroid Agent Communication Screen

## 1. Design Rationale

### 1.1 Problem Statement

Power users need to interact with a running PicoClaw agent via chat while simultaneously monitoring the raw gateway process output. Currently these concerns are separated across different screens (`MissionControlActivity` for the web dashboard and `LogViewerActivity` for log files), forcing context-switching that breaks workflow flow.

### 1.2 Design Direction: "Split-Pane Power User"

A single screen divided into two independently-functional panes — chat on top, terminal on bottom — separated by a draggable divider. Both panes are live and active simultaneously. The layout adapts to landscape by switching to a side-by-side split (chat left, terminal right).

This mirrors tools developers already use (VS Code split editor, tmux panes, Termux multi-session) and maximizes the tablet/DeX/foldable utility of ClawDroid.

---

## 2. Layout: Portrait (phone/tablet vertical)

### 2.1 ASCII Wireframe — Portrait

```
+---------------------------------------------------+
| [PROVIDER_ICON]  model-name              [ ⋮ ]    |  ← Top compact bar (48dp)
+---------------------------------------------------+
|                                                   |
|  ┌─────────────────────────────────────────────┐  |
|  │                                             │  |
|  │  [Agent]  Sure, let me check the gateway    │  |
|  │           status and running models...       │  |
|  │                                             │  |
|  │  [User]   /status                           │  |
|  │                                             │  |
|  │  [Agent]  ```json                           │  |
|  │           { "status": "running",            │  |
|  │             "models": ["gpt-4o","claude3"]  │  |  ← Agent Chat (50%)
|  │           } ```                             │  |    RecyclerView with
|  │                                             │  |    markdown rendering
|  │  [Agent]  Gateway is healthy. 3 providers   │  |
|  │           configured. All models online.    │  |
|  │                                             │  |
|  │  ───────────────────────────────────────    │  |
|  │  [User]   Can you restart gateway?          │  |
|  │                                             │  |
|  │  [Agent]  Running `gateway restart`...      │  |
|  │           Gateway restarted successfully.   │  |
|  │                                             │  |
|  │  ┌─────────────────────────────────────┐    │  |
|  │  │ +-----------------------------------+│    │  |
|  │  │ | Type a message...         [📎] [➤]││    │  |  ← Chat input bar
|  │  └─────────────────────────────────────┘    │  |
|  └─────────────────────────────────────────────┘  |
|                                                   |
|  ========  DRAGGABLE DIVIDER  ==================  |  ← 8dp touch target
|                                                   |
|  ┌─────────────────────────────────────────────┐  |
|  │  $ picoclaw gateway ─────────────────────── │  |  ← Terminal title bar
|  │                                             │  |
|  │  [INFO] 2026-05-17 10:32:01 Gateway start  │  |
|  │  [INFO] 2026-05-17 10:32:01 providers: 3   │  |  ← PicoClaw CLI Terminal
|  │  [INFO] 2026-05-17 10:32:01 models loaded  │  |    (50%)
|  │  [DEBUG] Connecting to api.openai.com...   │  |    Monospace, live
|  │  [DEBUG] WebSocket established             │  |    stdout/stderr merged
|  │  [INFO] 2026-05-17 10:32:05 Ready          │  |
|  │  [INFO] 2026-05-17 10:33:12 restart req    │  |
|  │  [INFO] 2026-05-17 10:33:12 Shutting down  │  |
|  │  [INFO] 2026-05-17 10:33:14 Gateway start  │  |
|  │  [INFO] 2026-05-17 10:33:14 Ready          │  |
|  │                                             │  |
|  │  ┌─────────────────────────────────────┐    │  |
|  │  │ $ _                                 │    │  |  ← Terminal command input
|  │  └─────────────────────────────────────┘    │  |
|  └─────────────────────────────────────────────┘  |
+---------------------------------------------------+
```

### 2.2 ASCII Wireframe — Landscape

```
+----------------------------------+----------------------------------+
| Top Compact Bar (full width)     |                                  |
| [PROVIDER_ICON] model [ ⋮ ]     |                                  |
+----------------------------------+----------------------------------+
|                                  |                                  |
|  ┌─ Agent Chat ───────────────┐ |  ┌─ PicoClaw CLI ──────────────  |
|  │                            │ |  │                               |
|  │ [Agent] Running `status`   │ |  │ $ picoclaw gateway ────────  |
|  │                            │ |  │                               |
|  │ [User]  /status            │ |  │ [INFO] Gateway starting...   |
|  │                            │ |  │ [INFO] providers: 3          |
|  │ [Agent] {"status":"ok"}    │ |  │ [DEBUG] api.openai.com OK    |
|  │                            │ |  │ [INFO] Ready on :8080        |
|  │ [Agent] All systems green. │ |  │                               |
|  │                            │ |  │ [INFO] restart requested     |
|  │                            │ |  │ [INFO] Gateway restarted     |
|  │                            │ |  │                               |
|  │ ┌────────────────────────┐ │ |  │ ┌───────────────────────────┐│
|  │ │ Type a message... [➤] │ │ |  │ │ $ _                       ││
|  │ └────────────────────────┘ │ |  │ └───────────────────────────┘│
|  └────────────────────────────┘ |  └──────────────────────────────  |
+----------------------------------+----------------------------------+
```

---

## 3. Widget Hierarchy (Material Design 3)

### 3.1 Root Layout — Portrait

```
ConstraintLayout (root, match_parent × match_parent)
├── TopAppBar (MaterialToolbar, 48dp, app:layout_constraintTop_toTopOf)
│   ├── ImageView (provider icon, 24dp, circular crop)
│   ├── TextView (model name, textAppearance="titleSmall")
│   └── ImageButton (kebab menu overflow, ic_more_vert, 24dp)
│       └── PopupMenu
│           ├── "Switch Provider"
│           ├── "Change Model"
│           ├── "Fetch Models"
│           └── "Settings"
│
├── FrameLayout (chatPane, 0dp × 0dp)
│   │  app:layout_constraintTop_toBottomOf="@id/topBar"
│   │  app:layout_constraintBottom_toTopOf="@id/divider"
│   │  app:layout_constraintStart_toStartOf="parent"
│   │  app:layout_constraintEnd_toEndOf="parent"
│   │
│   ├── RecyclerView (chatMessages)
│   │   adapter: ChatMessageAdapter
│   │   layoutManager: LinearLayoutManager (bottom-stack, reverseLayout=false)
│   │   stackFromEnd: true  (auto-scroll to latest)
│   │
│   └── LinearLayout (chatInputBar, align bottom, 48dp)
│       ├── EditText (messageInput, 0dp weight=1, hint="Type a message…")
│       ├── ImageButton (attachmentButton, ic_attach_file, 24dp)
│       └── ImageButton (sendButton, ic_send, 24dp)
│
├── View (divider, 8dp × match_parent)
│   │  app:layout_constraintTop_toBottomOf="@id/chatPane"
│   │  background: @color/divider (gray 400, 1dp center line)
│   │  touchDelegate: ±12dp vertical expansion
│   │  OnTouchListener: vertical drag → reposition guideline
│   │
├── FrameLayout (terminalPane, 0dp × 0dp)
│   │  app:layout_constraintTop_toBottomOf="@id/divider"
│   │  app:layout_constraintBottom_toBottomOf="parent"
│   │  app:layout_constraintStart_toStartOf="parent"
│   │  app:layout_constraintEnd_toEndOf="parent"
│   │
│   ├── TextView (terminalTitleBar, 32dp)
│   │   text: "$ picoclaw gateway"
│   │   fontFamily: monospace
│   │   background: @color/surfaceDim (#1A1A1A)
│   │
│   ├── ScrollView (terminalOutput, fill)
│   │   └── TextView (terminalContent)
│   │       fontFamily: monospace
│   │       textSize: 11sp
│   │       textColor: @color/terminal_green (#00FF41)
│   │       background: @color/terminal_bg (#0A0A0A)
│   │       lineSpacingMultiplier: 1.15
│   │       autoLink: none
│   │       textIsSelectable: true
│   │
│   └── LinearLayout (terminalInputBar, align bottom, 40dp)
│       ├── TextView (prompt, "$", monospace, 14sp)
│       └── EditText (commandInput, 0dp weight=1, monospace, 14sp)
│           hint: "status | gateway restart | version | model list"
│           imeOptions: actionSend
│
└── Guideline (verticalSplitGuideline, NOT used in portrait)
    app:layout_constraintGuide_percent="0.50"
```

### 3.2 Root Layout — Landscape (configuration change)

In landscape, the layout shifts to side-by-side:

```
ConstraintLayout (root, match_parent × match_parent)
├── TopAppBar (same as portrait, full width)
│
├── FrameLayout (chatPane, 0dp × 0dp)
│   │  app:layout_constraintTop_toBottomOf="@id/topBar"
│   │  app:layout_constraintBottom_toBottomOf="parent"
│   │  app:layout_constraintStart_toStartOf="parent"
│   │  app:layout_constraintEnd_toStartOf="@id/divider"
│
├── View (divider, 8dp × match_parent)
│   │  app:layout_constraintTop_toBottomOf="@id/topBar"
│   │  app:layout_constraintBottom_toBottomOf="parent"
│   │  app:layout_constraintStart_toEndOf="@id/chatPane"
│   │  OnTouchListener: horizontal drag → reposition guideline
│
└── FrameLayout (terminalPane, 0dp × 0dp)
    │  app:layout_constraintTop_toBottomOf="@id/topBar"
    │  app:layout_constraintBottom_toBottomOf="parent"
    │  app:layout_constraintStart_toEndOf="@id/divider"
    │  app:layout_constraintEnd_toEndOf="parent"
```

Note: A **single XML layout file** suffices. Use a `ConstraintLayout` root with both portrait and landscape constraint sets. At runtime in `onConfigurationChanged`, toggle between vertical-split constraints (portrait) and horizontal-split constraints (landscape) by updating the divider’s and panes’ constraint connections programmatically, without fragment transactions. This avoids two separate layout files and keeps state alive across rotation.

Alternative approach: Use two `activity_split_pane.xml` layout variants — `res/layout/` (portrait) and `res/layout-land/` (landscape). Both share the same view IDs so `ViewBinding` works identically. The Activity’s `onCreate` does not need to branch on orientation; the framework inflates the correct variant.

---

## 4. Component Choices

| UI Element | Component | Rationale |
|---|---|---|
| Root container | `ConstraintLayout` | Handles both portrait (top/bottom) and landscape (left/right) constraints in a single ViewGroup. No nested layouts. Percentage-based sizing via guidelines. |
| Top bar | `MaterialToolbar` (`TopAppBar`) | Built-in kebab menu via `app:menu`, compact height (48dp via `style="@style/Widget.Material3.Toolbar"`), `navigationIcon` for back. |
| Kebab menu | `PopupMenu` anchored to toolbar overflow | Standard Material pattern; avoids custom dropdown overhead. |
| Chat message list | `RecyclerView` + `ChatMessageAdapter` | Efficient for long conversations. `DiffUtil` for minimal rebinding. `LinearLayoutManager` with `stackFromEnd = true`. |
| Chat message item | `MaterialCardView` or `ConstraintLayout` per item | Per-message layout determined by sender (user = right-aligned, agent = left-aligned). |
| Markdown rendering | `TextView` with custom `MarkdownSpanBuilder` | Project has no third-party markdown lib. Build spans from parsed markdown: bold (`**`), code (` ``` `), inline code (`` ` ``), lists. Source of truth: a lightweight parser class `MarkdownParser.kt` in `com.example.clawdroid.terminal.markdown`. |
| Code blocks | `TextView` with monospace `TypefaceSpan` + dark background `BackgroundColorSpan` | Syntax highlighting deferred to future enhancement (ANSI-to-span converter). For now: grey background + monospace font. |
| Copy on tap | `OnLongClickListener` → copy message text to `ClipboardManager` | Standard Android long-press-for-copy pattern. A Toast confirms "Copied". |
| Chat input bar | `LinearLayout` (horizontal) at bottom of chat pane | Fixed 48dp height. `EditText` grows via `maxLines="5"` + `singleLine="false"`. Send button enabled only when text is non-blank (via `TextWatcher`). |
| Divider | Custom `View` subclass `SplitPaneDivider` | Renders a 1dp center line with handle dots (3 dots, 4dp diameter). Touch target expanded to 32dp via `TouchDelegate`. Drag gesture handled in `onTouchEvent` via `GestureDetector` or manual `ACTION_MOVE`. |
| Terminal title bar | `TextView` with monospace font, dark background | Shows `$ picoclaw gateway` as a static chrome element. |
| Terminal output | `ScrollView` + `TextView` | Simple, no custom `Canvas` rendering needed. `textIsSelectable="true"` so users can copy terminal output. Auto-scrolls to bottom on new lines via `scrollView.fullScroll(View.FOCUS_DOWN)`. |
| Terminal input bar | `LinearLayout` (horizontal) | `$ ` prompt as prefix `TextView` + `EditText` for raw command input. Sends to PicoClaw process stdin. |

---

## 5. Split-Pane Implementation

### 5.1 Approach: ConstraintLayout + Draggable Divider View

**Why not a library or Fragment-based split?**

- The project has zero Fragment usage (all Activities). Introducing FragmentManager for a pane split adds lifecycle complexity with no benefit.
- ConstraintLayout already handles percentage-based sizing via `Guideline`. The divider simply updates the guideline’s percent on drag.
- A lightweight custom `View` subclass (`SplitPaneDivider`) handles the drag gesture and communicates the new ratio to the parent ConstraintLayout via a callback.

### 5.2 SplitPaneDivider Implementation Outline

```kotlin
class SplitPaneDivider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onDrag: ((fraction: Float) -> Unit)? = null

    private var lastTouchY = 0f
    private var lastTouchX = 0f
    private var isPortrait = true  // updated by Activity

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.rawY
                lastTouchX = event.rawX
                parent.requestDisallowInterceptTouchEvent(true)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val delta = if (isPortrait) event.rawY - lastTouchY
                            else event.rawX - lastTouchX
                val parentHeight = (parent as View).height
                val parentWidth = (parent as View).width
                val total = if (isPortrait) parentHeight.toFloat() else parentWidth.toFloat()
                val current = if (isPortrait) y + delta else x + delta
                val fraction = (current / total).coerceIn(0.25f, 0.75f)
                onDrag?.invoke(fraction)
                lastTouchY = event.rawY
                lastTouchX = event.rawX
                true
            }
            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw 3 handle dots centered in the view
        val color = ContextCompat.getColor(context, R.color.divider_handle)
        paint.color = color
        val spacing = 16f
        val radius = 4f
        val cx = width / 2f
        val cy = height / 2f
        if (isPortrait) {
            canvas.drawCircle(cx, cy - spacing, radius, paint)
            canvas.drawCircle(cx, cy, radius, paint)
            canvas.drawCircle(cx, cy + spacing, radius, paint)
        } else {
            canvas.drawCircle(cx - spacing, cy, radius, paint)
            canvas.drawCircle(cx, cy, radius, paint)
            canvas.drawCircle(cx + spacing, cy, radius, paint)
        }
    }
}
```

### 5.3 Activity Drag Handling

```kotlin
// In PowerUserActivity.kt
private var splitFraction = 0.50f  // 50/50 default

divider.onDrag = { fraction ->
    splitFraction = fraction.coerceIn(0.25f, 0.75f)  // min 25%, max 75% per pane
    val constraintSet = ConstraintSet()
    constraintSet.clone(rootLayout)

    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Set chatPane height percent
        constraintSet.constrainPercentHeight(R.id.chatPane, splitFraction)
    } else {
        // Set chatPane width percent
        constraintSet.constrainPercentWidth(R.id.chatPane, splitFraction)
    }
    constraintSet.applyTo(rootLayout)
}
```

**Persistence**: `splitFraction` is saved to `SharedPreferences` in `onStop()` and restored in `onCreate()` so the user’s preferred split ratio survives app restarts.

### 5.4 Orientation Change Handling

```kotlin
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    divider.isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT

    // Rebuild constraints for the new orientation
    val constraintSet = ConstraintSet()
    constraintSet.clone(rootLayout)

    if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Top/bottom split: chat top, terminal bottom
        constraintSet.connect(R.id.chatPane, ConstraintSet.TOP, R.id.topBar, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.chatPane, ConstraintSet.BOTTOM, R.id.divider, ConstraintSet.TOP)
        constraintSet.connect(R.id.divider, ConstraintSet.TOP, R.id.chatPane, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.divider, ConstraintSet.BOTTOM, R.id.terminalPane, ConstraintSet.TOP)
        constraintSet.connect(R.id.terminalPane, ConstraintSet.TOP, R.id.divider, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.terminalPane, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.clear(R.id.chatPane, ConstraintSet.START)
        constraintSet.clear(R.id.chatPane, ConstraintSet.END)
        constraintSet.connect(R.id.chatPane, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(R.id.chatPane, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
    } else {
        // Left/right split: chat left, terminal right
        constraintSet.connect(R.id.chatPane, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(R.id.chatPane, ConstraintSet.END, R.id.divider, ConstraintSet.START)
        constraintSet.connect(R.id.divider, ConstraintSet.START, R.id.chatPane, ConstraintSet.END)
        constraintSet.connect(R.id.divider, ConstraintSet.END, R.id.terminalPane, ConstraintSet.START)
        constraintSet.connect(R.id.terminalPane, ConstraintSet.START, R.id.divider, ConstraintSet.END)
        constraintSet.connect(R.id.terminalPane, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        // Both panes span full height below topBar
        constraintSet.connect(R.id.chatPane, ConstraintSet.TOP, R.id.topBar, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.chatPane, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.terminalPane, ConstraintSet.TOP, R.id.topBar, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.terminalPane, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    }
    constraintSet.applyTo(rootLayout)
}
```

Add `android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"` to the `<activity>` declaration in `AndroidManifest.xml` so the Activity handles the rotation itself (preserving state, avoiding full teardown/recreate).

---

## 6. Data Flow for Real-Time Updates

### 6.1 Architecture Overview

```
┌──────────────────────────────────────────────────────┐
│                     App (Application)                 │
│                                                      │
│  ┌─────────────────────┐   ┌──────────────────────┐  │
│  │ AgentSessionManager │   │ EmbeddedTermuxSession│  │
│  │                     │   │                      │  │
│  │ chatMessages:       │   │ outputLines:         │  │
│  │  StateFlow<List<    │   │  StateFlow<List<     │  │
│  │    ChatMessage>>    │   │    String>>          │  │
│  │                     │   │                      │  │
│  │ sendMessage(text)   │   │ stdin: OutputStream  │  │
│  │ resetConversation() │   │ isRunning: StateFlow │  │
│  └─────────┬───────────┘   └──────────┬───────────┘  │
│            │                          │              │
└────────────┼──────────────────────────┼──────────────┘
             │                          │
             │  lifecycleScope.launch   │
             │  .collect { }            │
             ▼                          ▼
┌──────────────────────────────────────────────────────┐
│              PowerUserActivity                        │
│                                                      │
│  chatAdapter.submitList(messages)                    │
│  terminalContent.text = lines.joinToString("\n")     │
│  terminalScroll.fullScroll(FOCUS_DOWN)               │
│                                                      │
│  chatInput → AgentSessionManager.sendMessage()       │
│  terminalInput → EmbeddedTermuxSession.stdin         │
└──────────────────────────────────────────────────────┘
```

### 6.2 AgentSessionManager (New Class)

A new singleton-scoped class managed by `App`:

```kotlin
// package com.example.clawdroid.agent

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,          // USER or ASSISTANT
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false   // true while receiving tokens
)

enum class Role { USER, ASSISTANT }

class AgentSessionManager(
    private val scope: CoroutineScope,
    private val serverPort: StateFlow<Int>
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(text: String) {
        // 1. Append user message
        val userMsg = ChatMessage(role = Role.USER, content = text)
        _messages.value = _messages.value + userMsg

        // 2. Append placeholder assistant message (streaming=true)
        val assistantMsg = ChatMessage(role = Role.ASSISTANT, content = "", isStreaming = true)
        _messages.value = _messages.value + assistantMsg

        // 3. POST to /api/chat on MissionControlServer (or direct HTTP to PicoClaw gateway)
        scope.launch(Dispatchers.IO) {
            val port = serverPort.value
            val url = URL("http://localhost:$port/api/chat")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.write(
                JSONObject().put("message", text).toString().toByteArray()
            )

            // 4. Stream response tokens into the assistant message
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val fullResponse = StringBuilder()
            reader.lines().forEach { line ->
                fullResponse.append(line)
                _messages.value = _messages.value.dropLast(1) +
                    assistantMsg.copy(content = fullResponse.toString(), isStreaming = true)
            }

            // 5. Finalize: streaming=false
            _messages.value = _messages.value.dropLast(1) +
                assistantMsg.copy(content = fullResponse.toString(), isStreaming = false)
        }
    }

    fun resetConversation() {
        _messages.value = emptyList()
    }
}
```

### 6.3 Terminal Output Data Flow

The terminal pane is backed by the existing `EmbeddedTermuxSession.outputLines: StateFlow<List<String>>` from the running PicoClaw session.

```kotlin
// In PowerUserActivity.kt

private fun observeTerminalOutput() {
    val session = (application as App).getPicoClawSession() ?: return
    lifecycleScope.launch {
        session.outputLines.collect { lines ->
            terminalContent.text = lines.joinToString("\n")
            // Auto-scroll if user is at bottom
            if (isTerminalScrolledToBottom()) {
                terminalScroll.post {
                    terminalScroll.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }
}
```

Terminal input from the `$ _` command line is piped to the process’s stdin:

```kotlin
private fun sendTerminalCommand(command: String) {
    val session = (application as App).getPicoClawSession() ?: {
        showToast("PicoClaw not running")
        return
    }
    // Write command + newline to process stdin
    session.writeStdin("$command\n")
    commandInput.text.clear()
}
```

**Note**: `EmbeddedTermuxSession` currently does not expose stdin writing. This requires adding a `writeStdin(text: String)` method that obtains the `Process.outputStream` and writes to it. Implementation:

```kotlin
// Added to EmbeddedTermuxSession.kt
fun writeStdin(text: String): Boolean {
    val proc = process ?: return false
    try {
        proc.outputStream.write(text.toByteArray())
        proc.outputStream.flush()
        return true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to write to stdin", e)
        return false
    }
}
```

### 6.4 Chat ↔ Gateway Communication

The chat pane sends messages to PicoClaw’s gateway API. This requires a new endpoint on `MissionControlServer`:

```kotlin
// Added to MissionControlServer.kt
uri == "/api/chat" && method == Method.POST -> handleChat(session)
```

The chat endpoint proxies to the PicoClaw gateway process. Alternatively, the `AgentSessionManager` communicates directly with the gateway’s HTTP API (which runs on PicoClaw’s configured port) rather than going through the Mission Control server, reducing coupling.

---

## 7. Profile Top Bar Details

### 7.1 Layout Spec

| Property | Value |
|---|---|
| Height | 48dp (compact) |
| Background | `@color/surface` (dark: `#1E1E1E`, light: `#FFFFFF`) |
| Elevation | 2dp (subtle separation) |
| Content description | "Current provider and model" |

### 7.2 Elements (left to right)

1. **Provider icon** (24dp × 24dp `ImageView`, circular crop with 2dp colored ring indicating provider status)
   - OpenAI = green ring, Anthropic = purple ring, Google = blue ring, Local/Ollama = orange ring
   - Falls back to generic `ic_ai` vector drawable
   - Tapping the icon opens provider switcher dialog

2. **Model name** (`TextView`, `textAppearance="titleSmall"`, maxLines=1, ellipsize=end)
   - Displays `"gpt-4o"`, `"claude-3-opus"`, `"gemini-2.5-pro"` etc.
   - When streaming: text animates with a subtle pulse alpha (0.6 → 1.0 → 0.6, 1200ms cycle) to indicate active inference
   - Tapping the model name opens model picker bottom sheet

3. **Kebab menu** (`ImageButton`, `ic_more_vert`, 24dp, contentDescription="More options")
   - Anchors a `PopupMenu` with:
     - `Switch Provider` → opens `ProviderListActivity`
     - `Change Model` → opens model picker bottom sheet filtered to current provider
     - `Fetch Models` → triggers a background fetch from provider’s `/v1/models` endpoint (with progress indicator replacing menu item icon temporarily)
     - `Settings` → opens `ConfigActivity`

### 7.3 Provider/Model State

Source of truth: `ProviderConfigManager` (existing class at `config/ProviderConfigManager.kt`) + `SharedPreferences` for the active provider/model selection.

---

## 8. Chat Message Rendering

### 8.1 ChatMessageAdapter

```kotlin
class ChatMessageAdapter(
    private val onCopyMessage: (ChatMessage) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).role == Role.USER) VIEW_TYPE_USER else VIEW_TYPE_ASSISTANT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> UserMessageViewHolder(
                ItemUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> AssistantMessageViewHolder(
                ItemAssistantMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message, onCopyMessage)
            is AssistantMessageViewHolder -> holder.bind(message, onCopyMessage)
        }
    }
}
```

### 8.2 Markdown Rendering Strategy

No third-party markdown library is added. A lightweight `MarkdownParser` class converts markdown to `SpannableString` using Android span APIs:

| Markdown | Span Used | Visual |
|---|---|---|
| `**bold**` | `StyleSpan(Typeface.BOLD)` | Bold text |
| `*italic*` | `StyleSpan(Typeface.ITALIC)` | Italic text |
| `` `code` `` | `TypefaceSpan("monospace")` + `BackgroundColorSpan(colorSurfaceVariant)` | Inline code |
| ` ```...``` ` | `TypefaceSpan("monospace")` + `BackgroundColorSpan(#1A1A1A)` + `ForegroundColorSpan(#00FF41)` | Code block (terminal green) |
| `- item` | `BulletSpan(16)` | Bullet list |
| `1. item` | Leading number + indent via `LeadingMarginSpan.Standard(32)` | Numbered list |
| `[text](url)` | `URLSpan(url)` | Clickable link |

### 8.3 Copy-on-Tap

Long-press on any message copies its raw text content to the clipboard:

```kotlin
view.setOnLongClickListener {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Chat message", message.content))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    true
}
```

---

## 9. Terminal Emulator Details

### 9.1 Visual Spec

| Property | Value |
|---|---|
| Background | `#0A0A0A` (near-pure black with slight warmth) |
| Font family | `monospace` (system default: Roboto Mono on Pixel, Droid Sans Mono on older) |
| Text size | 11sp |
| Text color | `#00FF41` (neon terminal green) |
| Line spacing | 1.15× multiplier |
| Selection | Enabled via `textIsSelectable="true"` |
| Horizontal scroll | `ScrollView` wraps content only vertically; `android:scrollHorizontally="true"` on the `TextView` for wide log lines |
| Max buffer lines | 5000 (trim from head when exceeded to prevent OOM) |

### 9.2 ANSI Color Support (Future)

The current implementation renders plain text. A future enhancement can parse ANSI escape codes (`\033[32m`, `\033[0m`, etc.) into `ForegroundColorSpan` and `BackgroundColorSpan` for full terminal color support. This is deferred to a follow-up ticket.

### 9.3 Supported Built-in Commands

Commands typed into the terminal input are sent directly to the PicoClaw gateway process via stdin. Recognized commands:

| Command | Description | Expected Output |
|---|---|---|
| `status` | Show gateway health + provider status | JSON status block |
| `gateway restart` | Restart the gateway process | Restart confirmation + new PID |
| `version` | Show PicoClaw binary version | `PicoClaw v0.2.1 (linux/arm64)` |
| `model list` | List available models across providers | Table of model names + providers |
| `help` | Show available commands | Command list |
| **Raw commands** | Any text not matching above is sent to the gateway as-is | Gateway’s native response |

These are ingested by the PicoClaw gateway process; the terminal pane is a pass-through. No command parsing happens in the Android layer.

### 9.4 Process Lifecycle in Terminal

When the Activity starts:
1. Check if PicoClaw session exists via `App.getPicoClawSession()`.
2. If session exists and `isRunning.value == true`, begin collecting `outputLines` immediately (terminal shows historical output from before the Activity opened).
3. If session does not exist or is stopped, show a centered placeholder: `[PicoClaw not running]  Tap START in Dashboard to launch`.

When the Activity is destroyed:
- The session **continues running** (it’s scoped to `App.appScope`, not the Activity lifecycle).
- The terminal output buffer is preserved in the session; re-opening the screen shows all prior output.

---

## 10. Edge Cases

### 10.1 No PicoClaw Session Running
- **Chat pane**: Disabled overlay with message "Start PicoClaw to chat". Input bar hidden.
- **Terminal pane**: Shows placeholder `[PicoClaw not running]` in dimmed monospace text.
- **Top bar**: Model name shows last-used or "—" if none configured. Kebab menu still functional (user can change settings while PicoClaw is stopped).

### 10.2 Session Dies Mid-Use
- `ProcessMonitor` detects session death → `SessionStatus` state updates.
- **Chat**: An auto-inserted system message `[Session terminated unexpectedly. Restarting...]` appears in chat. Send button disabled.
- **Terminal**: Final output lines are preserved (no data loss). A red separator line `───────────────── PROCESS TERMINATED ─────────────────` is appended.
- **Top bar**: Model name pulses red briefly (2-second pulse, 3 cycles), then resets.
- Auto-restart is attempted after a 2-second delay.

### 10.3 Rapid Orientation Changes
- `onConfigurationChanged` handles orientation without Activity recreation.
- `ConstraintSet.clone()` + `applyTo()` is cheap enough for rapid calls. No detectable jank.
- `GestureDetector` on the divider is cancelled if orientation changes mid-drag (`MotionEvent.ACTION_CANCEL` forwarded).

### 10.4 Very Long Chat History
- `RecyclerView` with `DiffUtil` handles large lists efficiently.
- `setHasStableIds(true)` + each `ChatMessage.id` is a `UUID`.
- Cap at 500 messages in-memory (trim oldest when exceeded). Optionally persist to Room database for history across sessions (deferred).

### 10.5 Very Long Terminal Output
- `outputLines` is capped at 5000 lines (trim from head in the `StateFlow` collector).
- `TextView` max length before `SpannableString` becomes too large: 5000 lines ≈ ~250KB of text, well within `TextView` limits.
- If output exceeds, a trimmed header is prepended: `[... 1200 earlier lines truncated ...]`.

### 10.6 Keyboard Overlaps Input Bar
- `android:windowSoftInputMode="adjustResize"` on the Activity in `AndroidManifest.xml` ensures the entire layout (both panes) resizes when the keyboard opens.
- The chat and terminal input bars remain above the keyboard since they are pinned to the bottom of their respective panes.
- In portrait, keyboard opening shrinks both panes proportionally. The divider ratio is preserved.

### 10.7 Empty State
- **Chat**: Empty state `ImageView` + `TextView` with agent icon and "Send a message to start" in the center of the chat pane. Hidden when messages exist.
- **Terminal**: Empty state shows `$ Waiting for gateway output...` in dimmed green. Replaced by real output on first line arrival.

### 10.8 Network Errors (Chat API Calls)
- If `POST /api/chat` fails (HTTP error, timeout):
  - The streaming placeholder message is replaced with an error message: `[Error: Unable to reach PicoClaw gateway. Is it running?]` styled in red/italic.
  - A Snackbar appears at the bottom of the root layout: "Connection failed. Retry?" with RETRY action.
  - The user’s original message is preserved; retry re-sends it.

### 10.9 Tablets / Foldables / DeX
- The layout handles any aspect ratio via percentage-based sizing.
- On very wide screens (> 900dp), the landscape split defaults to 50/50, which is comfortable for side-by-side reading.
- On foldables with a hinge, the app can be spanned across both screens. The split ratio can be set to 100/0 (terminal-only on one screen) by dragging the divider to the edge.

---

## 11. Theme & Colors

### 11.1 Color Tokens

| Token | Light | Dark | Usage |
|---|---|---|---|
| `surface` | `#FFFFFF` | `#1E1E1E` | Top bar, chat input bar background |
| `surfaceDim` | `#F5F5F5` | `#1A1A1A` | Terminal title bar |
| `surfaceVariant` | `#E8E8E8` | `#2D2D2D` | Inline code background |
| `terminal_bg` | `#F0F0F0` (approximates light terminal) | `#0A0A0A` | Terminal output area |
| `terminal_green` | `#007A33` | `#00FF41` | Terminal text |
| `terminal_dim` | `#999999` | `#444444` | Terminal empty state text |
| `chat_user_bubble` | `@color/primary` | `@color/primary` | User message background |
| `chat_assistant_bubble` | `#F0F0F0` | `#2A2A2A` | Assistant message background |
| `chat_user_text` | `@color/onPrimary` | `@color/onPrimary` | User message text |
| `chat_assistant_text` | `@color/onSurface` | `@color/onSurface` | Assistant message text |
| `divider` | `#E0E0E0` | `#3A3A3A` | Pane divider line |
| `divider_handle` | `#BDBDBD` | `#5A5A5A` | Divider drag handle dots |
| `error` | `#D32F2F` | `#CF6679` | Error messages in chat, terminal |
| `streaming_pulse` | Alpha 0.6–1.0 | Alpha 0.6–1.0 | Model name when agent is generating |

### 11.2 Typography

| Element | Material 3 Token | Size | Weight | Family |
|---|---|---|---|---|
| Model name (top bar) | `titleSmall` | 14sp | Medium | sans-serif |
| Chat message body | `bodyLarge` | 16sp | Regular | sans-serif |
| Chat message timestamp | `labelSmall` | 11sp | Regular | sans-serif |
| Code blocks (chat) | custom | 13sp | Regular | monospace |
| Terminal output | custom | 11sp | Regular | monospace |
| Terminal input | custom | 14sp | Regular | monospace |
| Terminal title bar | custom | 12sp | Bold | monospace |
| Kebab menu items | `bodyMedium` | 14sp | Regular | sans-serif |

---

## 12. Accessibility

- **Divider**: `contentDescription = "Drag to resize panes. Current split: 50 percent chat, 50 percent terminal."` Updated dynamically on drag.
- **Chat messages**: Each message has `contentDescription = "${role} message: ${content.take(50)}..."` so TalkBack reads the sender and preview.
- **Code blocks**: Announced as "Code block: [n] lines" by TalkBack.
- **Copy on tap**: Announced as "Long press to copy message to clipboard".
- **Top bar**: Provider icon has `contentDescription = "Current provider: OpenAI. Model: gpt-4o. Tap to change."`. Kebab menu has `contentDescription = "More options"`.
- **Touch targets**: All buttons >= 48dp. Divider touch delegate expands the hit area to 32dp tall (from 8dp visual).
- **Keyboard navigation**: D-pad/trackpad focus moves logically: top bar → chat messages → chat input → divider → terminal output → terminal input.
- **Colour**: Terminal green `#00FF41` on `#0A0A0A` has contrast ratio > 15:1, exceeding WCAG AAA.

---

## 13. AndroidManifest Declaration

```xml
<activity
    android:name=".agent.PowerUserActivity"
    android:exported="false"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize|keyboardHidden"
    android:windowSoftInputMode="adjustResize"
    android:theme="@style/Theme.ClawDroid.PowerUser" />
```

---

## 14. New Files Required

| File | Package/Purpose |
|---|---|
| `PowerUserActivity.kt` | `com.example.clawdroid.agent` — Main Activity |
| `AgentSessionManager.kt` | `com.example.clawdroid.agent` — Chat session state + API calls |
| `ChatMessage.kt` | `com.example.clawdroid.agent.model` — Data class for messages |
| `ChatMessageAdapter.kt` | `com.example.clawdroid.agent.ui` — RecyclerView adapter |
| `MarkdownParser.kt` | `com.example.clawdroid.agent.ui.markdown` — Markdown-to-Spannable |
| `SplitPaneDivider.kt` | `com.example.clawdroid.agent.ui` — Custom divider View |
| `activity_power_user.xml` | `res/layout/` — Portrait layout |
| `activity_power_user.xml` | `res/layout-land/` — Landscape layout |
| `item_user_message.xml` | `res/layout/` — User chat bubble |
| `item_assistant_message.xml` | `res/layout/` — Assistant chat bubble |
| `power_user_strings.xml` | `res/values/` — String resources |
| `power_user_colors.xml` | `res/values/` — Color tokens |

---

## 15. Open Questions for Engineering

1. **Gateway API contract**: Does PicoClaw’s gateway expose an `/api/chat` endpoint for JSON message posts, or does chat interaction go through a different mechanism (WebSocket, stdin/stdout protocol)? The mockup assumes HTTP POST with streaming JSON lines response. Needs confirmation against PicoClaw’s actual API surface.

2. **stdin passthrough risk**: Writing arbitrary text to the PicoClaw process’s stdin could interfere with the gateway’s own protocol if the gateway reads stdin for internal commands. Should terminal input be sanitized or limited to a known command set?

3. **ANSI parsing priority**: Should basic ANSI color support be included in the initial implementation (added complexity but major UX win) or deferred? If deferred, what escape-sequence stripping is needed to avoid garbled terminal output?

4. **Chat persistence**: Should chat history survive process death (via Room/SQLite), or is it acceptable to lose chat on session restart (simpler, matches terminal’s ephemeral nature)?

5. **Tablet multi-window**: Should the split-pane Activity support Android multi-window (`resizeableActivity="true"`) so users can have chat in one window and terminal in another via Android’s native split-screen? This would require the Activity to gracefully handle very small window sizes.

6. **Provider fetching UX**: "Fetch Models" in the kebab menu triggers a background network call. Should this show a full-screen loading state, a Snackbar with indeterminate progress, or an inline progress indicator replacing the menu item icon?
