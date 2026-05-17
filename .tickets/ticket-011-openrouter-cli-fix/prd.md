# Ticket 011: OpenRouter Provider & CLI Button Fixes

## Status: Done
**Created**: 2026-05-17
**Depends on**: ticket-009 (provider management), ticket-010 (agent communication)

---

## Problem Statement

Two critical bugs in the agent communication flow (ticket-010):

1. **OpenRouter models not loading/fetching**: OpenRouter is configured as a provider but model fetching fails. Chatting with the OpenRouter provider does not produce responses.
2. **Quick CLI buttons produce no output**: The command chips (status, version, gateway, model list, restart) in the terminal bottom sheet only echo the command text but never execute against the PicoClaw process or capture output.

### Root Cause Analysis

**Bug 1 — OpenRouter model fetch & chat**:
- `AgentChatActivity.kt:180-224` — `fetchModelsFromProvider()` hits `$baseUrl/models` but OpenRouter's `/v1/models` endpoint returns a different JSON structure (flat `data` array with `id`, `name`, `context_length`, `pricing` fields). The current parser expects OpenAI-style `data[].id` which may work, but the real failure is likely:
  - OpenRouter provider entries may have `api_base` empty in `config.json` (predefined provider table in ticket-009 shows OpenRouter has no default `api_base` set)
  - The fallback `https://api.openai.com/v1` is wrong for OpenRouter — should be `https://openrouter.ai/api/v1`
  - Chat completion at line 294 sends to `$baseUrl/chat/completions` — OpenRouter supports this but requires `HTTP-Referer` and `X-Title` headers (OpenRouter-specific requirement)
  - Model field in config uses format `openrouter/anthropic/claude-sonnet-4-5` but the chat request sends the raw `model` value which may include the `openrouter/` prefix that OpenRouter's API doesn't expect (it wants just `anthropic/claude-sonnet-4-5`)

**Bug 2 — CLI buttons**:
- `AgentChatActivity.kt:338-343` — `sendTerminalCommand()` only appends `"> $command"` to `terminalLines` and updates the RecyclerView. It never sends the command to the PicoClaw process stdin or captures stdout.
- `loadProviderTerminalOutput()` (line 359-371) correctly subscribes to `TermuxSession.outputLines` but this only captures PicoClaw's own log output, not command responses.

## Proposed Solution

### Bug 1 — OpenRouter Fix
1. Add OpenRouter to the predefined providers table with `api_base = "https://openrouter.ai/api/v1"`
2. In `fetchModelsFromProvider()`, handle OpenRouter's model response format (may include additional fields, need to filter to usable models)
3. In `sendChatMessage()`, add OpenRouter-specific headers (`HTTP-Referer`, `X-Title`) when the provider's api_base contains "openrouter"
4. Strip the `openrouter/` prefix from the model name when sending to OpenRouter's `/v1/chat/completions` endpoint
5. Add error logging with HTTP status code and response body for debugging

### Bug 2 — CLI Button Fix
1. Create a mechanism to send commands to the running PicoClaw process and capture output
2. Options:
   - **Option A**: Write commands to PicoClaw's stdin via the Termux session's input stream (if accessible)
   - **Option B**: Use PicoClaw's local HTTP API (if it exposes a command endpoint)
   - **Option C**: Write commands to a file/pipe that PicoClaw monitors
3. Capture the response and append to `terminalLines`
4. Update command chips to use the new send-and-capture flow

## Technical Considerations

- OpenRouter's `/v1/models` endpoint returns all available models (hundreds). Should filter/paginate to avoid overwhelming the dropdown
- OpenRouter requires `HTTP-Referer` and `X-Title` headers for usage tracking (documented in their API spec)
- PicoClaw's terminal session may not expose a writable stdin stream — need to check `TermuxSession` API
- CLI commands should have a timeout (5-10s) to avoid hanging the UI
- Terminal output should be rate-limited to avoid flooding the RecyclerView

## Dependencies

- **Depends on ticket-009**: Provider configuration and predefined providers list
- **Depends on ticket-010**: AgentChatActivity, terminal bottom sheet, command chips

---

## Tasks

### Task 1: Fix OpenRouter Provider Configuration
- [x]
- **Problem**: OpenRouter missing from predefined providers with correct api_base
- **Test**: OpenRouter provider shows with `https://openrouter.ai/api/v1` as default endpoint
- **Depends on**: None
- **Subtasks**:
  - [x] Subtask 1.1: Add OpenRouter to predefined providers in ProviderEditDialog with `api_base = "https://openrouter.ai/api/v1"`
    - **Objective**: OpenRouter quick-fill auto-populates correct endpoint
    - **Test**: Selecting OpenRouter preset fills api_base field
  - [x] Subtask 1.2: Update `fetchModelsFromProvider()` to handle OpenRouter model list format
    - **Objective**: Models fetched from OpenRouter display correctly in dropdown
    - **Test**: Tap "Fetch Models" with OpenRouter selected → models appear in dropdown
  - [x] Subtask 1.3: Add OpenRouter-specific headers to chat completion requests
    - **Objective**: `HTTP-Referer` and `X-Title` headers sent when api_base contains "openrouter"
    - **Test**: Network inspection shows OpenRouter headers present in requests

### Task 2: Fix OpenRouter Chat Completion
- [x]
- **Problem**: Chat messages to OpenRouter provider fail or return errors
- **Test**: Send message with OpenRouter selected → agent response appears
- **Depends on**: Task 1
- **Subtasks**:
  - [x] Subtask 2.1: Strip `openrouter/` prefix from model name in chat request
    - **Objective**: Model field sent as `anthropic/claude-sonnet-4-5` not `openrouter/anthropic/claude-sonnet-4-5`
    - **Test**: Request body contains correct model identifier
  - [x] Subtask 2.2: Add detailed error logging for chat failures (HTTP status, response body)
    - **Objective**: Errors show actual API response instead of generic exception message
    - **Test**: Invalid API key shows "401 Unauthorized" error message
  - [x] Subtask 2.3: Handle HTTP error responses (non-200) gracefully
    - **Objective**: Read errorStream when response code >= 400
    - **Test**: Bad request shows meaningful error instead of crash

### Task 3: Fix CLI Command Execution
- [x]
- **Problem**: Command chips only echo text, never execute or capture output
- **Test**: Tap "status" chip → PicoClaw status output appears in terminal
- **Depends on**: None
- **Subtasks**:
  - [x] Subtask 3.1: Investigate TermuxSession API for stdin write capability
    - **Objective**: Determine if commands can be sent to running PicoClaw process
    - **Test**: Document whether stdin is accessible
  - [x] Subtask 3.2: Implement command execution via available mechanism (stdin write or alternative)
    - **Objective**: Commands sent to PicoClaw and output captured
    - **Test**: "version" command returns PicoClaw version string
  - [x] Subtask 3.3: Add command timeout (10s) and error handling
    - **Objective**: Commands that hang or fail show error message
    - **Test**: Invalid command shows error after timeout
  - [x] Subtask 3.4: Wire command chips to new execution flow
    - **Objective**: All 5 chips (status, version, gateway, model list, restart) work
    - **Test**: Each chip produces expected output in terminal

### Task 4: Testing & Quality
- [x]
- **Problem**: Verify fixes don't break existing functionality
- **Test**: Full quality check passes
- **Depends on**: Task 1, Task 2, Task 3
- **Subtasks**:
  - [x] Subtask 4.1: Run unit tests for modified components
    - **Objective**: No regressions in provider/chat logic
    - **Test**: `./gradlew test` passes
  - [x] Subtask 4.2: Run full quality check (lint + test + assembleDebug)
    - **Objective**: All quality gates pass
    - **Test**: `./gradlew lint && ./gradlew test && ./gradlew assembleDebug` all exit 0

---

## Files

### Modified
| File | Change |
|------|--------|
| `ProviderEditDialog.kt` | Add OpenRouter to predefined providers |
| `AgentChatActivity.kt` | Fix model fetch, add OpenRouter headers, fix model prefix, fix CLI execution, add error handling |
| `model/ModelProvider.kt` | May need helper method for provider detection |

---

## Acceptance Criteria
1. [x] OpenRouter appears in predefined provider dropdown with correct `api_base`
2. [x] "Fetch Models" with OpenRouter selected returns and displays models
3. [x] Chat message sent to OpenRouter provider receives a valid response
4. [x] OpenRouter-specific headers (`HTTP-Referer`, `X-Title`) included in requests
5. [x] Error messages show HTTP status and API error details (not generic exceptions)
6. [x] CLI command chips (status, version, gateway, model list, restart) execute and show output
7. [x] Command timeout prevents UI hangs
8. [x] All existing tests still pass
9. [x] Full quality check (lint + test + assembleDebug) exits 0
