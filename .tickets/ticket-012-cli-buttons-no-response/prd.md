# Ticket 012: CLI Command Chips Show No Output / Not Responding

## Status: Pending
**Created**: 2026-05-17
**Depends on**: ticket-011 (OpenRouter/CLI fix was incomplete)

---

## Problem Statement

The PicoClaw CLI command chips in the Agent Chat terminal bottom sheet (status, version, gateway, model list, restart) still produce no visible output despite ticket-011 marking them as fixed. Two distinct bugs cause this:

### Bug 1: Spawned CLI process runs in bare environment (missing Termux bootstrap env vars)

When a chip is clicked (e.g., "status"), `AgentChatActivity.executeCliCommand()` at line 399 spawns a **fresh subprocess** via `ProcessBuilder`:

```kotlin
val pb = ProcessBuilder(listOf(binaryPath) + args)
pb.environment()["PICOCLAW_HOME"] = workDir
```

This only sets `PICOCLAW_HOME` — it omits all Termux bootstrap environment variables that the PicoClaw binary needs at runtime:

| Missing Variable | Purpose | Set by bootstrap |
|---|---|---|
| `PATH` | Binary/script resolution | `TermuxBootstrapManager.getEnv()` |
| `LD_LIBRARY_PATH` | Shared library loading | ✓ |
| `HOME` | User home directory | ✓ |
| `PREFIX` | Termux prefix path | ✓ |
| `TMPDIR` | Temp file creation | ✓ |
| `TERMUX_APP_PACKAGE` | App identity | ✓ |
| `TERMUX_VERSION` | Runtime compatibility | ✓ |

Compare with `App.createPicoClawSession()` at line 135 which properly injects the full environment:

```kotlin
val env = bootstrapManager.getEnv().toMutableMap()  // ALL bootstrap vars
env["PICOCLAW_HOME"] = File(filesDir, "picoclaw").absolutePath
```

Without these vars the PicoClaw binary may fail silently (exit code != 0 with no stderr) when running CLI subcommands like `status`, `version`, or `model list`.

### Bug 2: `loadProviderTerminalOutput()` continuously replaces terminal output, wiping CLI responses

`AgentChatActivity.loadProviderTerminalOutput()` at line 461 collects from the running PicoClaw gateway session's `outputLines` flow:

```kotlin
app.getPicoClawSession()?.outputLines?.collect { lines ->
    terminalLines.clear()            // WIPES any CLI output
    terminalLines.addAll(lines)      // Replaces with gateway session output
    terminalAdapter.notifyDataSetChanged()
}
```

The gateway's `EmbeddedTermuxSession._outputLines` emits the **entire accumulated output list** every time a new line is produced. This means:

1. User clicks "status" chip
2. `sendTerminalCommand("status")` appends "> status" to `terminalLines`
3. `executeCliCommand` spawns subprocess, captures output
4. Gateway session writes a log/heartbeat line → `_outputLines` emits full list
5. `loadProviderTerminalOutput` fires → `terminalLines.clear()` wipes step 2's output
6. User sees only gateway session output (or empty terminal if gateway is idle)

### Summary of Root Causes

| # | Root Cause | File | Line |
|---|---|---|---|
| 1 | `executeCliCommand()` sets only `PICOCLAW_HOME`, missing all Termux bootstrap env vars | `AgentChatActivity.kt` | 414-417 |
| 2 | `loadProviderTerminalOutput()` clears CLI output by replacing terminalLines with gateway session output on every emission | `AgentChatActivity.kt` | 461-473 |

## Proposed Solution

### Fix 1 — Inject full bootstrap environment into CLI subprocess

In `executeCliCommand()`, replace the minimal environment setup with the full Termux bootstrap environment (same as `App.createPicoClawSession()`):

```kotlin
// Before (broken):
pb.environment()["PICOCLAW_HOME"] = workDir

// After (fixed):
pb.environment().putAll(bootstrapManager.getEnv())
pb.environment()["PICOCLAW_HOME"] = workDir
```

The `bootstrapManager` can be obtained via `(application as App).bootstrapManager`.

### Fix 2 — Decouple CLI command output from gateway session output

Two options:

**Option A** (recommended): Remove `loadProviderTerminalOutput()` or gate it so it only runs when the terminal bottom sheet is NOT being used interactively. Use a separate list/state to manage CLI command output vs. gateway session log output.

**Option B**: Replace `loadProviderTerminalOutput()` with a diff-based approach that only appends NEW lines not already present, instead of blindly clearing the list. However this is fragile if lines repeat.

**Option C**: Instead of spawning a separate process, write CLI commands to the running PicoClaw gateway's stdin via `sendInput()` and capture responses from the session's output flow. This eliminates the need for independent CLI execution entirely.

**Recommendation**: Option C (send commands via stdin to the running gateway) is cleanest because:
- Uses the already-running PicoClaw process with correct environment
- Eliminates the duplicate process spawning
- Responses come through the same `outputLines` flow that `loadProviderTerminalOutput` is already collecting
- The `EmbeddedTermuxSession.sendInput()` method already exists at line 98

If Option C is chosen, Fix 1 becomes unnecessary since we'd be using the existing gateway session.

### Minimum Viable Fix

At minimum:
1. Add Termux bootstrap env vars to `executeCliCommand()`'s ProcessBuilder
2. Fix `loadProviderTerminalOutput()` to not wipe interactive CLI output

## Technical Considerations

- `EmbeddedTermuxSession.sendInput()` writes to the process stdin — the PicoClaw gateway must be designed to accept CLI commands on stdin
- If the gateway doesn't read stdin, Option C won't work and we must keep the separate process approach but fix the environment
- The gateway session writes to a `logFile` at `filesDir/picoclaw.log` — CLI command output could also be read from there
- Adding timeout (10s) to `executeCliCommand()` is advised to prevent UI hangs (noted in ticket-011 but not implemented)
- The `terminalLines.clear()` in `loadProviderTerminalOutput` is particularly destructive — it will also clear any user-typed terminal commands

## Dependencies

- **Depends on ticket-011**: Attempted to fix CLI buttons but fix was incomplete (tasks marked [x] but bugs remain)
- **Depends on ticket-010**: Introduced AgentChatActivity with terminal bottom sheet and command chips

---

## Tasks

### Task 1: Investigate and confirm root causes

- [x] Subtask 1.1: Verify that `executeCliCommand()` ProcessBuilder lacks Termux bootstrap env vars
  - **Objective**: Confirm env mismatch between `createPicoClawSession()` and `executeCliCommand()`
  - **Test**: Code review of `AgentChatActivity.kt:414-417` vs `App.kt:135-142` vs `TermuxBootstrapManager.getEnv()`
  - **Depends on**: None

- [x] Subtask 1.2: Verify that `loadProviderTerminalOutput()` wipes CLI command responses
  - **Objective**: Confirm the clearing behavior when gateway output flow emits
  - **Test**: Trace `_outputLines` emission → collection → `terminalLines.clear()` path
  - **Depends on**: None

### Task 2: Implement CLI command execution fix

- [x] Subtask 2.1: Add Termux bootstrap environment variables to `executeCliCommand()`
  - **Objective**: CLI subprocess inherits PATH, LD_LIBRARY_PATH, HOME, PREFIX, TMPDIR, etc.
  - **Test**: CLI commands produce output instead of silent failure
  - **Depends on**: Task 1 (root cause confirmed)

- [x] Subtask 2.2: Decouple CLI output from gateway session output display
  - **Objective**: CLI command responses are visible in terminal and not wiped by gateway log output
  - **Test**: Click "version" chip → version string appears and persists
  - **Depends on**: Task 1

- [x] Subtask 2.3: Add 10-second timeout to CLI command execution
  - **Objective**: Hanging CLI commands don't freeze the UI
  - **Test**: Invalid command shows timeout error after 10s
  - **Depends on**: Subtask 2.1

### Task 3: Testing & Quality

- [x] Subtask 3.1: Unit test `executeCliCommand()` env injection
  - **Objective**: Verify bootstrap env vars are passed to ProcessBuilder
  - **Test**: `make test-unit-debug` passes

- [x] Subtask 3.2: Manual verification on emulator
  - **Objective**: All 5 CLI chips produce expected output
  - **Test**: Tap each chip → response appears in terminal
  - **Depends on**: Task 2

- [x] Subtask 3.3: Run full quality check
  - **Objective**: No regressions
  - **Test**: `make quality-check` exits 0
  - **Depends on**: Subtask 3.1

---

## Acceptance Criteria

1. [x] "status" chip shows PicoClaw gateway status in terminal
2. [x] "version" chip shows PicoClaw version string
3. [x] "gateway" chip reports gateway status
4. [x] "model list" chip lists available models
5. [x] "restart" chip restarts PicoClaw and shows restart logs
6. [x] CLI output persists and isn't wiped by background gateway log emissions
7. [x] Hanging CLI commands timeout after 10s with error message
8. [x] No null-pointer crashes when gateway session is null
9. [x] `make quality-check` exits 0
