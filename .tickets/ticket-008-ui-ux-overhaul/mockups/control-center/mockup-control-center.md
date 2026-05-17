# ClawDroid Main Screen Mockup — Control Center Design

## Design Rationale

### Problem Statement
The current `activity_main.xml` is a vertically-stacked column of plain `TextView`s and small `MaterialButton`s with the following issues:
1. **No visual hierarchy** — all information has equal weight, making scan-ability poor.
2. **No iconography** — buttons rely solely on text labels, increasing cognitive load.
3. **Invisible progress** — the bootstrap `ProgressBar` is a thin horizontal line that is easy to miss.
4. **Layout bug** — the Restart button text renders vertically in some locales due to `wrap_content` width in a constrained horizontal `LinearLayout`.
5. **Mission Control opens raw HTML** — because an external browser is launched with `Intent.ACTION_VIEW`, the browser sometimes shows raw source instead of rendering the dashboard.

### Design Direction: "Control Center"
Inspired by **iOS Control Center** and **Android Quick Settings**, this design transforms the main screen into a tactile, high-contrast dashboard. The user should be able to understand system state at a glance and hit the right control without precision aiming.

---

## User Flow

```
+---------------------------------------------------+
|  SWIPE DOWN (SwipeRefreshLayout)                  |
|  → Triggers status refresh + haptic feedback    |
+---------------------------------------------------+
|                                                   |
|  [HEADER] "ClawDroid" + ambient gradient bg       |
|                                                   |
|  ┌─────────────────────────────────────────────┐  |
|  │  STATUS TILE (large icon + text)            │  |
|  │  ● PicoClaw — RUNNING  [green glow]         │  |
|  └─────────────────────────────────────────────┘  |
|                                                   |
|  ┌─────────────────┐  ┌────────────────────────┐  |
|  │  BOOTSTRAP TILE │  │  SERVER / NETWORK TILE │  |
|  │  [progress]     │  │  8080  ▓▓▓▓░ HEALTHY  │  |
|  └─────────────────┘  └────────────────────────┘  |
|                                                   |
|  ┌─────────────┐  ┌─────────────┐  ┌──────────┐  |
|  │   [START]   │  │   [STOP]    │  │ [RESTART]│  |
|  │   Circle    │  │   Circle    │  │  Square  │  |
|  │   72×72dp   │  │   72×72dp   │  │  72×72dp │  |
|  └─────────────┘  └─────────────┘  └──────────┘  |
|                                                   |
|  ┌─────────────────┐  ┌────────────────────────┐  |
|  │ [Mission Ctrl]  │  │    [View Logs]         │  |
|  │  rounded rect   │  │    rounded rect        │  |
|  └─────────────────┘  └────────────────────────┘  |
|                                                   |
|  [ SETTINGS ]  (text button, bottom center)       |
|                                                   |
+---------------------------------------------------+
|  BOTTOM SHEET (peek height = 48dp)                |
|  → Drag up to reveal live log stream               |
|  → "Close" handle or swipe down to dismiss        |
+---------------------------------------------------+
```

### Primary Actions
1. **Start / Stop** — Large circular toggle buttons (72dp diameter). Color changes based on state:
   - Start: **Emerald green** (`#FF00C853`) with white `play_arrow` icon.
   - Stop: **Crimson red** (`#FFD50000`) with white `stop` icon.
   - Disabled: **Gray 400** (`#FFBDBDBD`) with 50% alpha icon.

2. **Restart** — Rounded-rect button (72×72dp) with **Amber** (`#FFFFAB00`) background and `refresh` icon. Only enabled when PicoClaw is running.

### Status Tiles
- **PicoClaw Status Tile**: Full-width card with a large status dot (16dp) and bold state text. Background is a subtle gradient that shifts:
  - Stopped → cool gray (`#FFECEFF1`)
  - Starting → warm amber (`#FFFFF3E0`)
  - Running → cool green (`#FFE8F5E9`)
  - Error → soft red (`#FFFFEBEE`)

- **Bootstrap Tile**: Compact card showing the current bootstrap phase with a thick, Material You-style linear progress indicator (8dp height) in `primary` color.

- **Server / Network Tile**: Compact card showing:
  - Large bold port number (e.g., **8080** in 24sp `headlineSmall`).
  - A 4-bar signal-strength-style health indicator.
  - "HEALTHY" / "UNHEALTHY" label.

### Secondary Actions
- **Mission Control**: Opens the web dashboard in an **embedded `WebView`** inside a full-screen bottom sheet (or `Activity` with a `WebView`). This fixes the "raw HTML" bug by controlling the rendering context. The button is a wide rounded-rect tile with a `dashboard` icon.
- **View Logs**: Opens a **peekable bottom sheet** (`BottomSheetDialogFragment`) that streams the latest log lines. The button is a wide rounded-rect tile with a `description` icon.

### Pull-to-Refresh
The entire content is wrapped in `androidx.swiperefreshlayout.widget.SwipeRefreshLayout`. Swiping down triggers a status refresh, plays a light haptic tick, and updates all tiles.

---

## Component Choices

| UI Element | Component | Rationale |
|------------|-----------|-----------|
| Root container | `SwipeRefreshLayout` | Standard pull-to-refresh gesture; familiar to Android users. |
| Content scroll | `NestedScrollView` | Allows content to scroll on small screens while supporting swipe-to-refresh. |
| Layout grid | `ConstraintLayout` (root) + `GridLayout` (tiles) | Flat hierarchy, responsive to screen width, easy to align. |
| Tiles/Cards | `MaterialCardView` (8dp radius, 2dp elevation) | Provides depth, consistent Material 3 styling, ripple feedback. |
| Primary toggles | Custom `MaterialCardView` circles (72dp) with `ImageView` center | Mimics physical toggle switches; large hit target (well above 48dp min). |
| Progress indicator | `LinearProgressIndicator` (8dp thick, `app:trackThickness="8dp"`) | Highly visible, Material You compliant, supports indeterminate + determinate modes. |
| Signal bars | Custom `LinearLayout` with 4 `View`s of increasing height | Lightweight, no custom view needed, easy to tint dynamically. |
| Bottom sheet (logs) | `BottomSheetDialogFragment` with `RecyclerView` | Standard Material pattern; peekable, scrollable, dismissible with swipe. |
| Web dashboard | `WebView` inside `BottomSheetDialogFragment` or new `Activity` | Prevents external browser from showing raw HTML; keeps user in-app. |
| Typography | Material 3 type scale (`displaySmall`, `headlineSmall`, `titleLarge`, `bodyLarge`) | Ensures readability, proper contrast, and future-proofing for dynamic type. |

---

## Accessibility Considerations
- Every interactive element has a content description (`android:contentDescription`).
- Status colors are paired with text labels (not color-only communication).
- Minimum touch target is **64dp** for all buttons.
- Bottom sheet has a drag handle (`@id/bottom_sheet_handle`) for motor-impaired users.
- `SwipeRefreshLayout` has a `contentDescription` indicating "Pull down to refresh status."

---

## State Mapping

| App State | Background Gradient | Start Btn | Stop Btn | Restart Btn | Status Text |
|-----------|---------------------|-----------|----------|-------------|-------------|
| Bootstrap in progress | Gray-Blue gradient | Disabled | Disabled | Disabled | "Setting up…" |
| Stopped | Cool gray | Enabled (green) | Disabled (gray) | Disabled (gray) | "Stopped" |
| Starting | Warm amber | Disabled (gray) | Disabled (gray) | Disabled (gray) | "Starting…" |
| Running | Cool green | Disabled (gray) | Enabled (red) | Enabled (amber) | "Running" |
| Error | Soft red | Enabled (green) | Disabled (gray) | Disabled (gray) | "Error: …" |

---

## Responsive Behavior
- On phones (< 360dp width): Tiles stack vertically, primary actions in a single row.
- On tablets / large screens (> 600dp width): Tiles arrange in a 2-column grid; primary actions scale to 96dp.
- Dark theme: Gradients invert to darker variants; tile backgrounds use `surfaceVariant`.

---

## Open Questions for Engineering
1. Should the log bottom sheet auto-scroll to the bottom on new lines?
2. Should the WebView dashboard cache its state when the sheet is dismissed?
3. Do we want a persistent notification when PicoClaw is running (outside this screen scope, but related)?
