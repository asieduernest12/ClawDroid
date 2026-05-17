# ClawDroid Dashboard Card Design — Mockup Document

## Design Rationale

### Why Cards?
The current UI suffers from a lack of visual hierarchy: status, controls, and actions are stacked without grouping, making it hard for users to understand what belongs together. Material Design 3 Cards provide:
- **Elevation** to visually separate sections
- **Rounded corners** for a modern, friendly aesthetic
- **Clear boundaries** that reduce cognitive load
- **Scannability** — users can glance at a card and know what it contains

### Why Chips for Status?
Plain text status ("PicoClaw: Stopped") is easy to miss and doesn't convey severity or state at a glance. Material Chips with color-coding:
- Use **green** for running/success states
- Use **red** for stopped/error states  
- Use **yellow/amber** for loading/transitional states
- Are compact, familiar, and accessible

### Why Icons on Buttons?
The current buttons are text-only uppercase labels that look generic and don't help users recognize actions quickly. Adding Material Icons:
- Speeds up recognition (play → start, stop → stop, refresh → restart)
- Reduces the need to read every label
- Makes the UI feel polished and intentional
- Fixes the "RESTART" vertical text rendering bug by using icon+compact-label layouts

### Why a Top App Bar?
The current screen has no anchor point — users don't know what app they're in. A top app bar:
- Brands the screen with the ClawDroid name/logo
- Provides a home for the Help menu (onboarding/info access)
- Follows Android platform conventions users expect
- Supports scroll-behavior elevation changes

### Why a Floating Action Button (FAB)?
The primary user goal is to start/stop PicoClaw. A FAB:
- Makes the primary action impossible to miss
- Changes color/icon based on state (play when stopped, stop when running)
- Animates state transitions for delightful feedback
- Follows Material Design primary-action pattern

---

## User Flow

### Fresh Install Flow
1. User opens app → sees branded app bar
2. **Bootstrap Card** shows circular spinner with message "Setting up your AI assistant…"
3. Secondary actions (Mission Control, Logs, Settings) are disabled until bootstrap completes
4. When bootstrap finishes, spinner is replaced with a green check chip
5. FAB becomes enabled and pulses subtly to invite interaction

### Running State Flow
1. User taps FAB (play icon) → FAB morphs into a loading spinner
2. **PicoClaw Status Chip** turns amber with text "Starting…"
3. Once process starts, chip turns green with text "Running"
4. FAB icon changes to stop icon
5. **Server Status Card** updates with port and a green "Online" chip
6. Mission Control and Logs buttons become enabled

### Stopping State Flow
1. User taps FAB (stop icon) → FAB shows loading spinner
2. PicoClaw Status Chip turns amber with text "Stopping…"
3. Once stopped, chip turns red with text "Stopped"
4. FAB icon changes back to play icon
5. Server Status shows gray "Offline" chip

### Error State Flow
1. If bootstrap fails, the Bootstrap Card shows a red error chip and a "Retry" button inline
2. Inline error message persists (not a Toast) so users can read and act on it
3. If PicoClaw fails to start, the Status Card shows a red error message below the chip

---

## Component Choices

| Section | Component | Rationale |
|---------|-----------|-----------|
| Root Layout | `CoordinatorLayout` + `AppBarLayout` | Enables scroll-snapping, FAB anchoring, and elevation behaviors |
| App Bar | `MaterialToolbar` | Standard MD3 component; supports title, subtitle, and overflow menu |
| Content | `NestedScrollView` | Ensures all cards remain accessible on small screens |
| Status Section | `MaterialCardView` | Groups bootstrap + process + server status into one scannable unit |
| Bootstrap Indicator | `CircularProgressIndicator` | Much more visible than a thin horizontal line; conveys "busy" clearly |
| Status Indicators | `MaterialChip` with custom colors | Compact, color-coded, accessible; familiar to Android users |
| Primary Action | `FloatingActionButton` (FAB) | Platform-standard for the single most important action |
| Secondary Controls | `MaterialButton` (icon + text) inside `GridLayout` | Fixed the vertical-text bug by giving buttons adequate width; icons prevent mis-taps |
| Info/Help | `MaterialCardView` with `MaterialDivider` | Separates educational content from controls without feeling cramped |
| Server Info | `MaterialCardView` with `ListItem` style rows | Port, health, and uptime each get a clear row |

---

## Layout Architecture

```
CoordinatorLayout (root)
├── AppBarLayout
│   └── MaterialToolbar (title: ClawDroid, menu: Help)
├── NestedScrollView
│   └── LinearLayout (vertical, padding)
│       ├── Status Card
│       │   ├── CardHeader: "System Status"
│       │   ├── Bootstrap Row: CircularProgressIndicator + Chip
│       │   ├── MaterialDivider
│       │   ├── PicoClaw Row: Icon + Chip + Error Text
│       │   ├── MaterialDivider
│       │   └── Server Row: Icon + Port + Health Chip
│       ├── Info Card
│       │   ├── CardHeader: "What is PicoClaw?"
│       │   └── Body: 2-line user-friendly description
│       └── Controls Card
│           ├── CardHeader: "Controls"
│           ├── GridLayout (2 cols)
│           │   ├── Mission Control (icon + text)
│           │   ├── View Logs (icon + text)
│           │   ├── Settings (icon + text)
│           │   └── Restart (icon + text)
│           └── Inline Error Text (persisting messages)
└── FloatingActionButton (anchored to bottom-end)
```

---

## Accessibility Considerations

- All icons have `contentDescription` via button text (icon+label buttons)
- Chips use high-contrast colors (green on light surface, red on light surface)
- Error text uses `textAppearanceBodyMedium` with `colorError` for visibility
- Touch targets are minimum 48dp for all interactive elements
- Progress indicators announce state changes to TalkBack via `setAccessibilityLiveRegion`

---

## Responsive Behavior

- On phones: single-column cards, FAB anchored to bottom-end with 16dp margin
- On tablets (sw600dp): cards expand to max-width 600dp, centered in scroll view
- In landscape: scroll view allows vertical scrolling; FAB remains anchored

---

## Theme & Theming

The layout is built entirely with Material Design 3 components, so it automatically adapts to:
- **Light theme**: surfaces are near-white, on-surface text is dark, chips use theme-aware tints
- **Dark theme**: surfaces elevate with lighter overlays, chips maintain color semantics (green still means "good")
- **Dynamic Color (Android 12+)**: if enabled, `colorPrimary` and `colorSecondary` adapt to wallpaper; chips and FAB inherit these tones while preserving semantic color mapping via custom `MaterialColors` attributes

---

## Migration Notes for Developers

1. Replace existing `activity_main.xml` with `activity_main_dashboard.xml` (or merge in-place)
2. Update `MainActivity.kt` view bindings to match new IDs:
   - `R.id.status_bootstrap_chip` (was `terminal_status_text`)
   - `R.id.status_picoclaw_chip` (was `picoclaw_status_text`)
   - `R.id.status_server_chip` (new)
   - `R.id.fab_primary_action` (replaces start/stop buttons)
   - `R.id.btn_restart`, `R.id.btn_mission_control`, `R.id.btn_view_logs`, `R.id.btn_settings` (keep or remap)
3. Add new string resources (see `strings-needed.md`)
4. Add new color attributes (see `colors-needed.md`)
5. Ensure `Theme.ClawDroid` parent remains `Theme.Material3.DayNight.NoActionBar` (toolbar is added explicitly in layout)

---

## Files Delivered

1. `mockup-dashboard-card.md` — this document
2. `activity_main_dashboard.xml` — complete Android XML layout
3. `strings-needed.md` — all new string resources required
4. `colors-needed.md` — color and theme additions required
