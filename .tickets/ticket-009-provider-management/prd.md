# Ticket 009: Provider Management UI

## Status: Backlog
**Created**: 2026-05-17  
**Depends on**: ticket-008 (UI/UX overhaul)

---

## Problem
Users need to add/remove AI providers (OpenAI, Anthropic, DeepSeek, etc.) with their API keys and optional custom URLs. Currently there is no UI for this — the user would need to manually edit a JSON file, which is impractical on Android. Adding or changing providers requires PicoClaw to be restarted.

## Scope
Create a minimal, focused provider management screen that:
1. Lists currently configured providers with key status
2. Lets users add new providers with a quick-fill dropdown
3. Lets users edit or delete existing providers
4. Persists changes to PicoClaw's `config.json`
5. Triggers PicoClaw restart to apply changes

---

## PicoClaw Config Format
The config file lives at `files/picoclaw/config.json`. The relevant section:

```json
{
  "model_list": [
    {
      "model_name": "gpt-5.4",
      "model": "openai/gpt-5.4",
      "api_key": "sk-your-openai-key",
      "api_base": "https://api.openai.com/v1"
    }
  ]
}
```

### Model Provider Fields
| Field | Required | Description |
|-------|----------|-------------|
| `model_name` | Yes | Display name shown in list |
| `model` | Yes | Provider/model ID, format: `provider/name` |
| `api_key` | No | API key (masked in UI, optional for local models) |
| `api_base` | No | Custom API endpoint URL |
| `auth_method` | No | For OAuth-based providers (e.g., Gemini) |
| `thinking_level` | No | For Anthropic extended thinking |

---

## Predefined Providers (Quick-Fill)
Dropdown presets for common providers, auto-filling `model` and `api_base`:

| Provider | Model ID | Default API Base |
|----------|----------|-----------------|
| OpenAI | `openai/gpt-5.4` | `https://api.openai.com/v1` |
| Anthropic | `anthropic/claude-sonnet-4.6` | `https://api.anthropic.com/v1` |
| DeepSeek | `deepseek/deepseek-chat` | — |
| Google Gemini | `antigravity/gemini-2.0-flash` | — |
| Azure OpenAI | `azure/my-deployment` | `https://your-resource.openai.azure.com` |
| Venice | `venice/venice-uncensored` | — |
| LongCat | `longcat/LongCat-Flash-Thinking` | — |
| Modelscope Qwen | `modelscope/Qwen/Qwen3-235B-A22B-Instruct-2507` | `https://api-inference.modelscope.cn/v1` |
| LM Studio (local) | `lmstudio/openai/gpt-oss-20b` | — |
| Custom | (user fills all fields) | — |

---

## Tasks

### Task 1: Create Provider Data Layer
- **Subtasks**:
  - [ ] Subtask 1.1: Create `model/ModelProvider.kt` data class
  - [ ] Subtask 1.2: Create `config/ProviderConfigManager.kt` — read/write `config.json`, merge modelList
  - [ ] Subtask 1.3: Load existing config on app startup, create default if missing

### Task 2: Create Provider List UI
- **Subtasks**:
  - [ ] Subtask 2.1: Create `activity_providers.xml` — RecyclerView with MaterialCardView items
  - [ ] Subtask 2.2: Create `ProviderListActivity.kt` — loads model_list, navigates to add/edit/delete
  - [ ] Subtask 2.3: Create item layout `item_provider.xml` — model_name, model, key status chip, edit/delete buttons
  - [ ] Subtask 2.4: Add FAB for "Add Provider"

### Task 3: Create Provider Add/Edit Dialog
- **Subtasks**:
  - [ ] Subtask 3.1: Create `dialog_provider.xml` — 4 fields + predefined dropdown
  - [ ] Subtask 3.2: Create `ProviderEditDialog.kt` (BottomSheetDialogFragment)
  - [ ] Subtask 3.3: Implement predefined provider quick-fill dropdown
  - [ ] Subtask 3.4: Add API key visibility toggle (eye icon)
  - [ ] Subtask 3.5: Validate model field (required, format check)

### Task 4: Wire Into Main App
- **Subtasks**:
  - [ ] Subtask 4.1: Add "Providers" button to Actions card in `activity_main.xml`
  - [ ] Subtask 4.2: Navigate to `ProviderListActivity` from MainActivity
  - [ ] Subtask 4.3: Declare new activity in `AndroidManifest.xml`

### Task 5: PicoClaw Reload on Provider Change
- **Subtasks**:
  - [ ] Subtask 5.1: After save, show Snackbar "Provider saved. Restart to apply."
  - [ ] Subtask 5.2: Add "Restart PicoClaw" menu item to ProviderListActivity
  - [ ] Subtask 5.3: Restart calls `terminalManager.stopPicoClaw()` then `launchPicoClaw()`
  - [ ] Subtask 5.4: Show loading state during restart

### Task 6: Update Tests
- **Subtasks**:
  - [ ] Subtask 6.1: Update Espresso tests if needed for new button
  - [ ] Subtask 6.2: Add `ProviderScenario.kt` acceptance test
  - [ ] Subtask 6.3: Run full quality check

---

## Files

### New
| File | Purpose |
|------|---------|
| `model/ModelProvider.kt` | Data class for provider entries |
| `config/ProviderConfigManager.kt` | JSON read/write for config.json |
| `ProviderListActivity.kt` | List all providers |
| `ProviderEditDialog.kt` | Add/edit dialog |
| `res/layout/activity_providers.xml` | List layout with RecyclerView + FAB |
| `res/layout/item_provider.xml` | Single provider card in list |
| `res/layout/dialog_provider.xml` | Add/edit form |

### Modified
| File | Change |
|------|--------|
| `MainActivity.kt` | Add `btnProviders` button → navigates to ProviderListActivity |
| `activity_main.xml` | Add "Providers" button to Actions card |
| `AndroidManifest.xml` | Declare `ProviderListActivity` |
| `strings.xml` | Add provider-related strings |

---

## Acceptance Criteria
1. "Providers" button visible in main Actions card
2. Tapping opens provider list showing configured models
3. Each card shows: display name, model ID, key status chip (green = set, gray = not set)
4. Tapping a card opens edit dialog pre-filled with that provider's data
5. FAB opens add dialog
6. Predefined dropdown auto-fills model + api_base
7. API key field has visibility toggle
8. Saving writes to config.json and shows Snackbar with restart prompt
9. "Restart PicoClaw" in menu restarts the process
10. Deleting a provider shows confirmation and removes from list
11. All existing tests still pass
