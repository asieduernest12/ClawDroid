# Ticket 013: Full-History Chat Sessions with Thinking Stream & Tool Calls

## Problem

The current `AgentChatActivity` has critical limitations:
1. **No context persistence**: Each API call sends only the current user message — the model loses all previous context immediately.
2. **No session management**: All messages live in an in-memory `RecyclerView.Adapter`. Killing the app or rotating the screen wipes the conversation. There is no way to have multiple independent conversations.
3. **No thinking visibility**: Users cannot see the model's reasoning chain (e.g., `<think>...</think>` blocks from DeepSeek, `reasoning_content` from OpenRouter, or tool-call planning).
4. **No tool call visibility**: When the agent invokes tools (function calls), the user sees only the final result — not the call arguments or intermediate steps.

## Goals

1. **Full history thread**: Every API request must include the entire session message history so the agent retains context.
2. **Multiple independent chat sessions**: Users can create, rename, switch between, and delete chat sessions. Sessions persist across app restarts.
3. **Model thinking stream**: Display reasoning/thinking content in a collapsible UI block above the final agent reply.
4. **Tool call visibility**: Display tool invocations (name + arguments + result) in a structured, expandable UI block.
5. **Deploy workflow**: Add a GitHub Actions workflow (based on the ptimeout/hatmountain pattern) that auto-merges `staging` → `main`, bumps version, builds APK, and creates a GitHub release.

## Acceptance Criteria

- [ ] Sending a message includes all prior messages in the same session in the `messages` array.
- [ ] Sessions survive process death (stored locally via SharedPreferences / JSON or Room).
- [ ] Session list UI allows creating a new session, switching sessions, renaming, and deleting.
- [ ] Thinking content is parsed from the API response and shown in a collapsible card labeled "Thinking...".
- [ ] Tool calls are parsed from the API response and shown in a collapsible card with call name, JSON arguments, and result.
- [ ] GitHub Actions workflow file exists in `.github/workflows/deploy.yml` and matches the ptimeout pattern (version bump + build + release).
- [ ] `make build-debug` passes inside the Docker container.
- [ ] Unit tests pass (`make test-unit`).

## Out of Scope

- Server-side session sync (cloud persistence)
- Real-time WebSocket streaming (keep using HTTP polling / completion for now)
- Message editing / branching conversation tree

## Implementation Plan

### Phase 1: Data Models & Persistence
1. Add `ChatSession` model (`id`, `title`, `createdAt`, `updatedAt`, `providerId`, `modelId`).
2. Extend `ChatMessage` with `sessionId`, `thinkingContent`, `toolCallsJson`.
3. Add `ChatHistoryManager` (SharedPreferences-backed JSON store for sessions + messages).

### Phase 2: Full History in API Calls
1. Update `AgentChatActivity.sendChatMessage()` to query `ChatHistoryManager` for the current session's messages and build the full `messages` JSONArray.

### Phase 3: Session Management UI
1. Add a session drawer or bottom sheet to list sessions.
2. Add "New Session" FAB or toolbar action.
3. Auto-generate session titles from the first user message (truncated).

### Phase 4: Thinking Stream UI
1. Add `item_chat_thinking.xml` layout (collapsible card with monospace text).
2. Parse `reasoning_content` (OpenRouter) or `<think>` blocks from response.
3. Store in `ChatMessage.thinkingContent` and render in adapter.

### Phase 5: Tool Call Visibility UI
1. Add `item_chat_toolcall.xml` layout (structured card: name, args, result).
2. Parse `tool_calls` from API response.
3. Store in `ChatMessage.toolCallsJson` and render in adapter.

### Phase 6: GitHub Actions Workflow
1. Create `.github/workflows/deploy.yml` following the ptimeout release pattern.
2. On push to `main`: bump version in `version.txt` or `build.gradle`, run `make build-release`, attach APK to GitHub release.

## Files to Touch

- `app/src/main/java/com/example/clawdroid/model/ChatMessage.kt`
- `app/src/main/java/com/example/clawdroid/model/ChatSession.kt` (new)
- `app/src/main/java/com/example/clawdroid/chat/ChatHistoryManager.kt` (new)
- `app/src/main/java/com/example/clawdroid/AgentChatActivity.kt`
- `app/src/main/java/com/example/clawdroid/ChatAdapter.kt` (extract from activity)
- `app/src/main/res/layout/activity_agent.xml`
- `app/src/main/res/layout/item_chat_message.xml`
- `app/src/main/res/layout/item_chat_thinking.xml` (new)
- `app/src/main/res/layout/item_chat_toolcall.xml` (new)
- `app/src/main/res/layout/dialog_sessions.xml` (new)
- `app/src/main/res/menu/menu_agent.xml`
- `app/src/main/res/values/strings.xml`
- `.github/workflows/deploy.yml` (new)
