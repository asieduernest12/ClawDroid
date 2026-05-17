#!/bin/bash
# emulator-control.sh — Unified ADB Android Emulator Controller
#
# Usage: bash emulator-control.sh [options] <action> [args...]
#
# Examples:
#   bash emulator-control.sh tap 540 960
#   bash emulator-control.sh type "hello world"
#   bash emulator-control.sh key back
#   bash emulator-control.sh swipe 100 200 800 200 500
#   bash emulator-control.sh -s emulator-5554 screenshot screen.png
#
# All actions from the SKILL.md are supported.

set -euo pipefail

# ── Config ──────────────────────────────────────────────────────────────
ADB="${ADB:-adb}"
ADB_ECHO="${ADB_ECHO:-}"
ADB_SERIAL="${ANDROID_SERIAL:-}"
ADB_INPUT_SOURCE="${ADB_INPUT_SOURCE:-}" # e.g., touchscreen, mouse, stylus, trackball

# Resolve skill root and tmp directory for screenshots / dumps
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
TMP_DIR="$SKILL_DIR/.tmp"
mkdir -p "$TMP_DIR"

# ── Helpers ─────────────────────────────────────────────────────────────

_input_source_flag() {
    if [ -n "$ADB_INPUT_SOURCE" ]; then
        echo "--source $ADB_INPUT_SOURCE"
    fi
}

# Convert percentage (e.g. "50%") to absolute pixel, fallback to raw number
_pct_or_px() {
    local val="$1" max="$2"
    if [[ "$val" == *% ]]; then
        local pct="${val%\%}"
        echo $(( max * pct / 100 ))
    else
        echo "$val"
    fi
}

_get_screen_width() {
    _adb wm size 2>/dev/null | sed -n 's/.* \([0-9][0-9]*\)x[0-9][0-9]*.*/\1/p' || echo 1080
}

_get_screen_height() {
    _adb wm size 2>/dev/null | sed -n 's/.* [0-9][0-9]*x\([0-9][0-9]*\).*/\1/p' || echo 1920
}

_get_wakefulness() {
    _adb dumpsys power 2>/dev/null | sed -n 's/.*mWakefulness=\([a-zA-Z0-9_][a-zA-Z0-9_]*\).*/\1/p'
}

_adb() {
    local cmd
    if [ -n "$ADB_SERIAL" ]; then
        cmd=("$ADB" -s "$ADB_SERIAL" shell "$@")
    else
        cmd=("$ADB" shell "$@")
    fi
    [ -n "$ADB_ECHO" ] && echo "  + ${cmd[*]}" >&2
    "${cmd[@]}"
}

_adb_host() {
    local cmd
    if [ -n "$ADB_SERIAL" ]; then
        cmd=("$ADB" -s "$ADB_SERIAL" "$@")
    else
        cmd=("$ADB" "$@")
    fi
    [ -n "$ADB_ECHO" ] && echo "  + ${cmd[*]}" >&2
    "${cmd[@]}"
}

_die() {
    echo "Error: $*" >&2
    exit 1
}

_require_args() {
    local min="$1" name="$2"; shift 2
    [ $# -lt "$min" ] && _die "${name} requires at least $min argument(s)"
}

_timestamp() {
    date +%Y%m%d-%H%M%S
}

_keycode() {
    local name="$1"
    case "$(echo "$name" | tr '[:upper:]' '[:lower:]')" in
        home)           echo 3 ;;
        back)           echo 4 ;;
        call)           echo 5 ;;
        endcall)        echo 6 ;;
        0)              echo 7 ;;
        1)              echo 8 ;;
        2)              echo 9 ;;
        3)              echo 10 ;;
        4)              echo 11 ;;
        5)              echo 12 ;;
        6)              echo 13 ;;
        7)              echo 14 ;;
        8)              echo 15 ;;
        9)              echo 16 ;;
        star)           echo 17 ;;
        pound)          echo 18 ;;
        dpad_up)        echo 19 ;;
        dpad_down)      echo 20 ;;
        dpad_left)      echo 21 ;;
        dpad_right)     echo 22 ;;
        dpad_center)    echo 23 ;;
        volume_up)      echo 24 ;;
        volume_down)    echo 25 ;;
        power)          echo 26 ;;
        camera)         echo 27 ;;
        clear)          echo 28 ;;
        a)              echo 29 ;;
        b)              echo 30 ;;
        c)              echo 31 ;;
        d)              echo 32 ;;
        e)              echo 33 ;;
        f)              echo 34 ;;
        g)              echo 35 ;;
        h)              echo 36 ;;
        i)              echo 37 ;;
        j)              echo 38 ;;
        k)              echo 39 ;;
        l)              echo 40 ;;
        m)              echo 41 ;;
        n)              echo 42 ;;
        o)              echo 43 ;;
        p)              echo 44 ;;
        q)              echo 45 ;;
        r)              echo 46 ;;
        s)              echo 47 ;;
        t)              echo 48 ;;
        u)              echo 49 ;;
        v)              echo 50 ;;
        w)              echo 51 ;;
        x)              echo 52 ;;
        y)              echo 53 ;;
        z)              echo 54 ;;
        comma)          echo 55 ;;
        period)         echo 56 ;;
        alt_left)       echo 57 ;;
        alt_right)      echo 58 ;;
        shift_left)     echo 59 ;;
        shift_right)    echo 60 ;;
        tab)            echo 61 ;;
        space)          echo 62 ;;
        sym)            echo 63 ;;
        explorer)       echo 64 ;;
        envelope)       echo 65 ;;
        enter)          echo 66 ;;
        del)            echo 67 ;;
        grave)          echo 68 ;;
        minus)          echo 69 ;;
        equals)         echo 70 ;;
        left_bracket)   echo 71 ;;
        right_bracket)  echo 72 ;;
        backslash)      echo 73 ;;
        semicolon)      echo 74 ;;
        apostrophe)     echo 75 ;;
        slash)          echo 76 ;;
        at)             echo 77 ;;
        num)            echo 78 ;;
        headsethook)    echo 79 ;;
        focus)          echo 80 ;;
        plus)           echo 81 ;;
        menu)           echo 82 ;;
        notification)   echo 83 ;;
        search)         echo 84 ;;
        play)           echo 85 ;;
        stop)           echo 86 ;;
        next)           echo 87 ;;
        previous)       echo 88 ;;
        rewind)         echo 89 ;;
        forward)        echo 90 ;;
        page_up)        echo 92 ;;
        page_down)      echo 93 ;;
        escape)         echo 111 ;;
        cap_lock)       echo 115 ;;
        scroll_lock)    echo 116 ;;
        break)          echo 121 ;;
        move_home)      echo 122 ;;
        move_end)       echo 123 ;;
        insert)         echo 124 ;;
        forward_del)    echo 112 ;;
        media_play)     echo 126 ;;
        media_pause)    echo 127 ;;
        media_close)    echo 128 ;;
        media_record)   echo 130 ;;
        f1)             echo 131 ;;
        f2)             echo 132 ;;
        f3)             echo 133 ;;
        f4)             echo 134 ;;
        f5)             echo 135 ;;
        f6)             echo 136 ;;
        f7)             echo 137 ;;
        f8)             echo 138 ;;
        f9)             echo 139 ;;
        f10)            echo 140 ;;
        f11)            echo 141 ;;
        f12)            echo 142 ;;
        num_lock)       echo 143 ;;
        numpad_0)       echo 144 ;;
        numpad_1)       echo 145 ;;
        numpad_2)       echo 146 ;;
        numpad_3)       echo 147 ;;
        numpad_4)       echo 148 ;;
        numpad_5)       echo 149 ;;
        numpad_6)       echo 150 ;;
        numpad_7)       echo 151 ;;
        numpad_8)       echo 152 ;;
        numpad_9)       echo 153 ;;
        numpad_divide)  echo 154 ;;
        numpad_multiply) echo 155 ;;
        numpad_subtract) echo 156 ;;
        numpad_add)     echo 157 ;;
        numpad_dot)     echo 158 ;;
        numpad_comma)   echo 159 ;;
        numpad_enter)   echo 160 ;;
        numpad_equals)  echo 161 ;;
        volume_mute)    echo 164 ;;
        info)           echo 165 ;;
        channel_up)     echo 166 ;;
        channel_down)   echo 167 ;;
        zoom_in)        echo 168 ;;
        zoom_out)       echo 169 ;;
        window)         echo 171 ;;
        guide)          echo 172 ;;
        dvr)            echo 173 ;;
        bookmark)       echo 174 ;;
        captions)       echo 175 ;;
        settings)       echo 176 ;;
        app_switch)     echo 187 ;;
        recent)         echo 187 ;;
        language_switch) echo 204 ;;
        cut)            echo 277 ;;
        copy)           echo 278 ;;
        paste)          echo 279 ;;
        *)
            # If it's a pure number, use it directly
            if [[ "$name" =~ ^[0-9]+$ ]]; then
                echo "$name"
            else
                _die "unknown key name '$name'. Run 'list_keys' for all supported names or use 'keyevent <code>'"
            fi
            ;;
    esac
}

# ── Actions ─────────────────────────────────────────────────────────────

action_tap() {
    _require_args 2 "tap" "$@"
    local x="$1" y="$2"
    _adb input $(_input_source_flag) tap "$x" "$y"
}

action_doubletap() {
    _require_args 2 "doubletap" "$@"
    local x="$1" y="$2"
    _adb input $(_input_source_flag) tap "$x" "$y"
    _adb input $(_input_source_flag) tap "$x" "$y"
}

action_swipe() {
    _require_args 4 "swipe" "$@"
    local x1="$1" y1="$2" x2="$3" y2="$4" duration="${5:-300}"
    _adb input $(_input_source_flag) swipe "$x1" "$y1" "$x2" "$y2" "$duration"
}

action_drag() {
    _require_args 4 "drag" "$@"
    local x1="$1" y1="$2" x2="$3" y2="$4" duration="${5:-500}"
    _adb input $(_input_source_flag) draganddrop "$x1" "$y1" "$x2" "$y2" "$duration"
}

action_longpress() {
    _require_args 2 "longpress" "$@"
    local x="$1" y="$2" duration="${3:-1500}"
    _adb input $(_input_source_flag) swipe "$x" "$y" "$x" "$y" "$duration"
}

# Percentage-based actions (resolution-independent)
action_tap_percent() {
    _require_args 2 "tap_percent" "$@"
    local w h
    w=$(action_size_width)
    h=$(action_size_height)
    local x y
    x=$(_pct_or_px "$1" "$w")
    y=$(_pct_or_px "$2" "$h")
    _adb input $(_input_source_flag) tap "$x" "$y"
}

action_swipe_percent() {
    _require_args 4 "swipe_percent" "$@"
    local w h
    w=$(action_size_width)
    h=$(action_size_height)
    local x1 y1 x2 y2
    x1=$(_pct_or_px "$1" "$w")
    y1=$(_pct_or_px "$2" "$h")
    x2=$(_pct_or_px "$3" "$w")
    y2=$(_pct_or_px "$4" "$h")
    local duration="${5:-300}"
    _adb input $(_input_source_flag) swipe "$x1" "$y1" "$x2" "$y2" "$duration"
}

action_scroll_down() {
    local x="${1:-$(action_size_width)}" y="${2:-$(action_size_height)}" steps="${3:-10}"
    x=$(( x / 2 )); y=$(( y / 2 ))
    local start_y=$(( y - 200 )) end_y=$(( y + 200 ))
    [ "$start_y" -lt 0 ] && start_y=0
    _adb input swipe "$x" "$start_y" "$x" "$end_y" "$(( steps * 50 ))"
}

action_scroll_up() {
    local x="${1:-$(action_size_width)}" y="${2:-$(action_size_height)}" steps="${3:-10}"
    x=$(( x / 2 )); y=$(( y / 2 ))
    local start_y=$(( y + 200 )) end_y=$(( y - 200 ))
    [ "$end_y" -lt 0 ] && end_y=0
    _adb input swipe "$x" "$start_y" "$x" "$end_y" "$(( steps * 50 ))"
}

action_scroll_left() {
    _require_args 2 "scroll_left" "$@"
    local x="$1" y="$2" steps="${3:-10}"
    local start_x=$(( x + 200 )) end_x=$(( x - 200 ))
    [ "$end_x" -lt 0 ] && end_x=0
    _adb input swipe "$start_x" "$y" "$end_x" "$y" "$(( steps * 50 ))"
}

action_scroll_right() {
    _require_args 2 "scroll_right" "$@"
    local x="$1" y="$2" steps="${3:-10}"
    local start_x=$(( x - 200 )) end_x=$(( x + 200 ))
    [ "$start_x" -lt 0 ] && start_x=0
    _adb input swipe "$start_x" "$y" "$end_x" "$y" "$(( steps * 50 ))"
}

action_pinchin() {
    local x="${1:-$(action_center_x)}" y="${2:-$(action_center_y)}" dist="${3:-200}"
    local half=$(( dist / 2 ))
    local x1=$(( x - half )) y1="$y" x2=$(( x + half )) y2="$y"
    # Two-finger swipe: fingers move toward center
    _adb input swipe "$x1" "$y1" "$x" "$y" 300 &
    _adb input swipe "$x2" "$y2" "$x" "$y" 300 &
    wait
}

action_pinchout() {
    local x="${1:-$(action_center_x)}" y="${2:-$(action_center_y)}" dist="${3:-200}"
    local half=$(( dist / 2 ))
    local x1="$x" y1="$y" x2="$x" y2="$y"
    # Two-finger swipe: fingers move away from center
    _adb input swipe "$x1" "$y1" "$(( x - half ))" "$y" 300 &
    _adb input swipe "$x2" "$y2" "$(( x + half ))" "$y" 300 &
    wait
}

action_unlock() {
    local w
    w=$(_get_screen_width)
    local h
    h=$(_get_screen_height)
    local cx=$(( w / 2 )) cy=$(( h / 2 ))
    local start_y=$(( h * 3 / 4 )) end_y=$(( h / 4 ))
    _adb input swipe "$cx" "$start_y" "$cx" "$end_y" 300
}

action_notifications() {
    local w
    w=$(_get_screen_width)
    [ -z "$w" ] && w=1080
    local cx=$(( w / 2 ))
    _adb input swipe "$cx" 5 "$cx" 600 200
}

action_quicksettings() {
    # Two-finger swipe down
    action_notifications &
    action_notifications &
    wait
}

action_dismiss() {
    local x="${1:-$(action_center_x)}" y="${2:-$(action_center_y)}"
    _adb input swipe "$x" "$y" "$(( x - 500 ))" "$y" 200
}

action_type() {
    _require_args 1 "type" "$@"
    local text="$1"
    # Escape quotes for the remote shell
    text="${text//\'/\'\\\'\'}"
    text="${text//\"/\\\"}"
    _adb input text "$text"
}

action_type_unicode() {
    _require_args 1 "type_unicode" "$@"
    local text="$1"
    # input text-unicode supports emoji, accented chars, etc. (ADB 1.0.41+)
    text="${text//\'/\'\\\'\'}"
    text="${text//\"/\\\"}"
    _adb input text-unicode "$text" 2>/dev/null || \
    _adb input text "$text"
}

action_type_slow() {
    _require_args 1 "type_slow" "$@"
    local text="$1" delay="${2:-50}"
    for (( i=0; i<${#text}; i++ )); do
        local ch="${text:$i:1}"
        case "$ch" in
            ' ') _adb input keyevent 62 ;;
            '"') _adb input text '\\\\\"' ;;
            "'") _adb input text \\\\\' ;;
            '\') _adb input text \\\\\\\\ ;;
            *)   _adb input text "$ch" ;;
        esac
        sleep "$(bc <<< "scale=3; $delay / 1000")"
    done
}

action_clear_field() {
    # Select all + delete
    _adb input keyevent 29  # Ctrl+A equivalent via KEYCODE_A
    _adb input keyevent 67  # KEYCODE_DEL multiple times
    for _ in $(seq 1 50); do _adb input keyevent 67; done
}

action_clipboard_set() {
    _require_args 1 "clipboard_set" "$@"
    local text="$1"
    # Try modern cmd clipboard API (Android 10+), fall back to legacy methods
    text="${text//\'/\'\\\'\'}"
    _adb cmd clipboard set-text "$text" 2>/dev/null || \
    _adb am broadcast -a clipper.set --es text "$text" 2>/dev/null || \
    _adb service call clipboard 1 i32 1 s16 "$text" 2>/dev/null || \
    _adb input text "$text"
}

action_clipboard_get() {
    local result
    # Try modern cmd clipboard API (Android 10+)
    result="$(_adb cmd clipboard get-text 2>/dev/null || true)"
    if [ -n "$result" ] && [ "$result" != "null" ]; then
        echo "$result"
        return
    fi
    # Fall back to broadcast method
    result="$(_adb am broadcast -a clipper.get 2>/dev/null | sed -n 's/.*text=//p' || true)"
    if [ -n "$result" ]; then
        echo "$result"
    else
        echo "(clipboard read not supported on this device/API level)"
    fi
}

action_clipboard_paste() {
    _adb input keyevent 279
}

action_keyevent() {
    _require_args 1 "keyevent" "$@"
    local longpress=""
    [[ "$1" == "--longpress" ]] && { longpress="--longpress"; shift; }
    _adb input keyevent $longpress "$1"
}

action_key() {
    _require_args 1 "key" "$@"
    local longpress=""
    [[ "$1" == "--longpress" ]] && { longpress="--longpress"; shift; }
    local code
    code="$(_keycode "$1")"
    _adb input keyevent $longpress "$code"
}

action_list_keys() {
    echo "Supported key names (case-insensitive):"
    sed -n 's/^[[:space:]]*\([^)[:space:]][^)[:space:]]*\))[[:space:]]*echo[[:space:]].*/\1/p' "$0" | sort | paste -s -d' ' | fold -s -w 72
}

action_screenshot() {
    local file="${1:-screenshot-$(_timestamp).png}"
    _adb screencap -p "/sdcard/$file" && \
    _adb_host pull "/sdcard/$file" "$TMP_DIR/$file" && \
    _adb rm -f "/sdcard/$file" && \
    echo "Screenshot saved: $TMP_DIR/$file"
}

action_screenshot_pull() {
    local file="${1:-screenshot-$(_timestamp).png}"
    _adb screencap -p "/sdcard/$file" && \
    _adb_host pull "/sdcard/$file" "$TMP_DIR/$file" && \
    _adb rm -f "/sdcard/$file" && \
    echo "Screenshot saved: $TMP_DIR/$file"
}

action_screenshot_uia() {
    local file="${1:-screenshot-uia-$(_timestamp).png}"
    _adb uiautomator screenshot "/sdcard/$file" && \
    _adb_host pull "/sdcard/$file" "$TMP_DIR/$file" && \
    _adb rm -f "/sdcard/$file" && \
    echo "Screenshot saved: $TMP_DIR/$file"
}

action_screenrecord_start() {
    local file="${1:-screenrecord.mp4}"
    echo "Starting screen recording to /sdcard/$file (Ctrl+C to stop) ..."
    _adb screenrecord "/sdcard/$file" &
    SR_PID=$!
    echo "$SR_PID" > /tmp/.emulator-screenrecord-pid
    echo "Recording PID: $SR_PID"
}

action_screenrecord_stop() {
    local pid
    if [ -f /tmp/.emulator-screenrecord-pid ]; then
        pid="$(cat /tmp/.emulator-screenrecord-pid)"
        kill "$pid" 2>/dev/null || true
        rm -f /tmp/.emulator-screenrecord-pid
        sleep 1
    else
        _adb pkill -l SIGINT screenrecord 2>/dev/null || true
        sleep 1
    fi
    local file="${1:-screenrecord-$(_timestamp).mp4}"
    _adb_host pull "/sdcard/screenrecord.mp4" "$TMP_DIR/$file" 2>/dev/null && \
    _adb rm -f "/sdcard/screenrecord.mp4" && \
    echo "Screen recording saved: $TMP_DIR/$file" || \
    echo "No screen recording found at /sdcard/screenrecord.mp4"
}

action_orientation() {
    _require_args 1 "orientation" "$@"
    local val
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        portrait)        val=portrait ;;
        landscape)       val=landscape ;;
        reverse_landscape) val=reverse_landscape ;;
        reverse_portrait) val=reverse_portrait ;;
        sensor)          val=sensor ;;
        *) _die "unknown orientation '$1'. Use: portrait, landscape, reverse_landscape, reverse_portrait, sensor" ;;
    esac
    _adb content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0
    case "$val" in
        portrait)          _adb content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0 2>/dev/null || true ;;
        landscape)         _adb content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:1 2>/dev/null || true ;;
        reverse_portrait)  _adb content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:2 2>/dev/null || true ;;
        reverse_landscape) _adb content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:3 2>/dev/null || true ;;
        sensor)            _adb content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0 2>/dev/null || true ;;
    esac
}

action_fold() {
    _adb emu fold 2>/dev/null || echo "Warning: 'fold' not supported on this emulator"
}

action_unfold() {
    _adb emu unfold 2>/dev/null || echo "Warning: 'unfold' not supported on this emulator"
}

action_wake() {
    local awake
    awake=$(_get_wakefulness)
    [ "$awake" = "Awake" ] && return 0
    _adb input keyevent 26
    sleep 0.5
}

action_sleep() {
    _adb input keyevent 26
}

action_is_awake() {
    local awake
    awake=$(_get_wakefulness)
    [ "$awake" = "Awake" ] && echo "true" || echo "false"
}

action_wake_unlock() {
    action_wake
    sleep 0.3
    action_unlock
}

action_animations_on() {
    _adb settings put global window_animation_scale 1.0
    _adb settings put global transition_animation_scale 1.0
    _adb settings put global animator_duration_scale 1.0
    echo "Animations enabled"
}

action_animations_off() {
    _adb settings put global window_animation_scale 0.0
    _adb settings put global transition_animation_scale 0.0
    _adb settings put global animator_duration_scale 0.0
    echo "Animations disabled"
}

action_animations_get() {
    echo "Window:  $(_adb settings get global window_animation_scale)"
    echo "Transition: $(_adb settings get global transition_animation_scale)"
    echo "Animator: $(_adb settings get global animator_duration_scale)"
}

action_open_app() {
    _require_args 1 "open_app" "$@"
    local pkg="$1"
    # If it contains a slash, treat as component name
    if [[ "$pkg" == */* ]]; then
        _adb am start -n "$pkg"
    else
        local launcher
        launcher="$(_adb cmd package resolve-activity --brief "$pkg" 2>/dev/null | tail -1 || true)"
        if [ -z "$launcher" ] || [ "$launcher" = "NO_ACTIVITY" ] || [ "$launcher" = "null" ]; then
            # Fallback: try monkey to launch
            _adb monkey -p "$pkg" -c android.intent.category.LAUNCHER 1 2>/dev/null || \
            _adb am start -n "$pkg/.MainActivity" 2>/dev/null || \
            _die "could not find launcher activity for package '$pkg'"
        else
            _adb am start -n "$launcher"
        fi
    fi
}

action_kill_app() {
    _require_args 1 "kill_app" "$@"
    _adb am force-stop "$1"
}

action_clear_app() {
    _require_args 1 "clear_app" "$@"
    _adb pm clear "$1"
}

action_reboot() {
    _adb reboot
}

action_gps() {
    _require_args 2 "gps" "$@"
    _adb emu geo fix "$1" "$2"
}

action_location() {
    _require_args 1 "location" "$@"
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        network) _adb cmd location set-provider network 2>/dev/null || _die "location provider change not supported" ;;
        gps) _adb cmd location set-provider gps 2>/dev/null || _die "location provider change not supported" ;;
        passive) _adb cmd location set-provider passive 2>/dev/null || _die "location provider change not supported" ;;
        *) _die "unknown location provider '$1'. Use: network, gps, passive" ;;
    esac
}

action_gps_on() {
    _adb settings put secure location_providers_allowed +gps 2>/dev/null || true
}

action_gps_off() {
    _adb settings put secure location_providers_allowed -gps 2>/dev/null || true
}

action_gps_route() {
    _require_args 1 "gps_route" "$@"
    shift
    if [ $# -lt 1 ]; then
        _die "gps_route requires at least one lat,lng pair"
    fi
    for coord in "$@"; do
        local lat="${coord%%,*}"
        local lng="${coord#*,}"
        action_gps "$lat" "$lng"
        sleep 5
    done
}

action_rotate() {
    _require_args 1 "rotate" "$@"
    _adb emu sensor set orientation "$1" 0 0 2>/dev/null || \
    echo "Warning: sensor rotation not supported on this device/emulator"
}

action_call() {
    _require_args 1 "call" "$@"
    local number="$1"
    _adb emu gsm call "$number"
    echo "Simulated incoming call from $number"
}

action_sms() {
    _require_args 2 "sms" "$@"
    local number="$1" text="$2"
    _adb emu sms send "$number" "$text"
    echo "Simulated SMS from $number: $text"
}

action_network_type() {
    _require_args 1 "network_type" "$@"
    _adb emu gsm data "$(echo "$1" | tr '[:lower:]' '[:upper:]')" 2>/dev/null || \
    echo "Warning: network type change not supported"
}

action_airplane_on() {
    _adb settings put global airplane_mode_on 1 && \
    _adb am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true
}

action_airplane_off() {
    _adb settings put global airplane_mode_on 0 && \
    _adb am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
}

action_wifi_on() {
    _adb svc wifi enable || \
    _adb am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings 2>/dev/null || true
}

action_wifi_off() {
    _adb svc wifi disable
}

action_mobile_data_on() {
    _adb svc data enable
}

action_mobile_data_off() {
    _adb svc data disable
}

action_battery_level() {
    _require_args 1 "battery_level" "$@"
    _adb emu power capacity "$1"
}

action_battery_status() {
    _require_args 1 "battery_status" "$@"
    local val
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        unknown)       val=1 ;;
        charging)      val=2 ;;
        discharging)   val=3 ;;
        not_charging)  val=4 ;;
        full)          val=5 ;;
        *) _die "unknown battery status '$1'. Use: unknown, charging, discharging, not_charging, full" ;;
    esac
    _adb emu power status "$val"
}

action_battery_ac() {
    _require_args 1 "battery_ac" "$@"
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        true|1|on)  _adb emu power ac 1 ;;
        false|0|off) _adb emu power ac 0 ;;
        *) _die "battery_ac requires true or false" ;;
    esac
}

action_battery_present() {
    _require_args 1 "battery_present" "$@"
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        true|1|on)  _adb emu power present true ;;
        false|0|off) _adb emu power present false ;;
        *) _die "battery_present requires true or false" ;;
    esac
}

action_battery_health() {
    _require_args 1 "battery_health" "$@"
    local val
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        unknown)       val=1 ;;
        good)          val=2 ;;
        overheat)      val=3 ;;
        dead)          val=4 ;;
        over_voltage)  val=5 ;;
        unspecified_failure) val=6 ;;
        cold)          val=7 ;;
        *) _die "unknown battery health '$1'. Use: good, overheat, dead, over_voltage, failure, cold" ;;
    esac
    _adb emu power health "$val"
}

action_battery_reset() {
    _adb emu power ac 1
    _adb emu power status 2
    _adb emu power present true
    _adb emu power health 2
    _adb emu power capacity 100
}

action_network_speed() {
    _require_args 1 "network_speed" "$@"
    _adb emu network speed "$1" 2>/dev/null || \
    echo "Warning: network speed change not supported on this emulator"
}

action_network_delay() {
    _require_args 1 "network_delay" "$@"
    _adb emu network delay "$1" 2>/dev/null || \
    echo "Warning: network delay change not supported on this emulator"
}

action_network_speed_raw() {
    _require_args 4 "network_speed_raw" "$@"
    _adb emu network speed "$1:$2:$3:$4" 2>/dev/null || \
    echo "Warning: raw network speed not supported"
}

action_network_delay_raw() {
    _require_args 2 "network_delay_raw" "$@"
    _adb emu network delay "$1:$2" 2>/dev/null || \
    echo "Warning: raw network delay not supported"
}

action_network_disconnect() {
    _adb emu network disconnect 2>/dev/null || \
    echo "Warning: network disconnect not supported"
}

action_network_reconnect() {
    _adb emu network reconnect 2>/dev/null || \
    echo "Warning: network reconnect not supported"
}

action_monkey() {
    local count="${1:-500}" pkg="${2:-}"
    if [ -n "$pkg" ]; then
        _adb monkey -p "$pkg" "$count" 2>&1
    else
        _adb monkey "$count" 2>&1
    fi
}

action_monkey_seed() {
    _require_args 2 "monkey_seed" "$@"
    local count="$1" seed="$2" pkg="${3:-}"
    if [ -n "$pkg" ]; then
        _adb monkey -p "$pkg" -s "$seed" "$count" 2>&1
    else
        _adb monkey -s "$seed" "$count" 2>&1
    fi
}

action_monkey_touch() {
    local count="${1:-500}" pkg="${2:-}"
    if [ -n "$pkg" ]; then
        _adb monkey -p "$pkg" --pct-touch 100 "$count" 2>&1
    else
        _adb monkey --pct-touch 100 "$count" 2>&1
    fi
}

action_monkey_app() {
    _require_args 1 "monkey_app" "$@"
    local pkg="$1" count="${2:-500}"
    _adb monkey -p "$pkg" "$count" 2>&1
}

action_monkey_throttle() {
    _require_args 2 "monkey_throttle" "$@"
    local count="$1" ms="$2" pkg="${3:-}"
    if [ -n "$pkg" ]; then
        _adb monkey -p "$pkg" --throttle "$ms" "$count" 2>&1
    else
        _adb monkey --throttle "$ms" "$count" 2>&1
    fi
}

action_uia_dump() {
    local file="${1:-uia-dump-$(_timestamp).xml}"
    _adb uiautomator dump "/sdcard/$file" 2>/dev/null || \
    _adb uiautomator dump --compressed "/sdcard/$file" 2>/dev/null || \
    _adb uiautomator dump "/sdcard/$file"
    _adb_host pull "/sdcard/$file" "$TMP_DIR/$file" 2>/dev/null && \
    _adb rm -f "/sdcard/$file" && \
    echo "UI dump saved: $TMP_DIR/$file"
}

action_uia_dump_pull() {
    action_uia_dump "$@"
}

action_uia_find() {
    _require_args 1 "uia_find" "$@"
    local tmp_xml
    tmp_xml="/tmp/uia-dump-$(_timestamp).xml"
    _adb uiautomator dump "/sdcard/ui.xml" 2>/dev/null || \
    _adb uiautomator dump "/sdcard/ui.xml"
    _adb_host pull "/sdcard/ui.xml" "$tmp_xml" 2>/dev/null || { echo "UI dump failed"; return 1; }
    _adb rm -f "/sdcard/ui.xml"
    grep -o "text=\"[^\"]*$1[^\"]*\"" "$tmp_xml" || echo "No elements found containing '$1'"
    grep -o "content-desc=\"[^\"]*$1[^\"]*\"" "$tmp_xml" || true
    rm -f "$tmp_xml"
}

action_uia_find_id() {
    _require_args 1 "uia_find_id" "$@"
    local tmp_xml
    tmp_xml="/tmp/uia-dump-$(_timestamp).xml"
    _adb uiautomator dump "/sdcard/ui.xml" 2>/dev/null || \
    _adb uiautomator dump "/sdcard/ui.xml"
    _adb_host pull "/sdcard/ui.xml" "$tmp_xml" 2>/dev/null || { echo "UI dump failed"; return 1; }
    _adb rm -f "/sdcard/ui.xml"
    grep -o "resource-id=\"[^\"]*$1[^\"]*\"" "$tmp_xml" || echo "No elements found with resource-id containing '$1'"
    rm -f "$tmp_xml"
}

action_uia_clickable() {
    local tmp_xml
    tmp_xml="/tmp/uia-dump-$(_timestamp).xml"
    _adb uiautomator dump "/sdcard/ui.xml" 2>/dev/null || \
    _adb uiautomator dump "/sdcard/ui.xml"
    _adb_host pull "/sdcard/ui.xml" "$tmp_xml" 2>/dev/null || { echo "UI dump failed"; return 1; }
    _adb rm -f "/sdcard/ui.xml"
    grep -o 'class="[^"]*"[^>]*clickable="true"[^>]*' "$tmp_xml" | \
        while IFS= read -r line; do
            local text bounds
            text=$(echo "$line" | grep -o 'text="[^"]*"' | head -1)
            bounds=$(echo "$line" | grep -o 'bounds="[^"]*"')
            echo "$text  $bounds"
        done
    rm -f "$tmp_xml"
}

action_info() {
    echo "Device: $(_adb getprop ro.product.model 2>/dev/null || echo unknown)"
    echo "Manufacturer: $(_adb getprop ro.product.manufacturer 2>/dev/null || echo unknown)"
    echo "Android: $(_adb getprop ro.build.version.release 2>/dev/null || echo unknown)"
    echo "SDK: $(_adb getprop ro.build.version.sdk 2>/dev/null || echo unknown)"
    echo "ABI: $(_adb getprop ro.product.cpu.abi 2>/dev/null || echo unknown)"
    action_size
    action_density
}

action_size() {
    local size
    size="$(_adb wm size 2>/dev/null || echo unknown)"
    echo "Resolution: $size"
}
action_size_width() {
    _get_screen_width
}
action_size_height() {
    _get_screen_height
}
action_center_x() {
    local w
    w=$(_get_screen_width)
    echo $(( w / 2 ))
}
action_center_y() {
    local h
    h=$(_get_screen_height)
    echo $(( h / 2 ))
}

action_density() {
    local dpi
    dpi="$(_adb wm density 2>/dev/null | sed -n 's/.* \([0-9][0-9]*\)/\1/p' || echo unknown)"
    echo "Density: ${dpi} dpi"
}

action_wait_boot() {
    echo "Waiting for device to boot..."
    _adb_host wait-for-device 2>/dev/null
    while [ "$(_adb getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    echo "Device boot completed."
}

action_list_apps() {
    local filter="${1:-}"
    if [ -n "$filter" ]; then
        _adb pm list packages 2>/dev/null | grep -i "$filter" || echo "No packages found matching '$filter'"
    else
        _adb pm list packages 2>/dev/null
    fi
}

action_start_activity() {
    _require_args 1 "start_activity" "$@"
    _adb am start -n "$1"
}

action_broadcast() {
    _require_args 2 "broadcast" "$@"
    local action="$1"; shift
    _adb am broadcast -a "$action" "$@"
}

action_setprop() {
    _require_args 2 "setprop" "$@"
    _adb setprop "$1" "$2"
}

action_getprop() {
    _require_args 1 "getprop" "$@"
    _adb getprop "$1"
}

action_raw() {
    _require_args 1 "raw" "$@"
    _adb "$@"
}

action_open_url() {
    _require_args 1 "open_url" "$@"
    _adb am start -a android.intent.action.VIEW -d "$1"
}

action_grant_permission() {
    _require_args 2 "grant_permission" "$@"
    _adb pm grant "$1" "$2"
}

action_revoke_permission() {
    _require_args 2 "revoke_permission" "$@"
    _adb pm revoke "$1" "$2"
}

action_accessibility_on() {
    _adb settings put secure accessibility_enabled 1 || true
}

action_immersive_mode() {
    _require_args 1 "immersive_mode" "$@"
    case "$(echo "$1" | tr '[:upper:]' '[:lower:]')" in
        full|fully)       _adb settings put global policy_control immersive.full="*" ;;
        status)           _adb settings put global policy_control immersive.status="*" ;;
        navigation)       _adb settings put global policy_control immersive.navigation="*" ;;
        off|disabled)     _adb settings put global policy_control immersive.preconfirms="*"; _adb settings put global policy_control "null" ;;
        *) _die "unknown immersive mode '$1'. Use: full, status, navigation, off" ;;
    esac
}

action_statusbar_expand() {
    _adb cmd statusbar expand-notification-panel 2>/dev/null || action_notifications
}

action_statusbar_collapse() {
    _adb cmd statusbar collapse-panel 2>/dev/null || _adb input keyevent 4
}

action_logcat() {
    local filter="${1:-*:V}"
    _adb_host logcat -v time "$filter"
}

action_logcat_clear() {
    _adb_host logcat -c
}

# ── Main Dispatch ───────────────────────────────────────────────────────

usage() {
    cat <<'USAGE'
Usage: bash emulator-control.sh [options] <action> [args...]

Options:
  -s <serial>    Target specific device serial (default: first device or ANDROID_SERIAL)
  -h             Show this help

Actions (keyboard/mouse/gestures):
  tap <x> <y>                         Tap at coordinates
  tap_percent <x%> <y%>               Tap at percentage of screen (e.g. 50% 50%)
  doubletap <x> <y>                   Double-tap
  swipe <x1> <y1> <x2> <y2> [ms]     Swipe gesture
  swipe_percent <x1%> <y1%> <x2%> <y2%> [ms]  Swipe at percentage coordinates
  drag <x1> <y1> <x2> <y2> [ms]      Drag and drop
  longpress <x> <y> [ms]              Long press
  scroll_down [x y steps]             Scroll down
  scroll_up [x y steps]               Scroll up
  scroll_left <x> <y> [steps]         Scroll left
  scroll_right <x> <y> [steps]        Scroll right
  pinchin [x y dist]                  Pinch in (zoom out)
  pinchout [x y dist]                 Pinch out (zoom in)
  unlock                              Swipe up to unlock
  notifications                       Pull notification shade
  quicksettings                       Open quick settings
  dismiss [x y]                       Swipe to dismiss

  type <text>                         Type text
  type_unicode <text>                 Type text with emoji/unicode support
  type_slow <text> [delay_ms]         Type text with per-character delay
  clear_field                         Select all + delete in focused field
  clipboard_set <text>                Copy text to clipboard
  clipboard_get                       Read clipboard content
  clipboard_paste                     Paste from clipboard

  key <name>                          Press key by name (home, back, etc.)
  key --longpress <name>              Long-press a key
  keyevent <code>                     Press key by Android keycode
  list_keys                           List all supported key names

Screen:
  screenshot [file]                   Take screenshot
  screenshot_pull [file]              Screenshot + pull to host
  screenshot_uia [file]               Screenshot via UiAutomator
  screenrecord_start [file]           Start recording
  screenrecord_stop [file]            Stop recording and pull

State:
  orientation <mode>                  portrait|landscape|reverse_landscape|reverse_portrait|sensor
  fold / unfold                       Foldable screen toggle
  wake                                Wake device
  wake_unlock                         Wake + unlock device
  sleep                               Put device to sleep
  is_awake                            Check if screen is on
  animations_on / animations_off      Toggle window animations
  animations_get                      Show animation scale values
  open_app <package>                  Launch app
  kill_app <package>                  Force-stop app
  clear_app <package>                 Clear app data
  reboot                              Reboot device
  immersive_mode <mode>               full|status|navigation|off
  statusbar_expand                    Expand notification panel
  statusbar_collapse                  Collapse notification panel

Location & Sensors:
  gps <lat> <lng>                     Set GPS location
  location <provider>                 network|gps|passive
  gps_on / gps_off                    Toggle GPS
  gps_route <lat,lng>...              Simulate route
  rotate <degrees>                    Set rotation

Telephony:
  call <number>                       Simulate incoming call
  sms <number> <text>                 Simulate incoming SMS
  network_type <type>                 lte|hspa|edge|gprs|gsm
  airplane_on / airplane_off          Toggle airplane mode
  wifi_on / wifi_off                  Toggle WiFi
  mobile_data_on / mobile_data_off    Toggle mobile data

Battery:
  battery_level <0-100>               Set battery level
  battery_status <status>             charging|discharging|not_charging|full
  battery_ac <true|false>             Set AC charging
  battery_present <true|false>        Set battery present
  battery_health <health>             good|overheat|dead|failure|cold
  battery_reset                       Reset to defaults

Network:
  network_speed <tier>                edge|gprs|umts|hspa|lte|full
  network_delay <tier>                gprs|edge|umts|lte|none
  network_speed_raw <up> <down> <lat> <loss>
  network_delay_raw <min> <max>
  network_disconnect / network_reconnect

Monkey:
  monkey [count] [pkg]                Random events
  monkey_seed <count> <seed> [pkg]    Reproducible monkey
  monkey_touch [count] [pkg]          Touch events only
  monkey_app <pkg> [count]            Single app monkey
  monkey_throttle <count> <ms> [pkg]  Throttled monkey

UI Analysis:
  uia_dump [file]                     Dump UI hierarchy XML
  uia_dump_pull [file]                Dump + pull UI hierarchy
  uia_find <text>                     Search UI for text
  uia_find_id <id>                    Search UI for resource-id
  uia_clickable                       List clickable elements

Utilities:
  info                                Device info summary
  size                                Get resolution
  density                             Get DPI
  wait_boot                           Wait for boot completion
  list_apps [filter]                  List installed packages
  start_activity <component>          Start specific activity
  broadcast <action> [extras]         Send broadcast
  setprop <name> <value>              Set system property
  getprop <name>                      Get system property
  raw <command>...                    Run raw ADB shell command
  open_url <url>                      Open URL in browser
  grant_permission <pkg> <perm>       Grant runtime permission
  revoke_permission <pkg> <perm>      Revoke runtime permission
  accessibility_on                    Enable accessibility
  logcat [filter]                     Read logcat
  logcat_clear                        Clear logcat

Set ANDROID_SERIAL env var or use -s to target a specific device.
Set ADB_ECHO=1 to show every ADB command.
USAGE
    exit 0
}

# Parse options
while getopts ":s:h" opt; do
    case "$opt" in
        s) ADB_SERIAL="$OPTARG" ;;
        h) usage ;;
        *) usage ;;
    esac
done
shift $((OPTIND - 1))

[ $# -lt 1 ] && usage

action="$1"
shift

# Map action names to function calls
case "$action" in
    tap)                action_tap "$@" ;;
    tap_percent)        action_tap_percent "$@" ;;
    doubletap)          action_doubletap "$@" ;;
    swipe)              action_swipe "$@" ;;
    swipe_percent)      action_swipe_percent "$@" ;;
    drag)               action_drag "$@" ;;
    longpress)          action_longpress "$@" ;;
    scroll_down)        action_scroll_down "$@" ;;
    scroll_up)          action_scroll_up "$@" ;;
    scroll_left)        action_scroll_left "$@" ;;
    scroll_right)       action_scroll_right "$@" ;;
    pinchin)            action_pinchin "$@" ;;
    pinchout)           action_pinchout "$@" ;;
    unlock)             action_unlock "$@" ;;
    notifications)      action_notifications "$@" ;;
    quicksettings)      action_quicksettings "$@" ;;
    dismiss)            action_dismiss "$@" ;;

    type)               action_type "$@" ;;
    type_unicode)       action_type_unicode "$@" ;;
    type_slow)          action_type_slow "$@" ;;
    clear_field)        action_clear_field "$@" ;;
    clipboard_set)      action_clipboard_set "$@" ;;
    clipboard_get)      action_clipboard_get "$@" ;;
    clipboard_paste)    action_clipboard_paste "$@" ;;

    key)                action_key "$@" ;;
    keyevent)           action_keyevent "$@" ;;
    list_keys)          action_list_keys "$@" ;;

    screenshot)         action_screenshot "$@" ;;
    screenshot_pull)    action_screenshot_pull "$@" ;;
    screenshot_uia)     action_screenshot_uia "$@" ;;
    screenrecord_start) action_screenrecord_start "$@" ;;
    screenrecord_stop)  action_screenrecord_stop "$@" ;;

    orientation)        action_orientation "$@" ;;
    fold)               action_fold "$@" ;;
    unfold)             action_unfold "$@" ;;
    wake)               action_wake "$@" ;;
    wake_unlock)        action_wake_unlock "$@" ;;
    sleep)              action_sleep "$@" ;;
    is_awake)           action_is_awake "$@" ;;
    animations_on)      action_animations_on "$@" ;;
    animations_off)     action_animations_off "$@" ;;
    animations_get)     action_animations_get "$@" ;;
    open_app)           action_open_app "$@" ;;
    kill_app)           action_kill_app "$@" ;;
    clear_app)          action_clear_app "$@" ;;
    reboot)             action_reboot "$@" ;;

    gps)                action_gps "$@" ;;
    location)           action_location "$@" ;;
    gps_on)             action_gps_on "$@" ;;
    gps_off)            action_gps_off "$@" ;;
    gps_route)          action_gps_route "$@" ;;
    rotate)             action_rotate "$@" ;;

    call)               action_call "$@" ;;
    sms)                action_sms "$@" ;;
    network_type)       action_network_type "$@" ;;
    airplane_on)        action_airplane_on "$@" ;;
    airplane_off)       action_airplane_off "$@" ;;
    wifi_on)            action_wifi_on "$@" ;;
    wifi_off)           action_wifi_off "$@" ;;
    mobile_data_on)     action_mobile_data_on "$@" ;;
    mobile_data_off)    action_mobile_data_off "$@" ;;

    battery_level)      action_battery_level "$@" ;;
    battery_status)     action_battery_status "$@" ;;
    battery_ac)         action_battery_ac "$@" ;;
    battery_present)    action_battery_present "$@" ;;
    battery_health)     action_battery_health "$@" ;;
    battery_reset)      action_battery_reset "$@" ;;

    network_speed)      action_network_speed "$@" ;;
    network_delay)      action_network_delay "$@" ;;
    network_speed_raw)  action_network_speed_raw "$@" ;;
    network_delay_raw)  action_network_delay_raw "$@" ;;
    network_disconnect) action_network_disconnect "$@" ;;
    network_reconnect)  action_network_reconnect "$@" ;;

    monkey)             action_monkey "$@" ;;
    monkey_seed)        action_monkey_seed "$@" ;;
    monkey_touch)       action_monkey_touch "$@" ;;
    monkey_app)         action_monkey_app "$@" ;;
    monkey_throttle)    action_monkey_throttle "$@" ;;

    uia_dump)           action_uia_dump "$@" ;;
    uia_dump_pull)      action_uia_dump_pull "$@" ;;
    uia_find)           action_uia_find "$@" ;;
    uia_find_id)        action_uia_find_id "$@" ;;
    uia_clickable)      action_uia_clickable "$@" ;;

    info)               action_info "$@" ;;
    size)               action_size "$@" ;;
    density)            action_density "$@" ;;
    wait_boot)          action_wait_boot "$@" ;;
    list_apps)          action_list_apps "$@" ;;
    start_activity)     action_start_activity "$@" ;;
    broadcast)          action_broadcast "$@" ;;
    setprop)            action_setprop "$@" ;;
    getprop)            action_getprop "$@" ;;
    raw)                action_raw "$@" ;;
    open_url)           action_open_url "$@" ;;
    grant_permission)   action_grant_permission "$@" ;;
    revoke_permission)  action_revoke_permission "$@" ;;
    accessibility_on)   action_accessibility_on "$@" ;;
    immersive_mode)     action_immersive_mode "$@" ;;
    statusbar_expand)   action_statusbar_expand "$@" ;;
    statusbar_collapse) action_statusbar_collapse "$@" ;;
    logcat)             action_logcat "$@" ;;
    logcat_clear)       action_logcat_clear "$@" ;;

    help|--help|-h)     usage ;;
    *)                  _die "unknown action '$action'. Run with -h for help." ;;
esac
