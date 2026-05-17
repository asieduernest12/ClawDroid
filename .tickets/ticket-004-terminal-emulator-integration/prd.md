# Ticket-004: Terminal Emulator Integration for PicoClaw

## 1. Problem Statement

PicoClaw is a CLI application that must run within a terminal environment. Android does not provide a built-in terminal that third-party apps can easily leverage. ClawDroid must integrate with existing Android terminal emulators (Termux, LibreTerm) to launch PicoClaw, pass configuration, capture output, and manage the process lifecycle. Without this, PicoClaw cannot actually execute on-device.

Business Impact:
- Core feature of running PicoClaw on Android is impossible
- Users must manually configure terminal emulators outside the app
- No way to programmatically start/stop PicoClaw from the Mission Control page

## 2. Proposed Solution

Implement a terminal integration layer that:
- **Detects installed terminal emulators** (Termux, LibreTerm) via package manager
- **Launches PicoClaw** using Android intents with command-line arguments
- **Reports process status** back to Mission Control server via a shared state
- **Handles missing dependencies** gracefully with user guidance
- **Provides fallback** via bundled BusyBox or simple shell execution

### Architecture Overview
```
MissionControlServer (/api/start)
        │
        ▼
TerminalManager.launchPicoClaw(config)
        │
        ├──► Intent: Termux:OpenURI / PicoClaw command
        │         or
        │    Intent: LibreTerm / command execution
        │
        ▼
   ProcessMonitor.pollStatus()
        │
        ▼
   Shared MutableStateFlow ──► MissionControlServer (/api/status)
```

## 3. Acceptance Criteria

### Functional Requirements
- [x] Application detects Termux if installed (`PackageManager.getPackageInfo("com.termux")`)
- [x] Application detects LibreTerm if installed
- [x] Application launches PicoClaw in detected terminal emulator via intent
- [x] Launch command reads configuration from Ticket-002 ConfigRepository
- [x] `/api/status` reflects correct PicoClaw running/stopped state
- [x] `/api/stop` can terminate PicoClaw process
- [x] If no terminal emulator found, display user-friendly message with install link
- [x] Process status is updated in real-time (polling interval: 2 seconds)

### Quality Requirements
- [x] Terminal detection completes in under 100ms
- [x] Intent launch does not block the main thread
- [x] Process monitor runs on background coroutine
- [x] Graceful degradation if terminal emulator crashes
- [x] Error messages are user-friendly (Toast/Snackbar, not stack traces)

### Development Requirements
- [x] Unit tests for TerminalManager (mock PackageManager)
- [x] Unit tests for ProcessMonitor
- [s] Integration test for intent construction  <!-- Defer: requires Termux on emulator -->
- [s] Instrumented test verifying terminal launch (requires Termux on emulator)  <!-- Defer: requires Termux on emulator -->
## 4. Technical Considerations

### Implementation Approach
- TerminalManager wraps Android PackageManager for emulator detection
- Intents follow Termux's intent API: `Termux:OpenURI` scheme
- LibreTerm uses standard Android intent with action `VIEW` and URI scheme
- ProcessMonitor uses `java.lang.Process` API or signals to check running state
- Config is serialized to a temp file and path passed as argument to PicoClaw

### Terminal Intent API

**Termux:**
```
Intent(TermuxService.ACTION_OPEN_URI)
  .setClassName("com.termux", "com.termux.app.RunCommandService")
  .putExtra(TermuxConstants.EXTRA_COMMAND_PATH, "/data/data/com.example.clawdroid/files/picoclaw/picoclaw-arm64")
  .putExtra(TermuxConstants.EXTRA_ARGUMENTS, arrayOf("--config", "/path/to/config.yaml"))
```

**LibreTerm:**
```
Intent(Intent.ACTION_VIEW)
  .setClassName("org.libreterm", "org.libreterm.TerminalActivity")
  .setData(Uri.parse("exec:///data/data/com.example.clawdroid/files/picoclaw/picoclaw-arm64 onboard"))
```

### Files to Create
```
app/src/main/java/com/example/clawdroid/
├── terminal/
│   ├── TerminalManager.kt         # Detects and launches terminal emulators
│   ├── ProcessMonitor.kt          # Polls PicoClaw process status
│   ├── TerminalType.kt            # Enum: TERMUX, LIBRE_TERM, NONE
│   └── model/
│       ├── PicoClawProcess.kt     # Process handle data class
│       └── TerminalStatus.kt      # Sealed class: Available, Unavailable
app/src/test/java/com/example/clawdroid/terminal/
├── TerminalManagerTest.kt
└── ProcessMonitorTest.kt
app/src/androidTest/java/com/example/clawdroid/terminal/
└── TerminalIntegrationTest.kt
```

### Performance
- Terminal detection: single PackageManager query (< 100ms)
- Process polling: lightweight PID existence check (< 10ms per poll)
- No persistent background threads when PicoClaw is not running

### Security
- Validate that launched intent targets a legitimate terminal app
- Never pass sensitive configuration as raw command-line args (use temp file)
- Sanitize file paths before passing to terminal intents

## 5. Dependencies

- **Depends on ticket-002**: Configuration values passed to terminal
- **Depends on ticket-003**: Mission Control API wired to TerminalManager actions

## 6. Subtask Checklist

- [x] Task 1: Create TerminalType enum and detection logic
    - **Problem**: Need to identify available terminal emulators
    - **Test**: TerminalManager detects installed terminal emulators
    - **Subtasks**:
        - [x] Subtask 1.1: Create TerminalType enum (TERMUX, LIBRE_TERM, NONE)
            - **Objective**: Define supported terminal types
            - **Test**: Enum values are accessible and documented
            - **Depends on**: None
        - [x] Subtask 1.2: Create TerminalManager with package detection
            - **Objective**: Use PackageManager to check installed terminal apps
            - **Test**: Package query returns correct availability for each emulator
            - **Depends on**: Subtask 1.1

- [x] Task 2: Implement intent-based terminal launcher
    - **Problem**: Need to construct and dispatch intents for each terminal type
    - **Test**: Correct intent is created for each terminal type
    - **Subtasks**:
        - [x] Subtask 2.1: Implement Termux intent builder
            - **Objective**: Construct Termux-specific command intent with config path
            - **Test**: Intent has correct action, package, and extras for Termux
            - **Depends on**: Task 1
        - [x] Subtask 2.2: Implement LibreTerm intent builder
            - **Objective**: Construct LibreTerm-specific exec intent
            - **Test**: Intent has correct action, package, and URI for LibreTerm
            - **Depends on**: Task 1
        - [x] Subtask 2.3: Create launchPicoClaw method in TerminalManager
            - **Objective**: Select best available terminal, build intent, fire it
            - **Test**: launchPicoClaw returns success with intent dispatched
            - **Depends on**: Subtask 2.1, Subtask 2.2

- [x] Task 3: Implement ProcessMonitor for PicoClaw status
    - **Problem**: Need to know if PicoClaw is running for Mission Control
    - **Test**: ProcessMonitor correctly reports running/stopped state
    - **Subtasks**:
        - [x] Subtask 3.1: Create MutableStateFlow<ProcessStatus> for shared state
            - **Objective**: Reactive status that Mission Control server can observe
            - **Test**: StatusFlow emits correct values
            - **Depends on**: None
        - [x] Subtask 3.2: Implement process polling logic
            - **Objective**: Check if PicoClaw process is alive (PID + process name)
            - **Test**: Polling returns correct state for running and stopped processes
            - **Depends on**: Subtask 3.1

- [x] Task 4: Wire TerminalManager to ConfigRepository
    - **Problem**: Terminal launch needs configuration values
    - **Test**: TerminalManager reads config correctly
    - **Subtasks**:
        - [x] Subtask 4.1: Inject ConfigRepository into TerminalManager
            - **Objective**: Read PicoClaw binary path, config path from settings
            - **Test**: TerminalManager uses config values for command construction
            - **Depends on**: Task 2, ticket-002

- [x] Task 5: Wire TerminalManager to MissionControlServer API
    - **Problem**: Mission Control API needs to trigger and monitor PicoClaw
    - **Test**: /api/start triggers launch, /api/stop terminates, /api/status reflects state
    - **Subtasks**:
        - [x] Subtask 5.1: Wire /api/start to TerminalManager.launchPicoClaw
            - **Objective**: POST /api/start calls TerminalManager
            - **Test**: API call triggers launch intent dispatch
            - **Depends on**: Task 3, Task 4, ticket-003 Task 3
        - [x] Subtask 5.2: Wire /api/stop to PicoClaw shutdown
            - **Objective**: POST /api/stop terminates process
            - **Test**: API call stops PicoClaw and status changes to stopped
            - **Depends on**: Subtask 5.1
        - [x] Subtask 5.3: Wire /api/status to ProcessMonitor state
            - **Objective**: GET /api/status returns real-time process state
            - **Test**: Status reflects running after start, stopped after stop
            - **Depends on**: Subtask 5.1

- [x] Task 6: Handle missing terminal gracefully
    - **Problem**: Users without Termux/LibreTerm need guidance
    - **Test**: App shows helpful message when no terminal found
    - **Subtasks**:
        - [x] Subtask 6.1: Add "no terminal" detection and user-facing message
            - **Objective**: Show Snackbar/Dialog with install instructions
            - **Test**: Dialog shows F-Droid / GitHub links for Termux
            - **Depends on**: Task 1

- [x] Task 7: Write tests and verify
    - **Problem**: Ensure terminal integration is reliable
    - **Test**: All tests pass
    - **Subtasks**:
        - [x] Subtask 7.1: Write TerminalManager unit tests
            - **Objective**: Mock PackageManager to test terminal detection and intent construction
            - **Test**: `./gradlew testDebugUnitTest --tests "com.example.clawdroid.terminal.TerminalManagerTest"` passes
            - **Depends on**: Task 2
        - [x] Subtask 7.2: Write ProcessMonitor unit tests
            - **Objective**: Test status polling with mock processes
            - **Test**: `./gradlew testDebugUnitTest --tests "com.example.clawdroid.terminal.ProcessMonitorTest"` passes
            - **Depends on**: Task 3
        - [s] Subtask 7.3: Write integration test on emulator  <!-- Defer: requires Termux on emulator -->
            - **Objective**: Verify end-to-end terminal integration
            - **Test**: `./gradlew connectedAndroidTest --tests "com.example.clawdroid.terminal.TerminalIntegrationTest"` passes (with Termux on emulator)
            - **Depends on**: Task 6
        - [x] Subtask 7.4: Run full quality check
            - **Objective**: Zero lint errors, all tests pass
            - **Test**: `./gradlew lint && ./gradlew test && ./gradlew assembleDebug` exit 0
            - **Depends on**: Subtasks 7.1-7.3