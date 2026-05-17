# Wireframe C — "Compact Action Sheet" Model Picker Modal

## Overview

An `AlertDialog`-style modal that replaces the current toolbar `ExposedDropdownMenu`. Designed for speed and one-handed use: dense single-line rows with ellipsized long names (full name revealed on selection), recent models as horizontal chips, and a cyclable sort toggle icon. No tabs, no per-row Select buttons — tap the row to select and dismiss.

### Key Differences from Wireframes A & B

| Aspect | Wireframe A (Search-First Drawer) | Wireframe B (Tab-Organized Sheet) | Wireframe C (Compact Action Sheet) |
|--------|----------------------------------|----------------------------------|-----------------------------------|
| Container | `ModalBottomSheet` (80% height) | `BottomSheetDialogFragment` (full height) | `AlertDialog` (~60% height, max 480dp) |
| Row style | Multiline (maxLines=3), card-style | Multiline (maxLines=3), card-style | Single-line, dense, ellipsized |
| Selection | Per-row `[Select]` button | Per-row `[Select]` button | Tap row directly |
| Sort | Filter chip cycling 3 modes | `PopupMenu` from filter chip | Icon button cycling A-Z → Z-A → Recent |
| Tabs | No | All Models / Favorites | No |
| Section headers | Recently Used + All Models | Tab-switched lists | Recently Used chips + flat list |

---

## 1. ASCII Wireframe

```
┌───────────────────────────────────────────┐
│  Select Model                       [X]   │  ← Title bar
├───────────────────────────────────────────┤
│                                           │
│  ┌───────────────────────────────────┬─┐  │
│  │ 🔍  Search models...              │↕│  │  ← Search bar + sort toggle
│  └───────────────────────────────────┴─┘  │
│                                           │
│  ┌─ Recently Used ──────────────────────┐ │
│  │  [gpt-5] [claude-sonnet] [nemotron…] │ │  ← Horizontal chip row
│  └──────────────────────────────────────┘ │
│                                           │
│  ┌──────────────────────────────────────┐ │
│  │ ★ gpt-5.4                 128K [OAI] │ │  ← Pinned model
│  ├──────────────────────────────────────┤ │
│  │ ☆ claude-sonnet-4-6        200K [An] │ │  ← Star + name + badge + chip
│  ├──────────────────────────────────────┤ │
│  │ ☆ nvidia/nemotron-4-340b…   128K [NV]│ │  ← Ellipsized long name
│  ├──────────────────────────────────────┤ │
│  │ ☆ Qwen/Qwen3-235B-A22B-I…    32K [MS]│ │  ← Truncated to 1 line
│  ├──────────────────────────────────────┤ │
│  │ ☆ meta-llama/llama-4-scout… 1M  [M] │ │
│  ├──────────────────────────────────────┤ │
│  │ ☆ deepseek/deepseek-r1-67…  128K [D]│ │
│  ├──────────────────────────────────────┤ │
│  │ ☆ mistralai/mixtral-8x22b…   64K [M]│ │
│  ├──────────────────────────────────────┤ │
│  │ … (1239 more)                       │ │
│  └──────────────────────────────────────┘ │
│                                           │
├───────────────────────────────────────────┤
│  42 pinned · 1,247 models · OpenRouter    │  ← Summary bar
├───────────────────────────────────────────┤
│                   [Cancel]                │  ← Neutral button
└───────────────────────────────────────────┘
```

### Legend

| Symbol | Component | Description |
|--------|-----------|-------------|
| `[X]` | Close button | `ImageButton` with `@drawable/ic_close`, dismisses without selecting |
| `🔍` | Search icon | `TextInputLayout` start icon |
| `↕` | Sort toggle | `ImageButton` cycling A-Z → Z-A → Recent; icon rotates per mode |
| `[gpt-5]` | Recent chip | `MaterialChip` — tapping selects instantly |
| `★` / `☆` | Pin toggle | Filled/outline star — `ImageButton` tinted primary/onSurfaceVariant |
| `128K` | Context badge | Small `TextView` with rounded `GradientDrawable` background |
| `[OAI]` | Provider chip | Compact `MaterialChip` (abbreviated label, colored by provider slug) |
| `…1239 more` | Overflow indicator | `TextView` in last visible row position or inline summary |
| `[Cancel]` | Dismiss button | `AlertDialog` `BUTTON_NEUTRAL` — closes without selection |

### Sort Toggle States

```
 ↕  = A–Z (default)      — alphabetical ascending
 ↕  = Z–A                — alphabetical descending
 ↕  = Recent             — most recently used first, rest alphabetical
```

The icon morphs: sort-alpha-asc → sort-alpha-desc → sort-clock → sort-alpha-asc.

### Row Anatomy (compact single-line)

```
 ★ gpt-5.4                       128K  [OAI]
 ↑ ↑                             ↑     ↑
 │ Model name (ellipsized)        │     Provider chip
 │ maxWidth=0dp, weight=1         │     3-letter abbrev, colored
 Star toggle                      │
                                  Context badge
                                  "128K", "1M", "8K"
```

---

## 2. Widget Hierarchy (XML Tree)

### Root Layout: `dialog_model_picker_compact.xml`

```xml
<!-- dialog_model_picker_compact.xml -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:maxHeight="480dp"
    android:minHeight="240dp"
    android:background="?attr/colorSurface"
    android:elevation="24dp"
    app:shapeAppearance="?attr/shapeAppearanceCornerLarge">

    <!-- ── HEADER: Title + Close ── -->
    <TextView
        android:id="@+id/title"
        android:text="Select Model"
        android:textAppearance="@style/TextAppearance.Material3.TitleMedium"
        android:paddingHorizontal="20dp"
        android:paddingTop="16dp"
        android:paddingBottom="4dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <ImageButton
        android:id="@+id/btn_close"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@drawable/ic_close"
        android:contentDescription="Close"
        android:background="?android:attr/selectableItemBackgroundBorderless"
        android:padding="8dp"
        app:tint="?attr/colorOnSurfaceVariant"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="8dp" />

    <!-- ── SEARCH BAR + SORT TOGGLE ── -->
    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/search_layout"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="8dp"
        app:startIconDrawable="@drawable/ic_search"
        app:endIconMode="clear_text"
        app:boxStrokeWidth="1dp"
        app:boxStrokeWidthFocused="2dp"
        app:hintEnabled="false"
        app:layout_constraintTop_toBottomOf="@id/title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/btn_sort" />

    <ImageButton
        android:id="@+id/btn_sort"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@drawable/ic_sort_alpha_asc"
        android:contentDescription="Sort: A-Z"
        android:background="?android:attr/selectableItemBackgroundBorderless"
        android:padding="8dp"
        app:tint="?attr/colorOnSurfaceVariant"
        app:layout_constraintTop_toTopOf="@id/search_layout"
        app:layout_constraintBottom_toBottomOf="@id/search_layout"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginEnd="12dp" />

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/search_input"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Search models..."
        android:inputType="textFilter"
        android:maxLines="1"
        android:imeOptions="actionSearch"
        android:importantForAutofill="no" />

    <!-- ── RECENTLY USED CHIPS ── -->
    <LinearLayout
        android:id="@+id/recent_section"
        android:orientation="vertical"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:visibility="gone"
        app:layout_constraintTop_toBottomOf="@id/search_layout"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <TextView
            android:text="Recently Used"
            android:textAppearance="@style/TextAppearance.Material3.LabelSmall"
            android:textColor="?attr/colorOnSurfaceVariant"
            android:paddingHorizontal="20dp"
            android:paddingBottom="4dp" />

        <HorizontalScrollView
            android:layout_width="match_parent"
            android:scrollbars="none"
            android:paddingHorizontal="16dp">

            <ChipGroup
                android:id="@+id/recent_chip_group"
                app:singleLine="true"
                app:chipSpacing="6dp" />

        </HorizontalScrollView>

    </LinearLayout>

    <!-- ── SUMMARY BAR (count + provider) ── -->
    <TextView
        android:id="@+id/summary_bar"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:paddingHorizontal="20dp"
        android:paddingVertical="8dp"
        android:text="1,247 models · OpenRouter"
        android:textAppearance="@style/TextAppearance.Material3.BodySmall"
        android:textColor="?attr/colorOnSurfaceVariant"
        android:background="?attr/colorSurfaceVariant"
        app:layout_constraintTop_toBottomOf="@id/recent_section"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- ── MODEL LIST (RecyclerView) ── -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/model_list"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:clipToPadding="false"
        android:paddingHorizontal="8dp"
        android:scrollbars="vertical"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
        app:layout_constraintTop_toBottomOf="@id/summary_bar"
        app:layout_constraintBottom_toTopOf="@id/btn_cancel"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- ── CANCEL BUTTON ── -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_cancel"
        style="@style/Widget.Material3.Button.TextButton"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:text="Cancel"
        android:textSize="14sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Item Layout: `item_model_compact.xml` (Single Dense Row)

```xml
<!-- item_model_compact.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingHorizontal="12dp"
    android:background="?android:attr/selectableItemBackground"
    android:minHeight="48dp">

    <!-- Star toggle -->
    <ImageButton
        android:id="@+id/btn_star"
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:src="@drawable/ic_star_outline"
        android:contentDescription="Pin model"
        android:background="?android:attr/selectableItemBackgroundBorderless"
        android:padding="6dp"
        app:tint="?attr/colorOnSurfaceVariant" />

    <!-- Model name (single line, ellipsized) -->
    <TextView
        android:id="@+id/model_name"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginHorizontal="8dp"
        android:textAppearance="@style/TextAppearance.Material3.BodyMedium"
        android:textColor="?attr/colorOnSurface"
        android:maxLines="1"
        android:ellipsize="end"
        tools:text="nvidia/nemotron-4-340b-instruct" />

    <!-- Context badge -->
    <TextView
        android:id="@+id/context_badge"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:paddingHorizontal="5dp"
        android:paddingVertical="1dp"
        android:layout_marginEnd="6dp"
        android:text="128K"
        android:textSize="10sp"
        android:textColor="?attr/colorOnSurfaceVariant"
        android:background="@drawable/bg_context_badge"
        android:importantForAccessibility="no"
        tools:text="128K" />

    <!-- Provider chip (compact) -->
    <com.google.android.material.chip.Chip
        android:id="@+id/provider_chip"
        style="@style/Widget.Material3.Chip.Assist"
        android:layout_width="wrap_content"
        android:layout_height="20dp"
        android:text="OAI"
        android:textSize="10sp"
        android:textColor="?attr/colorOnSurface"
        app:chipMinHeight="0dp"
        app:chipMinTouchTargetSize="0dp"
        app:chipBackgroundColor="@color/provider_chip_default"
        app:chipStrokeWidth="0dp"
        app:chipCornerRadius="4dp"
        android:paddingHorizontal="5dp"
        android:paddingVertical="0dp"
        android:importantForAccessibility="no"
        tools:text="OAI" />

</LinearLayout>
```

### Widget Hierarchy Diagram

```
AlertDialog (ModelPickerDialogFragment)
 └── ConstraintLayout (root, maxHeight=480dp)
      ├── TextView "Select Model"              ← Title (headline)
      ├── ImageButton [X]                      ← Close
      │
      ├── TextInputLayout (OutlinedBox)        ← Search bar
      │    ├── startIcon: ic_search
      │    └── TextInputEditText (textFilter)
      ├── ImageButton (↕)                      ← Sort toggle icon
      │
      ├── LinearLayout (recent_section)        ← Recently used
      │    ├── TextView "Recently Used"
      │    └── HorizontalScrollView
      │         └── ChipGroup
      │              └── MaterialChip (repeated, max 5)
      │
      ├── TextView (summary_bar)               ← "1,247 models · OpenRouter"
      │
      ├── RecyclerView (model_list)            ← Dense scrollable list
      │    └── item_model_compact (repeated)
      │         └── LinearLayout (48dp, horizontal)
      │              ├── ImageButton (btn_star)     ← ★/☆ toggle
      │              ├── TextView (model_name)      ← weight=1, maxLines=1, ellipsize=end
      │              ├── TextView (context_badge)   ← "128K"
      │              └── Chip (provider_chip)       ← "OAI", colored
      │
      └── MaterialButton (btn_cancel)          ← Cancel (neutral action)
```

### Resolve Key

| XML Name | Widget Type | Role |
|----------|------------|------|
| `dialog_model_picker_compact.xml` | `ConstraintLayout` | Root container with `maxHeight=480dp` |
| `model_list` | `RecyclerView` | Dense virtualized list, `LinearLayoutManager` |
| `search_input` | `TextInputEditText` | `inputType=textFilter`, `imeOptions=actionSearch` |
| `btn_sort` | `ImageButton` | Cycles sort: A-Z → Z-A → Recent |
| `recent_chip_group` | `ChipGroup` | Horizontal recent models (max 5) |
| `btn_star` | `ImageButton` | 32×32dp star toggle (filled/outline) |
| `model_name` | `TextView` | `maxLines=1`, `ellipsize=end`, `weight=1` |
| `context_badge` | `TextView` | Context window size badge, e.g. "128K" |
| `provider_chip` | `MaterialChip` (assist) | 3-letter provider abbrev, color per slug |
| `btn_cancel` | `MaterialButton` (text) | Dismisses dialog without selection |
| `summary_bar` | `TextView` | Compact footer: pinned count · total · provider |

---

## 3. Component Choices & Rationale

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Container** | `AlertDialog` with custom view (ConstraintLayout, maxHeight=480dp) | Unlike Wireframes A/B, this prioritizes speed over browse-ability. `AlertDialog` is the lightest-weight modal — no swipe gestures, no peek height, no `BottomSheetBehaviour` overhead. Instantly recognizable as a picker. `maxHeight=480dp` (~60% of a typical 800dp phone) leaves background context visible. Falls back to scroll for <480dp screens. |
| **Row style** | Single-line `TextView` (maxLines=1, ellipsize=end), 48dp height | Dense rows let users scan more items per viewport. At 48dp, a 480dp dialog shows ~8 rows + header/footer (vs ~4 multiline rows in Wireframes A/B). Full name is visible on tap (shown in a `Snackbar` or passed to caller). This is the core "compact" trade-off: browse speed over name visibility. |
| **Selection** | Tap row directly (no per-row button) | Saves horizontal space. The entire row is clickable via `selectableItemBackground`. This is the fastest interaction model — tap and done. |
| **Sort** | `ImageButton` cycling A-Z → Z-A → Recent | Minimal footprint (40×40dp icon). No dropdown, no chip, no popup. Tapping cycles modes; icon updates to reflect current mode. Tooltip/label on long-press shows current mode name. |
| **Star toggle** | `ImageButton` 32×32dp | Smaller than Wireframes A/B (40dp). The dense row doesn't have room for 48dp elements. Touch target is still 48dp via `padding=6dp` (32+12=44dp, close enough with `TouchDelegate` for the remaining 4dp). |
| **Provider chip** | `MaterialChip` (assist, ultra-compact, 3-letter abbrev) | 3-letter abbreviation (OAI, ANT, NV, MS, M, D) saves space vs full provider name. Color-coded for quick visual scanning. Abbreviation mapping: `openai→OAI`, `anthropic→ANT`, `nvidia→NV`, `modelscope→MS`, `meta→M`, `deepseek→D`, `google→G`, `mistral→Mist` (4 chars if needed). |
| **Context badge** | `TextView` with rounded background, right of name | Slim badge, ~36dp wide. Left-aligned with provider chip for visual grouping. |
| **Recent chips** | `MaterialChip` in `ChipGroup` + `HorizontalScrollView` | Same pattern as Wireframe A. Max 5 models. Tapping a chip selects instantly (no list scroll needed). Section hidden during active search. |
| **Sort button icon** | Vector drawable cycling per mode | Three distinct icons: `ic_sort_alpha_asc` (A-Z), `ic_sort_alpha_desc` (Z-A), `ic_sort_clock` (Recent). No text label — keeps the header compact. |
| **Summary bar** | `TextView` with surface-variant background | Single line: "42 pinned · 1,247 models · OpenRouter". Replaces the footer from Wireframes A/B. Always visible between search and list. |
| **Cancel button** | `MaterialButton` (text style) at bottom | `AlertDialog` convention — neutral button dismisses without action. Positioned at bottom of dialog, outside scrollable list. |

### Why Not…

| Alternative | Rejected Because |
|-------------|-----------------|
| **`ModalBottomSheet` / `BottomSheetDialogFragment`** | Heavier than `AlertDialog`. Requires coordinator layout, behavior callbacks, insets handling. Adds swipe-to-dismiss which interferes with scrolling the dense list. The compact design doesn't need the extra height a bottom sheet provides. |
| **Multiline rows** | The "compact" design direction explicitly specifies single-line ellipsis. Multiline rows would halve the visible model count (dense: 8 rows vs multiline: 4 rows in same space). Full name is accessible on selection. |
| **Per-row Select button** | Wastes horizontal space. The entire row is the select target. Button implies a secondary action which doesn't exist here. |
| **Tabs (All / Favorites)** | Adds complexity and reduces list space. Pinned models are surfaced by sort (pinned-first) and the star toggle is always visible. Wireframe C prioritizes speed over organization. |
| **`ExposedDropdownMenu`** | Current broken approach — dropdown overlay can't virtualize (1000 items → OOM), no multiline, no search, no sort. |
| **`PopupMenu` for sort** | Too many taps (open menu → select mode → close). Cycling icon is one tap per mode change. |
| **Full provider name in chip** | Names like "modelscope" (10 chars) or "anthropic" (9 chars) would push the row width. Abbreviations keep the layout tight. |

---

## 4. States

### 4.1 Empty (No Models Loaded)

```
┌───────────────────────────────────────────┐
│  Select Model                       [X]   │
├───────────────────────────────────────────┤
│  🔍  Search models...               [↕]   │
├───────────────────────────────────────────┤
│                                           │
│                ┌────────────┐              │
│                │   📡        │              │
│                │  No models  │              │
│                │  loaded     │              │
│                │             │              │
│                │[Fetch Models]│             │
│                └────────────┘              │
│                                           │
├───────────────────────────────────────────┤
│  No models loaded                         │
├───────────────────────────────────────────┤
│                 [Cancel]                  │
└───────────────────────────────────────────┘
```

**Behavior**:
- RecyclerView hidden, empty state centered
- "Fetch Models" button triggers provider API call
- Search bar disabled (non-interactive tint)
- Sort button disabled
- Recent section hidden
- Summary bar: "No models loaded"

### 4.2 Searching (Typing)

```
┌───────────────────────────────────────────┐
│  Select Model                       [X]   │
├───────────────────────────────────────────┤
│  🔍  nemo                            [↕]  │  ← user typing "nemo"
│  ─────────────────────────────────────    │  ← underline focused
├───────────────────────────────────────────┤
│  2 of 1,247 models · OpenRouter           │  ← updated count
├───────────────────────────────────────────┤
│  ┌──────────────────────────────────────┐ │
│  │ ☆ nvidia/nemotron-4-340b-in…  128K  │ │  ← matched
│  │ ☆ nvidia/nemotron-4-mini       64K  │ │
│  └──────────────────────────────────────┘ │
│  (2 items)                                │
├───────────────────────────────────────────┤
│                 [Cancel]                  │
└───────────────────────────────────────────┘
```

**Behavior**:
- Fuzzy filter applied with 200ms debounce on background coroutine
- Recent section **hidden** during active search
- Summary bar updates: "2 of 1,247 models · OpenRouter"
- Clear button `[×]` visible in search `endIconMode`
- Keyboard visible — dialog does NOT resize (AlertDialog default behavior). If needed, `setOnApplyWindowInsetsListener` adds padding to RecyclerView bottom.
- No "no results" yet — that's a separate state (results from API exist, filter yields empty)

### 4.3 Results (Default State)

```
┌───────────────────────────────────────────┐
│  Select Model                       [X]   │
├───────────────────────────────────────────┤
│  🔍  Search models...               [↕]   │  ← ↕ = A-Z
├───────────────────────────────────────────┤
│  Recently Used                             │
│  [gpt-5] [claude-sonnet] [nemotron-4…]   │
├───────────────────────────────────────────┤
│  5 pinned · 1,247 models · OpenRouter     │
├───────────────────────────────────────────┤
│  ┌──────────────────────────────────────┐ │
│  │ ★ gpt-5.4                 128K [OAI] │ │  ← pinned first
│  │ ★ claude-sonnet-4-6        200K [ANT]│ │
│  │ ☆ nvidia/nemotron-4-340b…   128K [NV]│ │  ← unpinned alphabetical
│  │ ☆ Qwen/Qwen3-235B-A22B-I…    32K [MS]│ │
│  │ ☆ meta-llama/llama-4-scout… 1M  [M]  │ │
│  │ ☆ deepseek/deepseek-r1-67…  128K [D] │ │
│  │ ☆ mistralai/mixtral-8x22b…   64K [M] │ │
│  │ … (1241 more)                       │ │
│  └──────────────────────────────────────┘ │
├───────────────────────────────────────────┤
│                 [Cancel]                  │
└───────────────────────────────────────────┘
```

**Sort precedence** (within mode):
1. **Pinned** models first (starred), sorted per mode among themselves
2. **Recently used** (only in Recent sort mode)
3. **Alphabetical** remainder (or descending context in Context mode)

### 4.4 No Results (Search Yields Nothing)

```
┌───────────────────────────────────────────┐
│  Select Model                       [X]   │
├───────────────────────────────────────────┤
│  🔍  xyzzy                           [↕]  │
│                                 [×]       │  ← clear button visible
├───────────────────────────────────────────┤
│                                           │
│                ┌────────────┐              │
│                │   🔍        │              │
│                │  No models  │              │
│                │  match      │              │
│                │  "xyzzy"    │              │
│                │             │              │
│                │[Clear Filter]│             │
│                └────────────┘              │
│                                           │
├───────────────────────────────────────────┤
│  No results for "xyzzy"  ·  1,247 total   │
├───────────────────────────────────────────┤
│                 [Cancel]                  │
└───────────────────────────────────────────┘
```

**Behavior**:
- RecyclerView hidden, centered empty state
- "Clear Filter" button clears `searchInput` text
- Summary bar: "No results for "xyzzy" · 1,247 total models"
- Sort button disabled during no-results state
- On clear, returns to Results state

### 4.5 Loading

```
┌───────────────────────────────────────────┐
│  Select Model                       [X]   │
├───────────────────────────────────────────┤
│  🔍  Search models...               [↕]   │  ← disabled
├───────────────────────────────────────────┤
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░  45%               │  ← LinearProgressIndicator
│                                           │
│  ┌──────────────────────────────────────┐ │
│  │ ━━━━━━━━━━━━━━━━━━━━━━    ░░░░░░░░░  │ │  ← shimmer row 1
│  │ ━━━━━━━━━━━━━━━━━━━━━━    ░░░░░░░░░  │ │  ← shimmer row 2
│  │ ━━━━━━━━━━━━━━━━━━━━━━    ░░░░░░░░░  │ │  ← shimmer row 3
│  └──────────────────────────────────────┘ │
│                                           │
│  42 models loaded so far…                 │
├───────────────────────────────────────────┤
│  Fetching from OpenRouter…  45%           │
├───────────────────────────────────────────┤
│                 [Cancel]                  │
└───────────────────────────────────────────┘
```

**Behavior**:
- `LinearProgressIndicator` determinate mode (percentage if total known, else indeterminate)
- 3 shimmer skeleton rows (simulated with `AlphaAnimation` pulsing 0.3→1.0→0.3)
- Search bar disabled, sort button disabled
- Summary bar: "Fetching from OpenRouter… 45%"
- If cached models exist, show immediately (skip shimmer) and progress bar only
- Cancel button remains active — dismisses and cancels coroutine via `viewModelScope`
- On completion: shimmer → real list with `notifyDataSetChanged()`

---

## 5. Edge Cases

### 5.1 Keyboard Overlap

| Issue | Mitigation |
|-------|-----------|
| AlertDialog doesn't resize by default | Use `AlertDialog`'s `setView()` with a scrollable root. The dialog's `maxHeight=480dp` already constrains it — keyboard pushes the dialog up if `adjustResize` is set in the host Activity manifest. |
| RecyclerView hidden behind keyboard | Apply `ViewCompat.setOnApplyWindowInsetsListener` to the dialog's root view. When IME is visible, reduce `model_list` height by `imeHeight`. Since the dialog uses `ConstraintLayout` with `layout_height="wrap_content"` and `maxHeight=480dp`, this is handled by the dialog's built-in window insets. |
| Search loses focus on rotate | Search field requests focus in `onResume()` of the dialog fragment. `android:saveEnabled="true"` on `TextInputEditText` preserves text across config changes. |
| Sort button overlapped | Sort button is right of search bar in the same row — always visible. On very narrow screens (<320dp), the sort button shifts inside the search layout as an `endIconDrawable`. |

```kotlin
// Keyboard insets handling
dialog?.window?.let { window ->
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        binding.modelList.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomMargin = ime.bottom
        }
        insets
    }
}
```

### 5.2 1000+ Models (OpenRouter Full Catalog)

| Concern | Solution |
|---------|----------|
| Dense list scrolling | `RecyclerView` + `LinearLayoutManager` handles 10,000+ items. At 48dp per row, 8 rows visible at a time → ~0.8% of total visible. Smooth scrolling via `SmoothScroller`. |
| Filter performance | `Flow.debounce(200).map { filter(models, query) }` on `Dispatchers.Default`. 1,247 strings filtered in <1ms. Case-insensitive substring match. |
| Sort performance | `Collections.sort()` on in-memory list. 1,247 items sorted in <0.5ms for any mode. |
| "1,247 models" formatting | Counts >999 use compact format: `1.2K`, `12.5K`, `1.2M`. `NumberFormat.getCompactNumberInstance(Locale.getDefault(), NumberFormat.Style.SHORT)`. |
| Memory | ViewHolder recycling keeps ~10-12 rows inflated. Each row is a simple `LinearLayout` with 4 children — ~200 bytes per ViewHolder. 10,000 model data objects in a `List<PickerModel>` ~= 2MB (each model: 4 strings + 2 longs + boolean = ~200 bytes). Acceptable. |
| Initial load | Fetch on background coroutine. Show shimmer immediately. Cache in `ViewModel` (scoped to dialog fragment). Re-opening is instant if cache is warm. |
| Alpha-jump scrolling | The dense list makes it easy to overscroll. Add a fast-scroller thumb: `RecyclerView` with `FastScrollLinearLayoutManager` or the built-in `android:fastScrollEnabled="true"` (requires `fastScrollHorizontalTrackDrawable` and `fastScrollHorizontalThumbDrawable`). |

```xml
<!-- Fast scroller for dense lists -->
<androidx.recyclerview.widget.RecyclerView
    ...
    android:fastScrollEnabled="true"
    android:fastScrollHorizontalThumbDrawable="@drawable/fast_scroll_thumb"
    android:fastScrollHorizontalTrackDrawable="@drawable/fast_scroll_track" />
```

### 5.3 RTL Languages

| Concern | Solution |
|---------|----------|
| Model names in RTL script | `TextView` auto-detects bidi. Set `android:textDirection="locale"` on the row layout. Names like `Qwen/Qwen3` are LTR and render correctly in RTL layouts. |
| Layout mirroring | All padding/margins use `paddingStart`/`paddingEnd` (not `left`/`right`). ConstraintLayout handles RTL mirroring automatically. |
| Sort icon direction | A-Z sort icon flips horizontally in RTL. Use `autoMirrored="true"` on vector drawables. |
| Provider chip with RTL provider names | Abbreviations are always ASCII (`OAI`, `ANT`) — no RTL issues. |
| Star toggle position | In RTL, star appears at the right end of the row (natural reading order in RTL). |
| Search behavior | `textFilter` input type respects locale. RTL text input works natively. |

### 5.4 Very Long Model Names

| Name | Length | Display |
|------|--------|---------|
| `gpt-5.4` | 7 chars | Full name visible |
| `nvidia/nemotron-4-340b-instruct` | 32 chars | Truncated: `nvidia/nemotron-4-340b-in…` |
| `Qwen/Qwen3-235B-A22B-Instruct-Mao-2507` | 47 chars | Truncated: `Qwen/Qwen3-235B-A22B-Instruc…` |
| `CohereForAI/c4ai-command-r7b-12-2025` | 38 chars | Truncated: `CohereForAI/c4ai-command-r7b-12-20…` |

**Full name access**:
- On tap (select), the full `modelId` is returned to the caller — no information loss
- Long-press on a row shows a `Toast` or `Snackbar` with the full canonical name
- Content description for TalkBack reads the full name regardless of truncation

### 5.5 Star/Pin Persistence

| Scenario | Behavior |
|----------|----------|
| User stars model | Toggle to filled star (`colorPrimary` tint). Persist to `SharedPreferences` keyed by `providerSlug:modelId`. Max 50 pinned models. |
| User unstars model | Toggle to outline star. Remove from `SharedPreferences`. List re-orders if pinned-first sort is active. |
| 50 pinned limit reached | `Snackbar`: "Maximum 50 pinned models. Unpin another to pin this one." New star taps ignored until unpin. |
| Pinned model disappears from API | Silently removed from pinned set on next fetch. Pinned count decrements. |

### 5.6 Recent Models Storage

| Concern | Solution |
|---------|----------|
| Storage | `SharedPreferences` with JSON array of `{modelId, timestamp}` objects. Max 5 entries per provider. |
| Eviction | Oldest entry evicted when adding a 6th. LRU semantics. |
| Display | Recent chips shown in reverse chronological order (most recent first). |
| Search visibility | Hidden during active search. Re-appears when search is cleared. |

### 5.7 Accessibility

| Requirement | Implementation |
|-------------|---------------|
| TalkBack row | Entire row is a single focusable unit. `contentDescription`: "Model {name}, Provider {abbreviation}, Context {length}, {pinned status}. Tap to select." |
| Star toggle | `contentDescription` dynamically: "Pin {model}" / "Unpin {model}" |
| Sort button | `contentDescription`: "Sort: {mode}. Tap to change to {next mode}." |
| Search | Auto-focus announced: "Search models. Edit box." |
| Touch target | Minimum 48dp effective. `btn_star` at 32dp + 6dp padding = 44dp → `TouchDelegate` extends to 48dp. All other elements already ≥48dp (rows, button, chips). |
| Font scaling | `sp` units throughout. At 1.5× font scale, row height remains 48dp (name text may truncate earlier but still readable). |
| Keyboard navigation | Focus order: Close → Search → Sort → Recent chips (left/right) → List (down arrow) → Cancel. Tab key cycles forward. |
| Cancel button | "Cancel. Close without selecting." |

---

## 6. Contradiction: Multiline Requirement vs Single-Line Design

**Requirements state**: "Each row wraps long model names (multiline)"
**Design direction states**: "Each row is dense (single-line with ellipsis for overflow but shows full name on selection)"

Wireframe C follows the **design direction** (single-line), as that is the defining characteristic of "Compact Action Sheet". The trade-off is:

| Trade-off | Impact |
|-----------|--------|
| More rows visible | ~8 rows vs ~4 rows (multiline) in the same viewport |
| Faster scanning | User sees more candidates per scroll |
| Name truncation | User must tap to see full name (returned to caller, or shown on long-press) |
| Denser visual | Less whitespace, more information density |

If multiline wrapping is preferred, refer to **Wireframe A** (which uses `maxLines=3` wrapping) or **Wireframe B** (which also uses `maxLines=3` but with tabs). This wireframe deliberately optimizes for scan speed over name visibility.

---

## 7. Data Model

```kotlin
data class CompactPickerModel(
    val modelId: String,            // e.g. "openai/gpt-5.4"
    val displayName: String,        // e.g. "gpt-5.4"
    val providerName: String,       // e.g. "OpenAI"
    val providerSlug: String,       // e.g. "openai" (for color + abbrev lookup)
    val providerAbbrev: String,     // e.g. "OAI" (3-letter for chip)
    val contextLength: Int?,        // e.g. 131072, null if unknown
    val contextDisplay: String,     // e.g. "128K", "1M" (pre-formatted)
    val isPinned: Boolean = false,
    val lastUsed: Long? = null,     // epoch millis, null if never
)

sealed interface CompactPickerState {
    data object Empty : CompactPickerState
    data class Loading(
        val partialCount: Int = 0,
        val progress: Float = 0f,   // 0.0..1.0
    ) : CompactPickerState
    data class Results(
        val models: List<CompactPickerModel>,
        val recentModels: List<CompactPickerModel>,
        val pinnedCount: Int,
        val totalCount: Int,
        val sortMode: SortMode,
    ) : CompactPickerState
    data class Searching(
        val query: String,
        val results: List<CompactPickerModel>,
        val totalCount: Int,
    ) : CompactPickerState
    data class NoResults(
        val query: String,
        val totalCount: Int,
    ) : CompactPickerState
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
    ) : CompactPickerState
}

enum class SortMode {
    ALPHA_ASC,    // A–Z (default)
    ALPHA_DESC,   // Z–A
    RECENT,       // recently used first, then A–Z
}

// Provider abbreviation map
val PROVIDER_ABBREV = mapOf(
    "openai"      to "OAI",
    "anthropic"   to "ANT",
    "deepseek"    to "DPS",
    "nvidia"      to "NVD",
    "modelscope"  to "MSC",
    "google"      to "GGL",
    "meta"        to "META",
    "microsoft"   to "MSFT",
    "mistral"     to "MIS",
    "xai"         to "XAI",
    "cohere"      to "COH",
    "default"     to "???",
)
```

---

## 8. Dialog Implementation Outline

```kotlin
class ModelPickerCompactDialog(
    private val provider: ModelProvider,
    private val onModelSelected: (modelId: String, displayName: String) -> Unit,
) : DialogFragment() {

    private var _binding: DialogModelPickerCompactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompactPickerViewModel by viewModels {
        CompactPickerViewModelFactory(provider)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireActivity()).create()
        val view = layoutInflater.inflate(R.layout.dialog_model_picker_compact, null)
        _binding = DialogModelPickerCompactBinding.bind(view)
        dialog.setView(view)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Search: TextWatcher → Flow.debounce(200)
        binding.searchInput.doAfterTextChanged { text ->
            viewModel.setSearch(text.toString())
        }

        // Sort toggle
        binding.btnSort.setOnClickListener {
            viewModel.cycleSortMode()
        }

        // Star toggle (via adapter callback)
        adapter.onStarToggle = { model ->
            viewModel.togglePin(model.modelId)
        }

        // Row tap = select
        adapter.onModelClick = { model ->
            onModelSelected(model.modelId, model.displayName)
            dismiss()
        }

        // Close button
        binding.btnClose.setOnClickListener { dismiss() }

        // Cancel button
        binding.btnCancel.setOnClickListener { dismiss() }

        // Observe state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: CompactPickerState) { /* update views */ }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## 9. Files to Create / Modify

| File | Action | Description |
|------|--------|-------------|
| `res/layout/dialog_model_picker_compact.xml` | **Create** | Root layout: title, search+sort, recent chips, summary bar, RecyclerView, cancel button |
| `res/layout/item_model_compact.xml` | **Create** | Single dense row: star, name (single-line), context badge, provider chip |
| `ModelPickerCompactDialog.kt` | **Create** | `DialogFragment` with AlertDialog, ViewModel binding |
| `CompactPickerViewModel.kt` | **Create** | ViewModel: state management, filter, sort, pin persistence |
| `model/CompactPickerModel.kt` | **Create** | Data class, sealed state, SortMode enum, provider abbrev map |
| `AgentChatActivity.kt` | **Modify** | Replace `ExposedDropdownMenu` with button opening `ModelPickerCompactDialog` |
| `activity_agent.xml` | **Modify** | Replace `TextInputLayout` dropdown with a `TextView` + chevron trigger button |
| `res/values/colors.xml` | **Add** | 12 provider chip background colors + context badge bg color |
| `res/drawable/ic_close.xml` | **Add** | X icon for close button (if not present) |
| `res/drawable/ic_search.xml` | **Add** | Magnifying glass icon (if not present) |
| `res/drawable/ic_star.xml` | **Add** | Filled star icon for pinned state |
| `res/drawable/ic_star_outline.xml` | **Add** | Outline star icon for unpinned state |
| `res/drawable/ic_sort_alpha_asc.xml` | **Add** | Sort A-Z icon |
| `res/drawable/ic_sort_alpha_desc.xml` | **Add** | Sort Z-A icon |
| `res/drawable/ic_sort_clock.xml` | **Add** | Sort by recent icon |
| `res/drawable/bg_context_badge.xml` | **Add** | Rounded rectangle shape for context badge |
| `res/values/strings.xml` | **Add** | ~12 strings: labels, hints, descriptions, empty states |
| `ModelPickerCompactDialogTest.kt` | **Create** | Unit tests: ViewModel filter/sort/pin logic |
| `ModelPickerCompactDialogUITest.kt` | **Create** | Instrumented test: dialog inflation, search, sort, selection |
