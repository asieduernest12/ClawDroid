# String Resources Required for Control Center Design

These string resources must be added to `app/src/main/res/values/strings.xml`.

## New Strings

```xml
<!-- Header -->
<string name="header_subtitle">PicoClaw Control Center</string>

<!-- Accessibility / Content Descriptions -->
<string name="desc_pull_to_refresh">Pull down to refresh system status</string>
<string name="desc_bootstrap_icon">Bootstrap setup icon</string>
<string name="desc_server_icon">Server status icon</string>

<!-- Tile Labels -->
<string name="status_label_picoclaw">PicoClaw</string>
<string name="bootstrap_label">Environment</string>
<string name="server_label">Server</string>

<!-- Short action labels for circular buttons -->
<string name="btn_start_short">Start</string>
<string name="btn_stop_short">Stop</string>
<string name="btn_restart_short">Restart</string>

<!-- Settings -->
<string name="btn_settings">Settings</string>

<!-- Server Health States -->
<string name="server_health_unknown">Unknown</string>
<string name="server_health_healthy">Healthy</string>
<string name="server_health_unhealthy">Unhealthy</string>

<!-- Bottom Sheet -->
<string name="logs_title">Live Logs</string>
<string name="logs_empty">No logs available yet.</string>
<string name="logs_close">Close</string>
<string name="logs_drag_handle">Drag to expand or collapse logs</string>

<!-- Web Dashboard -->
<string name="mission_control_title">Mission Control</string>
<string name="mission_control_loading">Loading dashboard…</string>
<string name="mission_control_error">Failed to load dashboard</string>

<!-- Controls Section Label -->
<string name="controls_label">Controls</string>
```

## Existing Strings to Keep (already in `strings.xml`)

```xml
<string name="app_name">ClawDroid</string>
<string name="welcome_message">Welcome to ClawDroid</string>

<string name="bootstrap_initializing">Initializing Termux environment…</string>
<string name="bootstrap_checking">Setting up Termux environment…</string>
<string name="bootstrap_extracting">Extracting Termux environment…</string>
<string name="bootstrap_ready">Termux environment ready</string>
<string name="bootstrap_error">Bootstrap error: %s</string>

<string name="picoclaw_status_stopped">PicoClaw: Stopped</string>
<string name="picoclaw_status_running">PicoClaw: Running</string>
<string name="picoclaw_status_starting">PicoClaw: Starting…</string>
<string name="picoclaw_status_stopping">PicoClaw: Stopping…</string>

<string name="btn_start">Start PicoClaw</string>
<string name="btn_stop">Stop PicoClaw</string>
<string name="btn_restart">Restart PicoClaw</string>
<string name="btn_mission_control">Mission Control</string>
<string name="btn_view_logs">View Logs</string>

<string name="server_status">Server: http://localhost:%1$d</string>
<string name="mission_control_hint">Open in browser</string>
```

## Notes
- The original `btn_start`, `btn_stop`, etc. strings are kept as `contentDescription` values for the circular buttons (for TalkBack), while `btn_start_short` is used as the visible text label underneath.
- `welcome_message` can be deprecated in favor of the new header title/subtitle structure, but should be kept for backward compatibility until fully migrated.
