---
name: android-emulator-control
description: Controls Android emulators via ADB — keyboard input, touch/mouse events, gestures (tap, swipe, pinch, long-press), text entry, orientation, location, calls, SMS, network conditioning, battery/sensor simulation, screen capture, and monkey testing.
allowed-tools: Bash(adb,*)
---

# Android Emulator Control

Control Android emulators (or connected devices) using keyboard, touch, mouse, and gesture actions through ADB.

## When to Use

- Automating UI interactions on an emulator (taps, swipes, text entry)
- Simulating hardware keys (back, home, volume, camera)
- Reproducing complex gesture sequences (pinch-to-zoom, long-press, drag-and-drop)
- Injecting fake calls, SMS, GPS locations, or sensor data for testing
- Controlling emulator state (orientation, battery, network conditions)
- Running randomized monkey tests for stress/fuzz testing
- Taking screenshots or screen recordings of emulator sessions

## Prerequisites

- ADB connected to a running emulator (`adb devices` shows the device)
- Android SDK platform-tools in PATH

## Configuration

| Env Variable | Purpose |
|-------------|---------|
| `ANDROID_SERIAL` | Target specific device (e.g. `emulator-5554`) |
| `ADB_ECHO=1` | Print every ADB command before running |
| `ADB_INPUT_SOURCE` | Input source for touch events: `touchscreen` (default), `mouse`, `stylus`, `trackball`, `joystick` |

```
adb devices
# List of devices attached
# emulator-5554   device
```

## Quick Start

```
# Debug: see every ADB command the skill runs
export ADB_ECHO=1

# Tap the center of the screen
bash .skills/android-emulator-control/scripts/emulator-control.sh tap 540 960

# Type text (supports spaces and special characters)
bash .skills/android-emulator-control/scripts/emulator-control.sh type "hello world"

# Swipe from left edge to right (e.g. notification shade)
bash .skills/android-emulator-control/scripts/emulator-control.sh swipe 100 500 900 500 200

# Press hardware Back key
bash .skills/android-emulator-control/scripts/emulator-control.sh key back

# Take a screenshot
bash .skills/android-emulator-control/scripts/emulator-control.sh screenshot screen.png
```

## Command Reference

### Device Selection

By default, all commands target the first device (`adb shell ...`). To target a specific emulator:

```
export ANDROID_SERIAL=emulator-5554
```

Or use `-s` with the script:

```
bash .skills/android-emulator-control/scripts/emulator-control.sh -s emulator-5554 tap 100 200
```

### Keyboard Input

| Action | Command | Description |
|--------|---------|-------------|
| Type text | `type "hello world"` | Types text (handles spaces, quotes, special chars) |
| Press key | `key back` | Press a named key |
| Long-press key | `key --longpress power` | Long-press a hardware key (e.g., power menu) |
| Key event code | `keyevent 4` | Press a key by Android keycode integer |
| Long-press keycode | `keyevent --longpress 26` | Long-press by keycode |
| Press Home | `key home` | Go to home screen |
| Press Back | `key back` | Navigate back |
| Press Recent Apps | `key recent` | Open recent apps overview |
| Press Menu | `key menu` | Open context menu |
| Press Power | `key power` | Toggle screen on/off |
| Press Volume Up | `key volume_up` | Increase volume |
| Press Volume Down | `key volume_down` | Decrease volume |
| Press Camera | `key camera` | Activate camera |
| Press Enter | `key enter` | Confirm/select |
| Press Delete | `key delete` | Backspace/delete |
| Press DPAD keys | `key dpad_up` / `dpad_down` / `dpad_left` / `dpad_right` / `dpad_center` | Directional pad navigation |
| Press Media keys | `key play` / `pause` / `stop` / `next` / `previous` | Media playback control |
| Press Call keys | `key call` / `endcall` | Accept / end a call |

**Supported key names:** `home`, `back`, `recent`, `menu`, `power`, `volume_up`, `volume_down`, `camera`, `enter`, `delete`, `dpad_up`, `dpad_down`, `dpad_left`, `dpad_right`, `dpad_center`, `call`, `endcall`, `play`, `pause`, `stop`, `next`, `previous`, `search`, `settings`, `tab`, `space`, `escape`, `cap_lock`, `page_up`, `page_down`, `clear`, `cut`, `copy`, `paste`, `num_0`–`num_9`, `a`–`z`, `0`–`9`, `comma`, `period`, `slash`, `backtick`, `minus`, `equals`, `left_bracket`, `right_bracket`, `semicolon`, `apostrophe`, `grave`.

If a name isn't in the list, pass the raw keycode integer via `keyevent N`.

### Touch / Mouse

| Action | Command | Description |
|--------|---------|-------------|
| Tap | `tap x y` | Single tap at coordinates |
| Tap (percentage) | `tap_percent 50% 50%` | Tap at percentage of screen (resolution-independent) |
| Double-tap | `doubletap x y` | Two quick taps at the same point |
| Swipe | `swipe x1 y1 x2 y2 [ms]` | Swipe from (x1,y1) to (x2,y2) over `ms` milliseconds (default 300) |
| Swipe (percentage) | `swipe_percent 10% 50% 90% 50% [ms]` | Swipe using percentage coordinates |
| Drag and drop | `drag x1 y1 x2 y2 [ms]` | Alias for swipe with longer default duration (500ms) |
| Long press | `longpress x y [ms]` | Tap-and-hold at coordinates (default 1500ms) |
| Scroll down | `scroll_down [x y steps]` | Vertical swipe downward at center (or at x,y) |
| Scroll up | `scroll_up [x y steps]` | Vertical swipe upward at center (or at x,y) |
| Scroll left | `scroll_left [x y steps]` | Horizontal swipe leftward |
| Scroll right | `scroll_right [x y steps]` | Horizontal swipe rightward |

### Gestures

| Action | Command | Description |
|--------|---------|-------------|
| Pinch in (zoom out) | `pinchin x y [distance]` | Two-finger pinch-in centered at (x,y), default distance 200px |
| Pinch out (zoom in) | `pinchout x y [distance]` | Two-finger pinch-out centered at (x,y), default distance 200px |
| Unlock swipe | `unlock` | Swipe up from bottom-center to unlock |
| Pull notification shade | `notifications` | Swipe down from top of screen |
| Open quick settings | `quicksettings` | Two-finger swipe down from top |
| Swipe to dismiss | `dismiss` | Swipe right-to-left to dismiss (e.g. notification, dialog) |

### Text & Clipboard

| Action | Command | Description |
|--------|---------|-------------|
| Type text | `type "text with spaces"` | Input text into focused field |
| Type Unicode | `type_unicode "hello 😊 café"` | Input text with emoji and international characters (ADB 1.0.41+) |
| Type with delay | `type_slow "text" [delay_ms]` | Types text character by character with delay (default 50ms) |
| Clear text field | `clear_field` | Select-all then delete in focused text field |
| Copy to clipboard | `clipboard_set "text"` | Copy text to device clipboard (uses `cmd clipboard` on Android 10+) |
| Get clipboard | `clipboard_get` | Read current clipboard content |
| Paste clipboard | `clipboard_paste` | Inject clipboard content into focused field |

### Screen Capture & Recording

| Action | Command | Description |
|--------|---------|-------------|
| Screenshot | `screenshot [file.png]` | Capture screen to PNG file (saved to `.skills/android-emulator-control/.tmp/`) |
| Screenshot to host | `screenshot_pull [file.png]` | Capture and pull to `.skills/android-emulator-control/.tmp/` |
| Start recording | `screenrecord_start [file.mp4]` | Start screen recording (default: `/sdcard/screenrecord.mp4`) |
| Stop recording | `screenrecord_stop` | Stop recording and pull to `.skills/android-emulator-control/.tmp/` |
| Screenshot via UiAutomator | `screenshot_uia [file.png]` | Take screenshot using UiAutomator dump (saved to `.skills/android-emulator-control/.tmp/`) |

### Emulator State

| Action | Command | Description |
|--------|---------|-------------|
| Set orientation | `orientation portrait` / `landscape` / `reverse_landscape` / `reverse_portrait` / `sensor` | Change device orientation |
| Fold/unfold (foldable) | `fold` / `unfold` | Toggle foldable screen state |
| Wake device | `wake` | Wake up screen (press power if off) |
| Wake + unlock | `wake_unlock` | Wake and swipe-to-unlock in one step |
| Go to sleep | `sleep` | Put device to sleep |
| Is awake | `is_awake` | Returns true/false if screen is on |
| Unlock (swipe) | `unlock` | Swipe up to unlock (assumes no PIN) |
| Open app | `open_app com.example.package` | Launch app by package name |
| Open app with activity | `open_app com.example.package/.MainActivity` | Launch specific activity |
| Kill app | `kill_app com.example.package` | Force-stop an app |
| Clean app data | `clear_app com.example.package` | Clear app data (resets to fresh install) |
| Reboot | `reboot` | Reboot the emulator |
| Animations on | `animations_on` | Enable window/transition/animator animations |
| Animations off | `animations_off` | Disable all animations (faster testing) |
| Show animations | `animations_get` | Show current animation scale values |
| Immersive mode | `immersive_mode full` / `status` / `navigation` / `off` | Set global immersive mode policy |
| Expand status bar | `statusbar_expand` | Expand notification panel |
| Collapse status bar | `statusbar_collapse` | Collapse notification panel |

### Location & Sensors

| Action | Command | Description |
|--------|---------|-------------|
| Set GPS | `gps lat lng` | Set GPS location (e.g. `gps 37.7749 -122.4194`) |
| Set location provider | `location network` / `gps` / `passive` | Switch location provider |
| Enable/disable GPS | `gps_on` / `gps_off` | Toggle GPS location service |
| Accelerate to location | `gps_route lat1,lng1 lat2,lng2 ...` | Simulate movement along a route (calls gps sequentially with delay) |
| Rotate device | `rotate degrees` | Set device rotation in degrees (0, 90, 180, 270) using sensor |

### Telephony (Calls & SMS)

| Action | Command | Description |
|--------|---------|-------------|
| Simulate incoming call | `call +15551234567` | Simulate incoming phone call |
| Accept call | `key call` | Answer the simulated call |
| End call | `key endcall` | Hang up the call |
| Simulate SMS | `sms +15551234567 "message text"` | Simulate receiving an SMS |
| Set cellular network | `network_type lte` / `hspa` / `edge` / `gprs` / `gsm` | Set mobile network type |
| Toggle airplane mode | `airplane_on` / `airplane_off` | Enable/disable airplane mode |
| Toggle WiFi | `wifi_on` / `wifi_off` | Enable/disable WiFi |
| Toggle mobile data | `mobile_data_on` / `mobile_data_off` | Enable/disable mobile data |

### Battery & Power

| Action | Command | Description |
|--------|---------|-------------|
| Set battery level | `battery_level 85` | Set battery percentage (0–100) |
| Set battery status | `battery_status charging` / `discharging` / `not_charging` / `full` | Override battery status |
| Set battery AC status | `battery_ac true` / `false` | Set AC charging state |
| Set battery present | `battery_present true` / `false` | Set whether battery is present |
| Set battery health | `battery_health good` / `overheat` / `dead` / `over_voltage` / `failure` | Override battery health |
| Reset battery | `battery_reset` | Reset battery to real sensor values |

### Network Conditioning

| Action | Command | Description |
|--------|---------|-------------|
| Set network speed | `network_speed edge` / `gprs` / `umts` / `hspa` / `lte` / `full` | Emulate network speed tier |
| Set network latency | `network_delay gprs` / `edge` / `umts` / `lte` / `none` | Emulate network latency tier |
| Set network (raw) | `network_speed_raw 100 100 100 100` | Speed: upload, download, latency, loss% |
| Set network (raw latency) | `network_delay_raw 300 50` | Delay: min and max latency in ms |
| Disconnect network | `network_disconnect` | Fully disconnect cellular |
| Reconnect network | `network_reconnect` | Reconnect cellular |

### Monkey Testing

| Action | Command | Description |
|--------|---------|-------------|
| Run monkey | `monkey [count] [package]` | Send random events (default 500, all packages) |
| Monkey with seed | `monkey_seed count seed [package]` | Reproducible monkey test with fixed seed |
| Monkey touch only | `monkey_touch count [package]` | Touch events only (no system keys) |
| Monkey app only | `monkey_app package [count]` | Constrain monkey to a single app |
| Monkey throttle | `monkey_throttle count ms [package]` | Add delay between events |

### Screenshot with Content Detection

| Action | Command | Description |
|--------|---------|-------------|
| Dump UI | `uia_dump [file.xml]` | Dump current UI hierarchy as XML (saved to `.skills/android-emulator-control/.tmp/`) |
| Dump and pull | `uia_dump_pull [file.xml]` | Dump UI hierarchy and pull to `.skills/android-emulator-control/.tmp/` |
| Find text | `uia_find "text"` | Search dumped UI for elements containing text |
| Find by ID | `uia_find_id "resource_id"` | Search dumped UI for elements by resource ID |
| Dump clickable | `uia_clickable` | List all clickable elements from UI dump |

### Helper Utilities

| Action | Command | Description |
|--------|---------|-------------|
| Show device info | `info` | Display device model, Android version, density, resolution |
| Show screen size | `size` | Get emulator resolution |
| Show density | `density` | Get screen density in dpi |
| List all keys | `list_keys` | Print all supported key names and their keycodes |
| Wait for boot | `wait_boot` | Wait until `sys.boot_completed=1` |
| List installed apps | `list_apps [filter]` | List packages (e.g. `list_apps clawdroid` to filter) |
| Start activity | `start_activity com.example/.ActivityName` | Start specific Android activity |
| Send broadcast | `broadcast action.name --extra key value` | Send arbitrary Android broadcast |
| Set system property | `setprop name value` | Set an Android system property |
| Get system property | `getprop name` | Read an Android system property |
| Run raw command | `raw "shell command here"` | Execute arbitrary ADB shell command |
| Open URL | `open_url https://example.com` | Open URL in browser |
| Grant permission | `grant_permission com.example.pkg android.permission.CAMERA` | Grant a runtime permission |
| Revoke permission | `revoke_permission com.example.pkg android.permission.CAMERA` | Revoke a runtime permission |
| Enable accessibility | `accessibility_on` | Enable accessibility services for testing |
| Read logcat | `logcat [filter]` | Read logcat (e.g. `logcat clawdroid:*` for app logs) |
| Clear logcat | `logcat_clear` | Clear all logcat buffers |

## ADB Keyevent Reference

| Key | Keycode (int) | Name |
|-----|--------------|------|
| Home | 3 | `home` |
| Back | 4 | `back` |
| Call | 5 | `call` |
| End Call | 6 | `endcall` |
| Volume Up | 24 | `volume_up` |
| Volume Down | 25 | `volume_down` |
| Power | 26 | `power` |
| Camera | 27 | `camera` |
| Clear | 28 | `clear` |
| DPAD Up | 19 | `dpad_up` |
| DPAD Down | 20 | `dpad_down` |
| DPAD Left | 21 | `dpad_left` |
| DPAD Right | 22 | `dpad_right` |
| DPAD Center | 23 | `dpad_center` |
| Enter | 66 | `enter` |
| Delete (Backspace) | 67 | `delete` |
| Menu | 82 | `menu` |
| Search | 84 | `search` |
| Recent Apps | 187 | `recent` |
| App Switch | 187 | `app_switch` |
| Media Play/Pause | 85 | `play` |
| Media Stop | 86 | `stop` |
| Media Next | 87 | `next` |
| Media Previous | 88 | `previous` |
| Volume Mute | 164 | `volume_mute` |
| Settings | 176 | `settings` |
| Space | 62 | `space` |
| Tab | 61 | `tab` |
| Escape | 111 | `escape` |
| Caps Lock | 115 | `cap_lock` |
| Page Up | 92 | `page_up` |
| Page Down | 93 | `page_down` |
| Cut | 277 | `cut` |
| Copy | 278 | `copy` |
| Paste | 279 | `paste` |

## Examples

### E2E Test Workflow

```bash
export ANDROID_SERIAL=emulator-5554
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

bash $CTRL wait_boot
bash $CTRL wake
bash $CTRL unlock
bash $CTRL open_app com.example.clawdroid

# Wait for app to load, then interact
sleep 3
bash $CTRL tap 540 300       # Tap first element
bash $CTRL type "hello"
bash $CTRL key enter

# Navigate back and check orientation
bash $CTRL orientation landscape
bash $CTRL screenshot e2e-test.png
bash $CTRL key back
```

### Filling a Login Form

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Focus the email field and type
bash $CTRL tap 200 400
bash $CTRL type "user@example.com"

# Focus the password field and type
bash $CTRL tap 200 500
bash $CTRL type "securepass123"

# Tap the login button
bash $CTRL tap 300 600

# Wait for result
sleep 2
bash $CTRL screenshot login-result.png
```

### Testing Pinch-to-Zoom in a Map

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

bash $CTRL open_app com.google.android.apps.maps
sleep 5
bash $CTRL pinchout 540 960 300   # Zoom in (center, 300px span)
sleep 1
bash $CTRL pinchout 540 960 300   # Zoom in more
sleep 1
bash $CTRL pinchin 540 960 300    # Zoom out
```

### Simulating an Incoming Call During App Usage

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

bash $CTRL call +14155551234
sleep 2
bash $CTRL screenshot incoming-call.png
bash $CTRL key endcall
```

### GPS Route Simulation

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Simulate driving along a route
bash $CTRL gps 37.7749 -122.4194
sleep 5
bash $CTRL gps 37.7849 -122.4094
sleep 5
bash $CTRL gps 37.7949 -122.3994
```

### Monkey Stress Test

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# 2000 random touch events in the ClawDroid app
bash $CTRL monkey_app com.example.clawdroid 2000

# 1000 reproducible events with seed
bash $CTRL monkey_seed 1000 42 com.example.clawdroid
```

### Using Percentage Coordinates (Resolution-Independent)

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Tap the center of any screen, regardless of resolution
bash $CTRL tap_percent 50% 50%

# Swipe from left edge to center (screen-size independent)
bash $CTRL swipe_percent 0% 50% 50% 50% 200

# Works on phones, tablets, and foldables without modification
```

### Unicode Text Input (Emoji & International)

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Emoji and accented characters
bash $CTRL type_unicode "Hello World 🌍 Café résumé ñoño 😊"

# Requires ADB 1.0.41+. Falls back to ASCII type if not supported.
```

### Changing Input Source (Touch vs Mouse vs Stylus)

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Simulate mouse cursor events instead of touch
ADB_INPUT_SOURCE=mouse bash $CTRL tap 540 960

# Simulate stylus input  
ADB_INPUT_SOURCE=stylus bash $CTRL swipe 100 500 800 500 200
```

### Faster Testing with Animations Off

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Disable all animations for faster test execution
bash $CTRL animations_off

# Run your tests here...

# Re-enable when done
bash $CTRL animations_on
```

### Immersive Mode and Status Bar

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Full immersive mode (hides status + nav bars)
bash $CTRL immersive_mode full

# Status bar only
bash $CTRL immersive_mode status

# Navigation bar only
bash $CTRL immersive_mode navigation

# Restore bars
bash $CTRL immersive_mode off

# Programmatic status bar
bash $CTRL statusbar_expand
bash $CTRL statusbar_collapse
```

### Long-Press Hardware Keys

```bash
CTRL=.skills/android-emulator-control/scripts/emulator-control.sh

# Power menu (long-press power button)
bash $CTRL key --longpress power

# Recent apps long-press (split screen on some devices)
bash $CTRL key --longpress recent

# Using raw keycode
bash $CTRL keyevent --longpress 26
```
