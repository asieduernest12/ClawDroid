# Ticket 010: Agent Communication — Chat + CLI + Provider Switching

## Status: Done
**Created**: 2026-05-17
**Depends on**: ticket-009 (provider management)

---

## Problem
Users can configure providers (ticket-009) but have no way to:
1. Quickly switch between providers/models
2. Fetch available models from a provider's API
3. Chat/interact with PicoClaw agents to get them to perform actions
4. See live PicoClaw gateway output
5. Send raw CLI commands to PicoClaw

The only way to interact with PicoClaw currently is via the WebView Mission Control, which is a secondary hop and lacks native feel.

## Solution
A single-screen "Chat-First Terminal" architecture where chat is the hero (70%) and terminal is a collapsible inspector (30%). Provider/model switching is in the toolbar. This screen becomes the primary landing after bootstrap.

## Mockup Selected
**Chat-First Terminal** (23/25 score). See `.tickets/ticket-010-agent-communication/mockups/01-chat-first-terminal.md` for full spec.

---

## Tasks

### Task 1: Provider/Model Toolbar Selectors
- **Problem**: User needs to switch AI providers and models quickly
- **Test**: Dropdowns show configured providers, model list fetched from provider
- **Subtasks**:
  - [ ] Subtask 1.1: Add provider dropdown (ExposedDropdownMenu) to activity_agent.xml toolbar
  - [ ] Subtask 1.2: Add model dropdown — fetches available models from active provider API
  - [ ] Subtask 1.3: Create `ModelFetcher.kt` — calls provider's /v1/models endpoint with API key
  - [ ] Subtask 1.4: Cache fetched models per provider in memory

### Task 2: Agent Chat Interface
- **Problem**: Users need to send messages to PicoClaw agents and see responses
- **Test**: Send message → agent responds with markdown-rendered reply
- **Subtasks**:
  - [ ] Subtask 2.1: Create `activity_agent.xml` — CoordinatorLayout + RecyclerView + input bar + BottomSheet
  - [ ] Subtask 2.2: Create `item_chat_message.xml` — message bubble layout (agent left, user right)
  - [ ] Subtask 2.3: Create `AgentChatActivity.kt` — manages chat messages, sends to PicoClaw API
  - [ ] Subtask 2.4: Implement markdown rendering (bold, italic, code blocks, lists) using Android Spans
  - [ ] Subtask 2.5: Add typing indicator (3-dot animation) while agent processes
  - [ ] Subtask 2.6: Auto-scroll to bottom on new messages

### Task 3: Collapsible CLI Terminal
- **Problem**: Need to see live PicoClaw gateway output and send raw commands
- **Test**: Terminal shows live gateway logs, command input sends to PicoClaw
- **Subtasks**:
  - [ ] Subtask 3.1: Add BottomSheet terminal panel to activity_agent.xml (peek height 30%)
  - [ ] Subtask 3.2: Bind terminal output to `EmbeddedTermuxSession.outputLines` StateFlow
  - [ ] Subtask 3.3: Add raw command input at bottom of terminal panel
  - [ ] Subtask 3.4: Implement stdin writing via `EmbeddedTermuxSession.writeStdin()`
  - [ ] Subtask 3.5: Add chevron toggle to expand/collapse terminal

### Task 4: Navigation & Wiring
- **Problem**: New chat screen must be reachable from main dashboard
- **Test**: FAB on main screen navigates to AgentChatActivity when PicoClaw is running
- **Subtasks**:
  - [ ] Subtask 4.1: Update MainActivity FAB to navigate to AgentChatActivity when PicoClaw is running
  - [ ] Subtask 4.2: Declare AgentChatActivity in AndroidManifest.xml
  - [ ] Subtask 4.3: Add "Chat with Agent" button to Actions card as primary action
  - [ ] Subtask 4.4: Update strings.xml with chat/terminal related strings

### Task 5: CLI Command Control
- **Problem**: Common PicoClaw commands need simple UI buttons
- **Test**: Command chips in terminal send predefined commands to PicoClaw
- **Subtasks**:
  - [ ] Subtask 5.1: Add horizontal command chip row above terminal output
  - [ ] Subtask 5.2: Predefined commands: "status", "version", "gateway restart", "model list"
  - [ ] Subtask 5.3: Show command output inline in terminal

### Task 6: Update Tests
- **Subtasks**:
  - [ ] Subtask 6.1: Update existing acceptance tests for new navigation
  - [ ] Subtask 6.2: Run full quality check (lint + test + assembleDebug)

---

## Files

### New
| File | Purpose |
|------|---------|
| `AgentChatActivity.kt` | Main chat screen with provider/model switching + terminal |
| `model/ChatMessage.kt` | Data class for chat messages (role, content, timestamp) |
| `model/ModelFetcher.kt` | Fetches available models from provider API |
| `res/layout/activity_agent.xml` | Chat screen layout (CoordinatorLayout + BottomSheet) |
| `res/layout/item_chat_message.xml` | Message bubble layout |

### Modified
| File | Change |
|------|--------|
| `MainActivity.kt` | FAB navigates to AgentChatActivity when running |
| `AndroidManifest.xml` | Declare AgentChatActivity |
| `strings.xml` | Chat/terminal strings |

---

## Acceptance Criteria
1. [ ] Toolbar shows active provider + model dropdowns
2. [ ] Model dropdown fetches and displays models from active provider's API
3. [ ] User can type a message and send to agent
4. [ ] Agent response appears with markdown rendered (bold, code blocks, lists)
5. [ ] Typing indicator shows while agent processes
6. [ ] Terminal bottom sheet shows live PicoClaw gateway output
7. [ ] Raw command input sends to PicoClaw stdin
8. [ ] Command chips send predefined commands (status, version, etc.)
9. [ ] Chevron toggle expands/collapses terminal
10. [ ] FAB on main screen opens agent chat when PicoClaw is running
11. [ ] All existing tests still pass
