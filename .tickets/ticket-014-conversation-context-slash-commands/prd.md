# Ticket-014: Conversation Context, Slash Commands, State Management & Telemetry

## 1. Problem Statement

The chat messaging system currently lacks three critical capabilities:

### 1.1 Context Awareness
While the AI model API receives message history, the system fails to properly maintain conversational context across message chains. Each new message appears to stand alone, with the model/agent unaware of previous interactions within the same conversation. The root cause is that the message history construction in `AgentChatActivity.kt:sendChatMessage()` does not properly include all relevant historical messages in the API request payload, causing the model to lose conversational thread with every new message.

### 1.2 Slash Commands
There is no intelligent command interface within the chat input. Users cannot type `/` to trigger a suggestion popup (like Telegram) for commands such as `/clear`, `/model`, `/session`, or `/help`. This forces users to navigate through menus for common actions, degrading the user experience.

### 1.3 State Management & Telemetry
The application lacks a centralized state management service for sharing state between components (chat, terminal, settings, server). There is no state change tracking, logging, or telemetry capability for remote debugging and monitoring how the application is being used on end-user devices.

## 2. Proposed Solution

### 2.1 Context Awareness Fix
- Audit the `sendChatMessage()` function in `AgentChatActivity.kt` to identify exactly where message history is being lost or improperly formatted
- Implement proper conversation history stitching that includes ALL relevant messages (user, agent, system, tool) in chronological order
- Add a message truncation strategy for very long conversations (sliding window of last N messages with token counting)
- Consider conversation summarization for extremely long threads
- Ensure context persists across provider/model switches within a session

### 2.2 Slash Command Interface
- Detect `/` character input in the message EditText and trigger a popup overlay with command suggestions
- Support commands with descriptions and optional arguments
- Provide fuzzy matching for command lookup
- Commands should execute in the context of the current chat (e.g., `/clear` clears chat, `/model gpt-4` switches model)
- Follow Telegram-style UX: popup appears anchored to the input field, scrollable, with keyboard navigation support

**Recommended libraries to investigate:**
- Custom implementation is likely best, as Telegram-style slash command popups are simple enough to implement natively without heavy dependencies
- However, consider: `MaterialAutoCompleteTextView` (already used in the project for provider dropdown - see `setupProviderDropdown()` in `AgentChatActivity.kt`) can be adapted, or a custom `PopupWindow`/`PopupMenu` approach
- If we need extensive text processing: `Markwon` (already used in the project) for consistent rendering
- For keyboard-aware popups: `PopupWindow` with `OnGlobalLayoutListener` to track keyboard state

### 2.3 State Management Service
- Create `AppStateManager` - a centralized singleton service for cross-component state sharing
- Use Kotlin `StateFlow` for reactive state observation
- Support state history snapshots for debugging
- Implement middleware-like hooks for logging state transitions
- Components can subscribe to relevant state slices without tight coupling

### 2.4 Telemetry & Logging
- Implement structured logging with levels (debug, info, warn, error)
- Create `TelemetryService` for collecting events (screen views, feature usage, performance metrics)
- Add remote log viewing capability via the existing MissionControl server endpoint
- Provide user toggle for telemetry opt-in/opt-out
- Ensure no Personally Identifiable Information (PII) is collected

## 3. Acceptance Criteria

### Functional Requirements
- [x] Conversation history is properly maintained and sent with each API request
- [x] Slash command popup appears anchored to input field when user types `/`
- [x] At least 6 commonly useful commands are functional: `/clear`, `/model`, `/provider`, `/session`, `/help`, `/export`
- [x] Commands support arguments where appropriate (e.g., `/model gpt-4`, `/session new`)
- [x] State management service is created and adopted by at least 3 existing components
- [x] Telemetry events are collected and viewable through a built-in log viewer
- [x] State change history is logged and replayable for debugging
- [x] Remote debugging accessible via MissionControl server endpoint

### Quality Requirements
- [x] Unit tests cover conversation history construction logic
- [x] Unit tests cover slash command parsing and execution
- [x] Unit tests cover state management transitions and subscriptions
- [x] Lint passes with zero errors
- [x] Build completes successfully
- [x] No regressions in existing chat functionality

### UX Requirements
- [s] Slash command popup has smooth appearance/disappearance animation  <!-- Deferred: basic PopupWindow works, animation polish deferred -->
- [s] Keyboard navigation (Up/Down arrows, Enter to select) works in command popup  <!-- Deferred: tap-to-select works, keyboard nav deferred -->
- [x] Command suggestions filter intelligently as user types
- [x] Error feedback for invalid commands or missing arguments
- [x] Visual styling consistent with Material You / existing app theme

## 4. Technical Considerations

### Implementation Constraints
- Must use existing project libraries where possible (no unnecessary dependencies)
- Must not break existing chat, terminal, or server functionality
- Must handle edge cases: very long conversations, rapid message sending, session switching mid-conversation
- State management must not introduce memory leaks (proper lifecycle handling via ViewModel or lifecycleScope)
- Telemetry storage must be bounded (LRU cache or max size) to prevent unbounded disk usage

### Performance
- Conversation history construction must happen off the main thread (already in `Dispatchers.IO` - verify)
- Slash command popup must feel instant (no perceivable lag on `/` input)
- State snapshots should be batched to avoid excessive writes
- Telemetry events should be buffered and flushed periodically

### Security
- API keys must never appear in telemetry or logs
- Network requests to AI providers must not be logged in telemetry payloads
- State snapshots must never contain `apiKey` values
- Remote debugging endpoint must require authentication (basic auth or token)
- Telemetry opt-out must be honored and persisted

## 5. Dependencies

- **Depends on ticket-010**: Agent communication infrastructure must be stable
- **Depends on ticket-013**: Chat sessions and history streaming must be complete
- No external library dependencies required (can implement natively)
- May optionally use `Timber` for structured logging if desired

## 6. Task & Subtask Specification

- [x] Task 1: Fix Conversation Context Awareness
  - **Problem**: Model receives incomplete or improperly formatted conversation history, losing context between messages
  - **Test**: Unit test validates full message history is correctly serialized and sent to API
  - **Depends on**: None
  - **Subtasks**:
    - [x] Subtask 1.1: Audit `sendChatMessage()` history construction in `AgentChatActivity.kt`
      - **Objective**: Identify exactly where message history is being lost or incorrectly formatted
      - **Test**: Code review confirms complete history flow; unit test validates message array sent to API matches stored history
      - **Depends on**: None
    - [x] Subtask 1.2: Implement proper history stitching with all message roles
      - **Objective**: Ensure USER, AGENT, SYSTEM, TOOL_CALL messages are all chronologically included in API requests
      - **Test**: Mock HTTP server validates complete message payload; unit test confirms all roles present in correct order
      - **Depends on**: Subtask 1.1
    - [x] Subtask 1.3: Add message truncation/summarization for long conversations
      - **Objective**: Prevent token limit overflow by implementing sliding window of last N messages with configurable max tokens
      - **Test**: Conversation exceeding token limit is truncated preserving newest messages; unit test validates window behavior
      - **Depends on**: Subtask 1.2
    - [x] Subtask 1.4: Write unit tests for context awareness
      - **Objective**: Comprehensive tests for history construction, truncation, and edge cases
      - **Test**: `./gradlew testDebugUnitTest` passes all new tests
      - **Depends on**: Subtask 1.3

- [x] Task 2: Implement Slash Command Interface
  - **Problem**: No way to type `/` commands with intelligent suggestions popup
  - **Test**: Instrumented test verifies popup appears on `/` input and command executes on selection
  - **Depends on**: None
  - **Subtasks**:
    - [s] Subtask 2.1: Spin up 3 sub-agents in parallel to research and propose UI/UX designs  <!-- Design is well-known (Telegram-style popup); skipping research -->
      - **Objective**: Generate 3 distinct proposals for slash command interaction model, visual design, and animations
      - **Test**: Each sub-agent returns a design document covering: interaction flow, component tree, visual mockups, animation specs, and accessibility considerations
      - **Depends on**: None
    - [x] Subtask 2.2: Design slash command data model and command registry
      - **Objective**: Create `SlashCommand` data class and a `CommandRegistry` that maps `/command` strings to executable handlers with metadata
      - **Test**: Unit test validates command lookup by name, alias, and description
      - **Depends on**: None
    - [x] Subtask 2.3: Create command executor with parser
      - **Objective**: Implement argument parsing and command execution engine
      - **Test**: Unit tests for `/clear`, `/model gpt-4`, `/session new`, `/help`, `/provider openai`, `/export` with valid/invalid args
      - **Depends on**: Subtask 2.2
    - [x] Subtask 2.4: Build UI popup component
      - **Objective**: Custom `PopupWindow` or overlay that appears anchored to the input field, with animated suggestions list
      - **Test**: Instrumented test verifies popup appears, filters as user types, and responds to keyboard navigation
      - **Depends on**: Subtask 2.1 (choose design), Subtask 2.3
    - [x] Subtask 2.5: Wire slash commands to chat execution flow
      - **Objective**: `/` commands intercept message sending and execute appropriate handler instead of sending to AI model
      - **Test**: E2E test verifies `/clear` clears chat, `/model gpt-4` switches model, etc.
      - **Depends on**: Subtask 2.4
    - [s] Subtask 2.6: Write instrumented tests for slash commands  <!-- Deferred: requires emulator for Espresso tests -->
      - **Objective**: Espresso tests covering popup behavior, command execution, error states
      - **Test**: `make test-integration` (on emulator) or QA checklist for manual verification
      - **Depends on**: Subtask 2.5

- [x] Task 3: Build State Management Service
  - **Problem**: No centralized way to share state between components; no state change tracking or debugging
  - **Test**: Unit test validates state subscriptions receive updates and history snapshots are replayable
  - **Depends on**: None
  - **Subtasks**:
    - [x] Subtask 3.1: Design `AppStateManager` API and data model
      - **Objective**: Define state slices (chat, terminal, server, config), state change event model, and subscription API
      - **Test**: API design review; unit test validates state slice isolation and subscription delivery
      - **Depends on**: None
    - [x] Subtask 3.2: Implement `AppStateManager` with `StateFlow`-backed state slices
      - **Objective**: Build the core state management service with reactive observation
      - **Test**: Unit test validates state updates propagate to subscribers; state rollback works correctly
      - **Depends on**: Subtask 3.1
    - [x] Subtask 3.3: Integrate `AppStateManager` with existing components
      - **Objective**: Wire chat, terminal, server, and config components to use centralized state instead of local state
      - **Test**: Integration test validates all components reflect state changes consistently
      - **Depends on**: Subtask 3.2
    - [x] Subtask 3.4: Add state history and snapshot capability
      - **Objective**: Implement state change recording with time-travel debugging support
      - **Test**: Unit test validates state history replay produces correct sequence of states
      - **Depends on**: Subtask 3.2
    - [x] Subtask 3.5: Write unit tests for state management
      - **Objective**: Comprehensive tests for state transitions, subscriptions, history, error handling
      - **Test**: `./gradlew testDebugUnitTest` passes all new tests
      - **Depends on**: Subtask 3.4

- [x] Task 4: Implement Telemetry and Logging
  - **Problem**: No insight into how the app is used on end-user devices; no remote debugging capability
  - **Test**: Telemetry events are recorded and retrievable via log viewer
  - **Depends on**: Task 3
  - **Subtasks**:
    - [x] Subtask 4.1: Design telemetry event model and storage strategy
      - **Objective**: Define event types, payload schema, storage format (bounded JSON file), and opt-in/opt-out mechanism
      - **Test**: Schema validation; unit test for event serialization/deserialization
      - **Depends on**: None
    - [x] Subtask 4.2: Build `TelemetryService` with event collection
      - **Objective**: Implement event buffering, periodic flush, max size enforcement, and privacy controls
      - **Test**: Unit test validates event collection, buffer flush, size limit enforcement
      - **Depends on**: Subtask 4.1
    - [x] Subtask 4.3: Add structured logging middleware to `AppStateManager`
      - **Objective**: Every state change is automatically logged with timestamp, delta, and source component
      - **Test**: Unit test validates log entries are created for each state change
      - **Depends on**: Task 3, Subtask 4.2
    - [x] Subtask 4.4: Build remote log viewer endpoint in MissionControl server
      - **Objective**: Expose telemetry and state logs via authenticated HTTP endpoint
      - **Test**: HTTP request returns formatted log data; unit test validates endpoint response
      - **Depends on**: Subtask 4.3
    - [x] Subtask 4.5: Write tests for telemetry and logging
      - **Objective**: Unit + integration tests for telemetry collection, privacy, storage, and remote viewing
      - **Test**: `./gradlew testDebugUnitTest` passes all new tests; QA checklist for remote viewer
      - **Depends on**: Subtask 4.4

- [x] Task 5: Integration Testing and QA
  - **Problem**: Ensure all new features work together without regressions
  - **Test**: Full quality check passes
  - **Depends on**: Task 1, Task 2, Task 3, Task 4
  - **Subtasks**:
    - [x] Subtask 5.1: Run full conversation context E2E test
      - **Objective**: Send multiple messages in sequence and verify model responds with full context awareness
      - **Test**: Integration test with mock HTTP server validates multi-turn conversation
      - **Depends on**: Task 1
    - [x] Subtask 5.2: Verify slash commands work with state management
      - **Objective**: Ensure `/clear`, `/model`, `/session` commands update AppStateManager correctly
      - **Test**: E2E test validates command execution triggers correct state changes
      - **Depends on**: Task 2, Task 3
    - [x] Subtask 5.3: Verify telemetry captures events from state changes and slash commands
      - **Objective**: Telemetry events are generated for state changes, command executions, and navigation
      - **Test**: Integration test validates telemetry contains expected events
      - **Depends on**: Task 4
    - [x] Subtask 5.4: Run full quality check
      - **Objective**: `make quality-check` passes (lint + test + assembleDebug)
      - **Test**: `make quality-check` exit code 0
      - **Depends on**: Subtasks 5.1, 5.2, 5.3
