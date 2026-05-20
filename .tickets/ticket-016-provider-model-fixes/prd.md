# Ticket: P0 — Fix Provider/Model Display, Default Providers, and Dev API Key Bundling

## Problem Statement

Three interrelated P0 issues make the app unusable out of the box in development mode:

### 1. Provider Dropdown Shows Model Names Instead of Provider Names
The PicoClaw `config.json` has a `model_list` where each entry contains a `model_name` (e.g. `"glm-4.7"`) and a `provider` field (e.g. `"zhipu"`). Our `ModelProvider.fromJson()` reads `model_name` into `modelName` and ignores the `provider` field entirely. This causes the provider dropdown in `AgentChatActivity` to display model names like "glm-4.7", "openrouter-auto", "gpt-5.4" as if they were provider names. The `ProviderListActivity` shows the same issue — each model entry appears as a separate "provider" card.

### 2. No Default OpenRouter Provider with Working API Key
The `.env` file contains `OPENROUTER_API_KEY` but it's never injected into the Android app. `ProviderConfigManager.ensureConfigExists()` creates an empty `model_list`, so no providers exist on first launch. Even when the PicoClaw binary generates its own config with 30 model entries, none have `api_key` set — every provider shows "No Key" and all API calls fail with HTTP 401.

### 3. Provider Edit Presets Have Confusing Names
The preset list in `ProviderEditDialog` uses model identifiers as display names:
- `"gpt-5.4"` instead of `"OpenAI"`
- `"claude-sonnet-4.6"` instead of `"Anthropic"`
- etc.

This misleads users about what they're configuring.

## Acceptance Criteria

- [x] `ModelProvider` data class includes a `provider` field parsed from JSON's `provider` field
- [x] Provider dropdown in chat groups models by `provider` field, showing unique provider names
- [x] OpenRouter provider has a pre-configured API key from `BuildConfig.OPENROUTER_API_KEY` (sourced from `.env`) in debug builds
- [x] `ensureConfigExists()` seeds the OpenRouter provider with dev API key on first launch
- [x] Model picker shows models belonging to the selected provider
- [x] `ProviderEditDialog` presets use proper provider names (OpenAI, Anthropic, etc.) instead of model names
- [x] `make quality-check` passes
- [x] No regressions in existing tests

## Tasks

### Task 1: Fix ModelProvider data class to include provider field

- [x] Subtask 1.1: Update `ModelProvider` data class
  - **Objective**: Add `provider` field (string, default empty) for the provider slug
  - **Test**: `ModelProvider.fromJson()` reads `provider` field from JSON; `toJson()` writes it
  - **Depends on**: None

- [x] Subtask 1.2: Update `AgentChatActivity` to group models by provider
  - **Objective**: Instead of showing each model as a separate "provider" entry, group by the `provider` field and show unique provider slugs in the dropdown. When a provider is selected, show its models in the `ModelPickerBottomSheet`.
  - **Test**: Provider dropdown shows unique provider names; model picker shows models for selected provider
  - **Depends on**: Subtask 1.1

### Task 2: Inject OpenRouter API key from .env for debug builds

- [x] Subtask 2.1: Add `OPENROUTER_API_KEY` to `BuildConfig` in `app/build.gradle.kts`
  - **Objective**: Read the `.env` file and inject `OPENROUTER_API_KEY` as `BuildConfig.OPENROUTER_API_KEY` for debug builds only
  - **Test**: `BuildConfig.OPENROUTER_API_KEY` is non-empty in debug APK (check via log output)
  - **Depends on**: None

- [x] Subtask 2.2: Seed default providers on first launch
  - **Objective**: Modify `ProviderConfigManager.ensureConfigExists()` to seed the OpenRouter provider with the dev API key from `BuildConfig.OPENROUTER_API_KEY` when creating the initial config
  - **Test**: First launch has an OpenRouter provider with "Has Key" status
  - **Depends on**: Subtask 2.1, Subtask 1.1

### Task 3: Fix ProviderEditDialog preset names

- [x] Subtask 3.1: Update PREDEFINED names and `PREDEFINED_NAMES` array
  - **Objective**: Change preset first elements from model-like names to actual provider names (e.g., "OpenAI" instead of "gpt-5.4", "Anthropic" instead of "claude-sonnet-4.6")
  - **Test**: Preset dropdown shows expected provider names
  - **Depends on**: None

### Task 4: Run quality check and tests

- [x] Subtask 4.1: Run lint and unit tests
  - **Objective**: `make lint` and `make test-unit-debug` pass
  - **Depends on**: All above tasks

- [x] Subtask 4.2: Build and verify on emulator
  - **Objective**: `make build-debug` succeeds; app launches showing correct provider names in dropdown
  - **Depends on**: Subtask 4.1
