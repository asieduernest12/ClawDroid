# Provider Management UI — Minimal Mockup

## Layout: `activity_providers.xml`

```
┌──────────────────────────────────────────┐
│  ← Providers                    [+ ADD]  │  ← Toolbar with back arrow + add button
├──────────────────────────────────────────┤
│                                          │
│  ┌─ OpenAI ──────────────────────────┐   │
│  │  gpt-5.4                   [EDIT] │   │  ← Card: model name + model identifier
│  │  openai/gpt-5.4                   │   │
│  │  api.openai.com/v1                │   │  ← api_base shown if custom
│  │  ●●●●●●●●●●               [DEL]  │   │  ← masked key + delete icon
│  └────────────────────────────────────┘   │
│                                          │
│  ┌─ Anthropic ───────────────────────┐   │
│  │  claude-sonnet-4.6         [EDIT] │   │
│  │  anthropic/claude-sonnet-4.6      │   │
│  │  ●●●●●●●●●●               [DEL]  │   │
│  └────────────────────────────────────┘   │
│                                          │
│  ┌─ Local LM Studio ────────────────┐   │
│  │  lmstudio-local            [EDIT] │   │
│  │  lmstudio/openai/gpt-oss-20b     │   │
│  │  (no key set)             [DEL]  │   │  ← local providers may have no key
│  └────────────────────────────────────┘   │
│                                          │
│                              ┌────────┐  │
│                              │  + ADD │  │  ← FAB (shown only on scroll up)
│                              └────────┘  │
└──────────────────────────────────────────┘
```

## Add/Edit Dialog: `dialog_provider.xml`

```
┌─ Add Provider ──────────────────────────┐
│                                          │
│  Display Name *                          │
│  ┌──────────────────────────────────────┐│
│  │ gpt-5.4                              ││
│  └──────────────────────────────────────┘│
│  Friendly name shown in the list.        │
│                                          │
│  Provider/Model *                        │
│  ┌──────────────────────────────────────┐│
│  │ openai/gpt-5.4                       ││
│  └──────────────────────────────────────┘│
│  Format: provider/model-identifier       │
│                                          │
│  API Key                                 │
│  ┌──────────────────────────────────────┐│
│  │ sk-your-key-here                     ││  ← TextInputLayout with toggle visibility
│  └──────────────────────────────────────┘│
│  (optional for local providers)          │
│                                          │
│  API Base URL                            │
│  ┌──────────────────────────────────────┐│
│  │ https://api.openai.com/v1            ││
│  └──────────────────────────────────────┘│
│  (optional — defaults to provider's own) │
│                                          │
│  ┌──────────────────────────────────────┐│
│  │ Predefined Providers         [DROPDN]││  ← Quick-fill dropdown
│  │  ├ OpenAI                            ││
│  │  ├ Anthropic                         ││
│  │  ├ DeepSeek                          ││
│  │  ├ Google (Gemini)                   ││
│  │  ├ Azure OpenAI                      ││
│  │  ├ Venice                            ││
│  │  ├ LongCat                           ││
│  │  ├ Modelscope (Qwen)                 ││
│  │  ├ LM Studio (local)                 ││
│  │  └ Custom...                         ││
│  └──────────────────────────────────────┘│
│                                          │
│               [CANCEL]  [SAVE PROVIDER]  │
└──────────────────────────────────────────┘
```

## Behavior

1. **List screen** (`ProviderListActivity`):
   - Shows all models from `config.json` > `model_list`
   - Each card shows: display name, model ID, key status (set/not set), custom URL if any
   - Tap → edit dialog
   - Swipe left → delete with confirmation
   - FAB → add dialog
   - Menu item: "Reload PicoClaw" — restarts process to pick up new config

2. **Add/Edit dialog** (BottomSheetDialogFragment or AlertDialog):
   - 4 fields: display_name, model, api_key, api_base
   - Predefined providers dropdown auto-fills model + api_base
   - API key has visibility toggle (eye icon)
   - Validation: model field required, must match `provider/name` format

3. **On save**:
   - Write updated `model_list` to `config.json`
   - Show Snackbar: "Provider saved. Restart PicoClaw to apply."
   - If picoclaw is running, offer "Restart Now" action in Snackbar

4. **Reload mechanism**:
   - PicoClaw reads config on startup only (no hot reload in current version)
   - So after any provider change, user must restart PicoClaw
   - "Restart PicoClaw" button/menu item kills and re-launches the process

## Config file location
- Path: `files/picoclaw/config.json` (same dir as binary)
- Managed by `ProviderConfigManager.kt` — reads/writes the JSON

## Files to create/modify
- NEW: `activity_providers.xml` — list layout
- NEW: `dialog_provider.xml` — add/edit dialog layout  
- NEW: `ProviderListActivity.kt` — manages list + dialogs
- NEW: `model/ModelProvider.kt` — data class for model_list entries
- NEW: `config/ProviderConfigManager.kt` — read/write config.json
- MODIFY: `MainActivity.kt` — add "Providers" button to Actions card
- MODIFY: `AndroidManifest.xml` — declare new activity
- MODIFY: `strings.xml` — provider-related strings
