# Minimalist Terminal Design — ClawDroid Main Screen

## 1. Design Rationale

### 1.1 Problem Statement
The current `activity_main.xml` suffers from:
- **No visual hierarchy**: plain TextViews stacked vertically with equal weighting.
- **Missing iconography**: control buttons are text-only, making them harder to scan at a glance.
- **Invisible progress**: the bootstrap `ProgressBar` uses default styling and is often `gone`, but when shown it lacks contrast.
- **Layout bug**: the "Restart" button label wraps vertically because the horizontal `LinearLayout` squeezes `wrap_content` buttons on narrow screens.
- **Mission Control defect**: opens an external browser and renders raw HTML source instead of a proper web view.

### 1.2 Design Direction: "Minimalist Terminal"
ClawDroid is, at its core, a **terminal emulator wrapper** around a lightweight AI binary. The UI should communicate that identity immediately. Rather than fighting the terminal aesthetic with Material cards and rounded surfaces, we **embrace** it:

- **AMOLED black** (`#000000`) background for power savings and immersion.
- **Monospace font** everywhere to evoke a code/terminal environment.
- **Neon accents** for semantic colour-coding (green=OK, red=error, cyan=info, yellow=warn).
- **ASCII-art separators** instead of card elevations — cheap, sharp, and thematically consistent.
- **Information-dense** layout: no 16dp gutters; we use 8dp or less.
- **Live log console** on the main screen — users should *see* the AI working without tapping "View Logs".
- **`>_` prompt** elements to reinforce interactivity.

---

## 2. User Flow

### 2.1 Cold Start (Bootstrap)
1. User launches app → screen flashes black immediately.
2. Header `>_ CLAWDROID` renders in cyan.
3. Status line 1: `[ >> ] Initializing Termux environment…` (yellow).
4. Thin neon-green progress bar fills across the width.
5. Bootstrap completes → line 1 flips to `[ OK ] Termux environment ready` (green); progress bar hides.
6. Hint line updates: `>_ Press START to launch PicoClaw`.

### 2.2 Start PicoClaw
1. User taps **START** (green icon button in bottom command bar).
2. Status line 2: `[ >> ] PicoClaw: Starting…` (yellow).
3. Log console (bottom 40%) begins streaming stdout lines in green monospace text.
4. Server port binds → status line 3: `[ :: ] localhost:8080 | HEALTHY` (cyan).
5. Line 2 flips to `[ OK ] PicoClaw: Running` (green).
6. Hint line: `>_ Press DASHBOARD to open Mission Control`.

### 2.3 Mission Control
1. User taps **DASHBOARD**.
2. **Fix for current bug**: Instead of firing an `ACTION_VIEW` Intent to an external browser, the app opens an **in-app Chrome Custom Tab** or an **embedded `WebView` activity** styled with the same dark theme. This guarantees the HTML is rendered, not displayed as source.
3. If the server is unhealthy, the button is disabled and a yellow Snackbar warns: `Server health check failed`.

### 2.4 Stop / Restart
1. **STOP** (red) kills the PicoClaw process; line 2 flips to `[ !! ] PicoClaw: Stopped` (red); log console appends a red "Process terminated" line.
2. **RESTART** (yellow) performs a stop→start cycle with a single tap. The button colour choice (yellow) signals "transient/caution".

### 2.5 Logs & Settings
- **LOGS** button can either focus the existing console (scroll to bottom) or launch a full-screen `LogActivity` with search/filter if the backlog grows large.
- **CONFIG** (renamed from "Settings" to fit the command-bar width) opens a preference screen using the same dark monospace theme.

---

## 3. Component Choices

### 3.1 Root Layout: `ConstraintLayout`
- **Why**: We need precise percentage-based splits (top status ~45%, log console 40%, command bar remainder). `ConstraintLayout` handles this with `layout_constraintHeight_percent` without nested `LinearLayout` weight penalties.

### 3.2 Status Panel: `NestedScrollView` → `LinearLayout`
- **Why**: On very small devices (e.g. API 21 phones with 4" screens) the status text can still overflow. Wrapping the top block in a scroll container guarantees accessibility while keeping the log console size fixed.

### 3.3 Log Console: `FrameLayout` + `ScrollView` + `TextView`
- **Why**: `FrameLayout` lets us layer the window chrome title bar and the `>_` prompt overlay without extra nesting. The `TextView` uses `lineSpacingMultiplier="1.15"` to mimic terminal line height.
- **Background**: `#0A0A0A` (slightly lighter than pure black) creates a visual boundary without needing card shadows.

### 3.4 Command Bar: `HorizontalScrollView` → `LinearLayout` → `MaterialButton`
- **Why**: six buttons do not fit comfortably on a 320dp-wide screen. `HorizontalScrollView` with `scrollbars="none"` gives a swipeable command palette feel (common in terminal apps like Termux).
- **Button style**: `Widget.MaterialComponents.Button.TextButton` removes background fill, keeping the bar flat. Icons are placed **above** text (`iconGravity="top"`) so each button is a compact square-ish target (56dp tall, ~72dp wide).
- **Bug fix**: `android:maxLines="1"`, `android:minWidth="72dp"`, and short uppercase labels (`START`, `STOP`, `RESTART`) prevent the old vertical-wrap bug entirely.

### 3.5 Separators: `TextView` with box-drawing characters
- **Why**: No `View` backgrounds or `ShapeDrawable` maintenance. A `TextView` containing `═` or `─` characters in monospace is razor-sharp, thematically correct, and zero-overhead.

### 3.6 Progress Bar: `?android:attr/progressBarStyleHorizontal`
- **Why**: Native component, but stripped down to 4dp height with `progressTint` and `progressBackgroundTint` mapped to neon green and dark grey. It looks like a classic terminal progress bar (e.g. `wget` or `apt`).

---

## 4. Typography

| Element              | Font Family   | Size  | Weight | Colour        |
|----------------------|---------------|-------|--------|---------------|
| Header (`>_ CLAWDROID`) | monospace | 18sp  | Bold   | Cyan          |
| Status lines         | monospace     | 14sp  | Normal | Green/Red/Cyan|
| Hints                | monospace     | 12sp  | Normal | Yellow        |
| Log output           | monospace     | 11sp  | Normal | Green         |
| Console chrome       | monospace     | 12sp  | Normal | Grey          |
| Command labels       | monospace     | 11sp  | Bold   | Accent-mapped |

**Font note**: `android:fontFamily="monospace"` resolves to the system monospace (Roboto Mono on Pixel devices, Droid Sans Mono on older API 21 hardware). For a consistent brand feel, consider bundling **JetBrains Mono** or **Fira Code** as a downloadable font resource (`res/font/`).

---

## 5. Accessibility Considerations

- **Colour alone is never the only cue**: bracket prefixes (`[ OK ]`, `[ !! ]`) convey state even if the user is colour-blind.
- **Touch targets**: command bar buttons are 56dp tall (meets Material minimum 48dp).
- **Contrast**: neon green `#00FF41` on black `#000000` gives a ratio > 15:1, far exceeding WCAG AAA.
- **Scalable text**: all sizes are in `sp` and the status panel scrolls if text scales up.

---

## 6. Migration Path (for developers)

1. Copy `colors_needed.xml` entries into `res/values/colors.xml`.
2. Copy `strings_needed.xml` entries into `res/values/strings.xml`.
3. Add the six Material icons (`ic_play_arrow`, `ic_stop`, `ic_refresh`, `ic_web`, `ic_list`, `ic_settings`) to `res/drawable/` (Vector Assets, 24dp).
4. Replace `activity_main.xml` with `activity_main_terminal.xml` (or create a new flavour/activity if A/B testing).
5. In `MainActivity.kt`, update `setContentView(R.layout.activity_main_terminal)`.
6. **Mission Control fix**: change the `btn_mission_control` click handler from:
   ```kotlin
   startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
   ```
   to a `CustomTabsIntent` or an internal `WebViewActivity` with dark-theme chrome.
7. (Optional) Add a subtle alpha-pulse `ObjectAnimator` to `console_prompt` for the blinking cursor effect.

---

## 7. Future Enhancements (out of scope for this mockup)

- **ANSI colour support** in the log `TextView` via a simple Spannable parser so PicoClaw can emit coloured logs natively.
- **Command palette overlay**: long-press the `>_` prompt to open a `RecyclerView` of recent commands.
- **Haptic feedback** on command-bar taps to reinforce the "mechanical keyboard" metaphor.
- **Font bundling**: ship JetBrains Mono Regular & Bold for pixel-perfect cross-device consistency.
