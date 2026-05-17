# String Resources Needed for Dashboard Card Design

This document lists all string resources required for the new `activity_main_dashboard.xml` layout.

## Existing Strings (Keep / Repurpose)

These strings already exist in `app/src/main/res/values/strings.xml` and can be reused:

| String Name | Current Value | Usage in New Layout |
|-------------|---------------|---------------------|
| `app_name` | ClawDroid | Toolbar title |
| `btn_mission_control` | Mission Control | Controls card button |
| `btn_view_logs` | View Logs | Controls card button |
| `btn_restart` | Restart PicoClaw | Controls card button |

## New Strings Required

Add these to `app/src/main/res/values/strings.xml`:

```xml
<!-- App Bar -->
<string name="app_subtitle">Your on-device AI assistant</string>

<!-- Menu -->
<string name="menu_help">Help</string>
<string name="menu_about">About</string>

<!-- Status Card -->
<string name="card_status_title">System Status</string>
<string name="label_bootstrap">Environment Setup</string>
<string name="label_picoclaw">PicoClaw</string>
<string name="label_server">Server</string>

<!-- Bootstrap Status Chips -->
<string name="status_bootstrap_pending">Pending</string>
<string name="status_bootstrap_checking">Checking…</string>
<string name="status_bootstrap_extracting">Extracting…</string>
<string name="status_bootstrap_ready">Ready</string>
<string name="status_bootstrap_error">Error</string>

<!-- PicoClaw Status Chips -->
<string name="status_picoclaw_stopped">Stopped</string>
<string name="status_picoclaw_running">Running</string>
<string name="status_picoclaw_starting">Starting…</string>
<string name="status_picoclaw_stopping">Stopping…</string>

<!-- Server Status Chips -->
<string name="status_server_offline">Offline</string>
<string name="status_server_online">Online</string>
<string name="status_server_checking">Checking…</string>
<string name="server_status_default">Not connected</string>

<!-- Info Card -->
<string name="card_info_title">What is PicoClaw?</string>
<string name="info_picoclaw_description">PicoClaw is a lightweight AI assistant that runs entirely on your device. It uses a tiny language model to answer questions, write code, and help with tasks without sending data to the cloud.</string>
<string name="btn_learn_more">Learn more</string>

<!-- Controls Card -->
<string name="card_controls_title">Actions</string>
<string name="btn_settings">Settings</string>
<string name="btn_retry">Retry</string>

<!-- FAB Accessibility -->
<string name="fab_start_desc">Start PicoClaw</string>
<string name="fab_stop_desc">Stop PicoClaw</string>

<!-- Icon Content Descriptions (accessibility) -->
<string name="icon_bootstrap_desc">Environment setup icon</string>
<string name="icon_picoclaw_desc">PicoClaw icon</string>
<string name="icon_server_desc">Server icon</string>
<string name="icon_error_desc">Error indicator</string>

<!-- Inline Messages -->
<string name="error_bootstrap_failed">Setup failed. Tap Retry to try again.</string>
<string name="error_picoclaw_not_ready">Wait for environment setup to complete before starting PicoClaw.</string>
<string name="error_mission_control_offline">Mission Control is unavailable while the server is offline. Start PicoClaw first.</string>
<string name="error_logs_empty">No logs available yet. Start PicoClaw to generate logs.</string>

<!-- Dialog / Onboarding (future use) -->
<string name="dialog_welcome_title">Welcome to ClawDroid</string>
<string name="dialog_welcome_message">ClawDroid runs PicoClaw — a private AI assistant that lives on your phone. No internet required after setup.</string>
<string name="dialog_welcome_button">Get Started</string>
```

## Renamed / Removed Strings

These strings from the old layout can be **removed** or **deprecated** once the migration is complete:

| Old String | Reason |
|------------|--------|
| `welcome_message` | Replaced by app bar subtitle + info card |
| `bootstrap_initializing` | Replaced by chip text + user-friendly label |
| `bootstrap_checking` | Replaced by `status_bootstrap_checking` |
| `bootstrap_extracting` | Replaced by `status_bootstrap_extracting` |
| `bootstrap_ready` | Replaced by `status_bootstrap_ready` |
| `bootstrap_error` | Replaced by inline error card with `btn_retry` |
| `picoclaw_status_stopped` | Replaced by `status_picoclaw_stopped` chip |
| `picoclaw_status_running` | Replaced by `status_picoclaw_running` chip |
| `picoclaw_status_starting` | Replaced by `status_picoclaw_starting` chip |
| `picoclaw_status_stopping` | Replaced by `status_picoclaw_stopping` chip |
| `btn_start` | Replaced by FAB |
| `btn_stop` | Replaced by FAB |
| `server_status` | Replaced by `server_info_text` + chip |
| `mission_control_hint` | No longer needed (external browser chooser removed) |

## Migration Strategy

1. **Phase 1**: Add all new strings without removing old ones (backward compatibility)
2. **Phase 2**: Update `MainActivity.kt` to reference new string IDs
3. **Phase 3**: Remove deprecated strings after full QA pass
