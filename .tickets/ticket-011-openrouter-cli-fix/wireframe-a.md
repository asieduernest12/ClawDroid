# Wireframe A — "Search-First Drawer" Model Picker Modal

## Overview
A full-height `ModalBottomSheet` that replaces the current toolbar `ExposedDropdownMenu` (textSize=13sp). Designed for browsing hundreds of models with long names like `nvidia/nemotron-4-340b-instruct` on device screens as small as 4.7" (API 21 min).

---

## 1. ASCII Wireframe

```
┌──────────────────────────────────────────────────────┐
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Select Model                          [ X ]    │ │  ← Title bar (handle + close)
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  🔍  Search models...                     [Sort▾]│ │  ← Search bar (pinned)
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Recently Used                                   │ │  ← Section header
│  ├──────────────────────────────────────────────────┤ │
│  │  ┌──────┐ ┌────────────┐ ┌──────────────────┐   │ │
│  │  │gpt-5 │ │claude-sonnet│ │nemotron-4-340b…  │   │ │  ← Horizontal chip row
│  │  └──────┘ └────────────┘ └──────────────────┘   │ │
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  All Models  (142)                               │ │  ← Section header
│  ├──────────────────────────────────────────────────┤ │
│  │                                                  │ │
│  │  ┌─────────────────────────────────────────────┐ │ │
│  │  │ ★  gpt-5.4                         128K    │ │ │  ← Pinned model
│  │  │    OpenAI                           [Select]│ │ │
│  │  └─────────────────────────────────────────────┘ │ │
│  │                                                  │ │
│  │  ┌─────────────────────────────────────────────┐ │ │
│  │  │ ☆  claude-sonnet-4-6                 200K   │ │ │  ← Unpinned model
│  │  │    Anthropic                         [Select]│ │ │
│  │  └─────────────────────────────────────────────┘ │ │
│  │                                                  │ │
│  │  ┌─────────────────────────────────────────────┐ │ │
│  │  │ ☆  nvidia/nemotron-4-340b-instruct   128K   │ │ │  ← Long name wraps
│  │  │    NVIDIA                           [Select]│ │ │     to second line
│  │  └─────────────────────────────────────────────┘ │ │
│  │                                                  │ │
│  │  ┌─────────────────────────────────────────────┐ │ │
│  │  │ ☆  Qwen/Qwen3-235B-A22B-Instruct-    128K   │ │ │  ← Another wrapped
│  │  │    2507                                      │ │ │     name example
│  │  │    ModelScope                        [Select]│ │ │
│  │  └─────────────────────────────────────────────┘ │ │
│  │                                                  │ │
│  │  ← RecyclerView (virtually scrolling) →          │ │
│  │                                                  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Showing 142 models  ·  OpenRouter API           │ │  ← Footer (sticky)
│  └──────────────────────────────────────────────────┘ │
│                                                        │
└──────────────────────────────────────────────────────┘
```

### Legend
- `[ X ]` — Close button (Material `IconButton` with `@drawable/ic_close`)
- `🔍` — Search icon (magnifying glass, start of `TextInputLayout`)
- `[Sort▾]` — Sort mode dropdown chip (recent → alphabetical → context length)
- `★` / `☆` — Filled/outline star for pinned/unpinned state
- `128K` — Context length badge (small `MaterialChip` or `TextView` with rounded bg)
- `[Select]` — TextButton trigger to select model and dismiss sheet
- Provider names (OpenAI, Anthropic, etc.) rendered as colored chips with provider-specific hue

---

## 2. Widget Hierarchy (XML Tree)

```xml
<!-- dialog_model_picker.xml -->
<ModalBottomSheet
    android:layout_height="match_parent"
    app:behavior_peekHeight="0dp"
    app:behavior_skipCollapsed="true">

    <CoordinatorLayout>

        <!-- ====== SCROLLABLE CONTENT ====== -->
        <NestedScrollView
            app:layout_behavior="…"
            android:fillViewport="true">

            <LinearLayout
                android:orientation="vertical">

                <!-- ── HEADER ── -->
                <LinearLayout
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <TextView
                        android:text="Select Model"
                        android:textAppearance="@style/TextAppearance.Material3.HeadlineSmall"
                        android:layout_weight="1" />

                    <ImageButton
                        android:src="@drawable/ic_close"
                        android:contentDescription="Close"
                        app:tint="?attr/colorOnSurface" />

                </LinearLayout>

                <!-- ── SEARCH + SORT ── -->
                <LinearLayout
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <com.google.android.material.textfield.TextInputLayout
                        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                        android:layout_weight="1"
                        app:endIconMode="clear_text"
                        app:prefixText="🔍">

                        <com.google.android.material.textfield.TextInputEditText
                            android:hint="Search models…"
                            android:inputType="textFilter"
                            android:maxLines="1"
                            android:imeOptions="actionSearch" />

                    </com.google.android.material.textfield.TextInputLayout>

                    <com.google.android.material.chip.Chip
                        android:id="@+id/sort_chip"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:text="A–Z ▾"
                        app:chipIcon="@drawable/ic_sort" />

                </LinearLayout>

                <!-- ── RECENTLY USED (chip row) ── -->
                <LinearLayout
                    android:orientation="vertical"
                    android:visibility="gone"
                    android:id="@+id/recent_section">

                    <TextView
                        android:text="Recently Used"
                        android:textAppearance="@style/TextAppearance.Material3.TitleSmall"
                        android:paddingHorizontal="16dp"
                        android:paddingTop="12dp"
                        android:paddingBottom="4dp" />

                    <HorizontalScrollView
                        android:layout_width="match_parent"
                        android:scrollbars="none">

                        <ChipGroup
                            android:id="@+id/recent_chip_group"
                            app:singleLine="true"
                            app:chipSpacing="8dp" />

                    </HorizontalScrollView>

                </LinearLayout>

                <!-- ── MODEL LIST ── -->
                <LinearLayout
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/section_header"
                        android:text="All Models  (142)"
                        android:textAppearance="@style/TextAppearance.Material3.TitleSmall"
                        android:paddingHorizontal="16dp"
                        android:paddingTop="12dp"
                        android:paddingBottom="4dp" />

                    <androidx.recyclerview.widget.RecyclerView
                        android:id="@+id/model_list"
                        android:clipToPadding="false"
                        android:paddingBottom="80dp"
                        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

                </LinearLayout>

            </LinearLayout>

        </NestedScrollView>

        <!-- ====== STICKY FOOTER ====== -->
        <TextView
            android:id="@+id/footer"
            android:layout_gravity="bottom"
            android:gravity="center"
            android:padding="12dp"
            android:text="Showing 142 models  ·  OpenRouter API"
            android:textAppearance="@style/TextAppearance.Material3.BodySmall"
            android:background="?attr/colorSurface" />

    </CoordinatorLayout>

</ModalBottomSheet>
```

### `item_model.xml` — Single Model Row

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardElevation="0dp"
    app:strokeWidth="0dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingVertical="8dp"
    android:paddingHorizontal="16dp"
    android:minHeight="64dp"
    android:background="?android:attr/selectableItemBackground">

    <!-- Star / Pin toggle -->
    <ImageButton
        android:id="@+id/btn_pin"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_star_outline"
        android:contentDescription="Pin model"
        app:tint="?attr/colorOnSurfaceVariant" />

    <!-- Text block (wraps) -->
    <LinearLayout
        android:orientation="vertical"
        android:layout_weight="1">

        <!-- Model name (wraps) -->
        <TextView
            android:id="@+id/model_name"
            android:layout_width="match_parent"
            android:textAppearance="@style/TextAppearance.Material3.BodyLarge"
            android:maxLines="3"
            android:lines="3"
            android:ellipsize="end" />

        <!-- Provider chip + context badge -->
        <LinearLayout
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <com.google.android.material.chip.Chip
                android:id="@+id/provider_chip"
                style="@style/Widget.Material3.Chip.Assist"
                android:text="OpenAI"
                app:chipMinHeight="20dp"
                app:chipBackgroundColor="@color/provider_chip_bg"
                app:chipStrokeWidth="0dp"
                android:textSize="11sp"
                android:paddingHorizontal="4dp"
                android:paddingVertical="0dp" />

            <TextView
                android:id="@+id/context_badge"
                android:layout_width="wrap_content"
                android:paddingHorizontal="6dp"
                android:paddingVertical="2dp"
                android:text="128K"
                android:textSize="11sp"
                android:background="@drawable/bg_context_badge"
                android:textColor="?attr/colorOnSurfaceVariant" />

        </LinearLayout>

    </LinearLayout>

    <!-- Select button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_select"
        style="@style/Widget.Material3.Button.TextButton"
        android:text="Select"
        android:contentDescription="Select this model" />

</com.google.android.material.card.MaterialCardView>
```

### Resolve Key

| XML Name | Widget Type | Role |
|----------|------------|------|
| `dialog_model_picker.xml` | `ModalBottomSheet` | Root container, ~80% screen height |
| `model_list` | `RecyclerView` | Virtualized model list, `LinearLayoutManager` |
| `sort_chip` | `MaterialChip` (filter style) | Toggle sort: recent / A–Z / context length |
| `recent_chip_group` | `ChipGroup` | Horizontally scrollable recent selection chips |
| `btn_pin` | `ImageButton` | Star toggle for pinned/favorited models |
| `provider_chip` | `MaterialChip` (assist style) | Provider label with color per provider |
| `context_badge` | `TextView` + `bg_context_badge` | Context length, e.g. "128K" |
| `btn_select` | `MaterialButton` (text style) | Select action → dismiss bottom sheet |

---

## 3. Component Choices & Rationale

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Container** | `ModalBottomSheet` (Material 3) over `AlertDialog` | AlertDialogs block interaction with underlying screen; BottomSheet allows partial transparency, swipe-to-dismiss, and feels less heavy. ~80% height leaves context visible, reducing cognitive load. |
| **Search** | `TextInputLayout` (OutlinedBox) with `textFilter` input type | Fuzzy search needs real-time keystroke filtering. `textFilter` disables autocorrect. `endIconMode="clear_text"` gives one-tap clear. `prefixText` for magnifying glass avoids extra compound drawable. |
| **Sort** | `MaterialChip` (filter style) | Standard Material toggle for sort mode. Tapping cycles: Recent → A–Z → Context Length → Recent. Arrow icon rotates per mode. |
| **Model Name** | `TextView` with `maxLines=3`, `ellipsize=end` | Must wrap long names like `nvidia/nemotron-4-340b-instruct`. 3 lines covers virtually all model names; unlikely to exceed. Single-line ellipsis would hide critical disambiguation. |
| **Provider Chip** | `MaterialChip` (assist style) with per-provider color | Material Chips are designed for labels/tags. `chipMinHeight=20dp` keeps them compact. Colors derived from a 12-color palette keyed by provider slug. |
| **Context Badge** | Plain `TextView` with `GradientDrawable` background | Lighter weight than a full Chip. No ripple, no padding issues. Rounded rect background is simple and recognizable as a badge/pill. |
| **Star/ Pin** | `ImageButton` with tint | Toggle `@drawable/ic_star` (filled) ↔ `@drawable/ic_star_outline`. Tint with `?attr/colorPrimary` when filled, `?attr/colorOnSurfaceVariant` when empty. |
| **Select** | `MaterialButton` (text style) | Lower visual weight than filled button. Only shown on tap of row or explicit press. |
| **List** | `RecyclerView` with `LinearLayoutManager` | Required for >1000 model list (no `ListView` or `ScrollView` with adapter). Built-in view recycling, `DiffUtil` for efficient updates on sort/filter. |
| **Recent Chips** | `ChipGroup` inside `HorizontalScrollView` | Recent models are few (max 5). Chips are tappable for instant selection. Horizontally scrollable if recent list grows. |
| **Footer** | `TextView` pinned to bottom | Always visible summary: count + source. Not part of the scrollable content. |

### Why Not…

- **Why not `AlertDialog`?** Blocks background interaction, fixed width, no keyboard avoidance, feels modal-heavy for a model browser.
- **Why not `BottomSheetDialogFragment` (legacy)?** `ModalBottomSheet` in Material 3 has built-in shape theming, drag handle, and proper `WindowInsets` handling.
- **Why not `AutoCompleteTextView`?** Dropdown overlay has no scrolling virtualisation — 1000 items would OOM. No multiline support.
- **Why not a full `Activity`?** Navigation feels disconnected from the chat screen. Bottom sheet keeps spatial context and avoids Activity lifecycle overhead.

---

## 4. States

### 4.1 Empty (No Models)

```
┌──────────────────────────────────────┐
│  Select Model                [ X ]   │
├──────────────────────────────────────┤
│  🔍  Search models…          [Sort▾] │
├──────────────────────────────────────┤
│                                      │
│           ┌──────────────────┐        │
│           │  📡               │       │
│           │  No models loaded │       │
│           │  Tap "Fetch" to   │       │
│           │  load from API   │       │
│           └──────────────────┘        │
│                                      │
│              [ Fetch Models ]        │
│                                      │
├──────────────────────────────────────┤
│  No models loaded                    │
└──────────────────────────────────────┘
```

**UI behaviour**:
- RecyclerView hidden, empty state card shown
- "Fetch Models" button triggers API call (same as toolbar button in AgentChatActivity)
- Sort chip disabled (grayed out)

### 4.2 Searching (Typing)

```
┌──────────────────────────────────────┐
│  Select Model                [ X ]   │
├──────────────────────────────────────┤
│  🔍  nemo                   [Sort▾] │  ← user typing "nemo"
│     ─────────────────────────────     │  ← underline active
├──────────────────────────────────────┤
│  All Models  (3 of 142)              │
├──────────────────────────────────────┤
│  ┌──────────────────────────────────┐│
│  │ ☆  nvidia/nemotron-4-340b…   128K││  ← matched
│  │    NVIDIA                [Select]││
│  └──────────────────────────────────┘│
│  ┌──────────────────────────────────┐│
│  │ ☆  nvidia/nemotron-4-mini    64K ││  ← matched
│  │    NVIDIA                [Select]││
│  └──────────────────────────────────┘│
├──────────────────────────────────────┤
│  Showing 3 of 142 models            │
└──────────────────────────────────────┘
```

**UI behaviour**:
- Fuzzy filter applied on each keystroke (debounced 200ms)
- Section header updates: `"All Models  (3 of 142)"`
- Footer updates: `"Showing 3 of 142 models"`
- Recently used section **hidden** during active search
- Keyboard visible — sheet adjusts via `WindowInsets` (see §5.1)

### 4.3 Results (Default State)

```
┌──────────────────────────────────────┐
│  Select Model                [ X ]   │
├──────────────────────────────────────┤
│  🔍  Search models…          [Sort▾] │
├──────────────────────────────────────┤
│  Recently Used                       │
│  ┌────────┐ ┌────────────┐          │
│  │gpt-5   │ │claude-sonnet│          │
│  └────────┘ └────────────┘          │
├──────────────────────────────────────┤
│  All Models  (142)                   │
├──────────────────────────────────────┤
│  ┌──────────────────────────────────┐│
│  │ ★  gpt-5.4                 128K  ││  ← pinned first
│  │    OpenAI                [Select]││
│  └──────────────────────────────────┘│
│  ┌──────────────────────────────────┐│
│  │ ☆  claude-sonnet-4-6       200K  ││
│  │    Anthropic              [Select]││
│  └──────────────────────────────────┘│
│  ┌──────────────────────────────────┐│
│  │ ☆  nvidia/nemotron-4-340b… 128K  ││
│  │    NVIDIA                [Select]││
│  └──────────────────────────────────┘│
│  … (scrolling virtual list) …       │
├──────────────────────────────────────┤
│  Showing 142 models  · OpenRouter    │
└──────────────────────────────────────┘
```

**Sorting rules** (within each sort mode):
1. **Pinned models** always appear first (within their sorted position)
2. Then **recently used** (if sort=Recent, these match the chip row)
3. Then **alphabetical** (or by context length descending if that sort mode)

### 4.4 No Results

```
┌──────────────────────────────────────┐
│  Select Model                [ X ]   │
├──────────────────────────────────────┤
│  🔍  xyzzy                  [Sort▾] │
├──────────────────────────────────────┤
│                                      │
│           ┌──────────────────┐        │
│           │  🔍               │       │
│           │  No models match  │       │
│           │  "xyzzy"         │       │
│           │                  │       │
│           │  Try a different │       │
│           │  search term     │       │
│           └──────────────────┘        │
│                                      │
├──────────────────────────────────────┤
│  No matching models                  │
└──────────────────────────────────────┘
```

### 4.5 Loading

```
┌──────────────────────────────────────┐
│  Select Model                [ X ]   │
├──────────────────────────────────────┤
│  🔍  Search models…          [Sort▾] │
├──────────────────────────────────────┤
│  All Models                          │
├──────────────────────────────────────┤
│                                      │
│       ┌────────────────────────┐     │
│       │  ⟳  Loading models…    │     │
│       │  ────────────────────  │     │  ← LinearProgressIndicator
│       │                       │     │
│       └────────────────────────┘     │
│                                      │
│  3 models loaded so far…             │  ← counter (if paginated)
│                                      │
├──────────────────────────────────────┤
│  Fetching models from OpenRouter…    │
└──────────────────────────────────────┘
```

**UI behaviour**:
- Shimmer placeholder items (3-5 skeleton rows) OR determinate progress bar
- "Fetching models from {provider}…" in footer
- Sort chip disabled during load
- If loading from local cache first, show cached results immediately + shimmer for new items

---

## 5. Edge Cases

### 5.1 Keyboard Showing

| Issue | Mitigation |
|-------|-----------|
| Sheet resizes when keyboard opens | Use `STATE_HIDDEN` + `WindowInsetsCompat` to keep sheet from collapsing. Apply `imePadding="true"` on the root `CoordinatorLayout`. The search bar should stay pinned at top, never scrolled off. |
| Keyboard pushes list behind | RecyclerView bottom padding increased by keyboard height via `ViewCompat.setOnApplyWindowInsetsListener`. Footer scrolls with content when keyboard is open. |
| Search loses focus | `TextInputEditText` requests focus on sheet show. `android:windowSoftInputMode="adjustResize"` in the host Activity manifest. |

```kotlin
// Insets handling pseudocode
ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
    val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
    binding.modelList.updatePadding(bottom = imeHeight + 80.dpToPx)
    insets
}
```

### 5.2 Very Long Model Names

| Issue | Mitigation |
|-------|-----------|
| Name exceeds card width | `maxLines="3"` with `ellipsize="end"`. 3 lines at `bodyLarge` (16sp) fits ~120 chars on 360dp screen. Virtually no model name exceeds this. |
| Name contains path separators (`/`) | No special handling — `TextView` renders them naturally. The wrap behavior splits at word boundaries, which for model IDs means at `/` or `-` characters. |
| Mixed RTL/LTR in names (e.g. Arabic-flagged models) | No special handling needed — `TextView` auto-detects bidi. Ensure `textDirection="locale"` is set on the card. |
| Provider chip overflow | Provider chip has fixed max-width ~80dp with `ellipsize="end"`. If provider name is very long (e.g. `modelscope/Qwen/Qwen3-235B…`), only the provider portion in the chip is shown: `modelscope`. |

### 5.3 1000+ Models (e.g. OpenRouter full catalog)

| Concern | Solution |
|---------|----------|
| Memory with `RecyclerView` | `RecyclerView` + `DiffUtil` handles 1000+ comfortably. Async filtering on a background coroutine (`Flow.debounce(200) + filter`) prevents UI jank. |
| Scrollbar / fling performance | `LinearLayoutManager` with `setHasFixedSize(false)` (since heights vary due to wrap). Use `SmoothScroller` for programmatic scroll. |
| Sorting overhead | Sort is O(n log n) on the in-memory list. 1000 models sorted in <1ms. No need for paging. |
| Section header "All Models (142)" | Show count of currently filtered models. If >999, show "1K+". |
| Initial load time | Fetch models asynchronously. Show shimmer placeholder immediately. Cache in-memory per provider (already done in AgentChatActivity `fetchedModels`). |
| Recent models storage | Persist to `SharedPreferences` (max 5 most recently selected models, keyed by provider). |

### 5.4 Provider Color Palette

```kotlin
// Provider color mapping (background tint for chips)
val PROVIDER_COLORS = mapOf(
    "openai"          to 0x1A10A37F,   // green tint
    "anthropic"       to 0x1ACC7B8C,   // pink tint
    "deepseek"        to 0x1A4F46E5,   // indigo tint
    "nvidia"          to 0x1A76B900,   // lime tint
    "modelscope"      to 0x1AFF9800,   // orange tint
    "google"          to 0x1A4285F4,   // blue tint
    "meta"            to 0x1A1877F2,   // blue tint
    "microsoft"       to 0x1A00A1F1,   // azure tint
    "venice"          to 0x1AE91E63,   // pink tint
    "mistral"         to 0x1AFF6F00,   // amber tint
    "xai"             to 0x1A1A1A2E,   // dark tint
    "default"         to 0x1A6B7280,   // gray tint
)
```

### 5.5 Accessibility

| Requirement | Implementation |
|-------------|---------------|
| Content descriptions | All interactive elements have `contentDescription`: close, search, sort, pin, select, each recent chip |
| Keyboard navigation | Focus order: search → sort → recent chips → model list (arrow keys) → close |
| TalkBack | Each model row is a single focusable element that reads: "Model name: {name}, Provider: {provider}, Context: {length}, Pinned: {yes/no}" |
| Touch target size | All interactive elements ≥48dp (Material 3 default) |
| Font scaling | Respects system font size — `sp` units used throughout |

---

## 6. Flow Diagram

```
                       ┌──────────────┐
                       │ User taps    │
                       │ "model" in   │
                       │ toolbar      │
                       └──────┬───────┘
                              │
                              ▼
                 ┌────────────────────────┐
                 │ Show ModalBottomSheet  │
                 │ 80% screen height      │
                 │ Animate slide-up       │
                 └───────────┬────────────┘
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
            ┌──────────────┐  ┌──────────────┐
            │ Models cached │  │ No cached    │
            │ in memory?   │  │ models       │
            └──────┬───────┘  └──────┬───────┘
                   │                 │
                   ▼                 ▼
            ┌──────────────┐  ┌──────────────┐
            │ Show list    │  │ Show loading │
            │ immediately  │  │ skeleton     │
            │ + apply sort │  │ + fetch from │
            └──────┬───────┘  │ provider API │
                   │          └──────┬───────┘
                   │                 │
                   └──────┬──────────┘
                          ▼
              ┌───────────────────────┐
              │ User can:             │
              │  • Type to filter     │
              │  • Tap sort chip      │
              │  • Tap star to pin    │
              │  • Tap recent chip    │
              │  • Scroll list        │
              │  • Tap Select         │
              │  • Tap X / swipe down │
              └───────────────────────┘
                          │
        ┌─────────────────┴─────────────┐
        ▼                               ▼
┌───────────────┐             ┌──────────────────┐
│ User taps     │             │ User taps X      │
│ "Select" or   │             │ or swipes down   │
│ recent chip   │             └────────┬─────────┘
└───────┬───────┘                      │
        │                              ▼
        ▼                      ┌──────────────────┐
┌────────────────────┐         │ Dismiss sheet    │
│ Return model to    │         │ (no change)      │
│ AgentChatActivity  │         └──────────────────┘
│ via callback       │
│ Dismiss sheet      │
└────────────────────┘
```

---

## 7. Data Model

```kotlin
data class PickerModel(
    val modelId: String,          // e.g. "openai/gpt-5.4"
    val displayName: String,     // e.g. "gpt-5.4" or full name
    val provider: String,        // e.g. "OpenAI"
    val providerSlug: String,    // e.g. "openai" (for color lookup)
    val contextLength: Int?,     // e.g. 131072, nullable if unknown
    val isPinned: Boolean = false,
    val lastUsed: Long? = null,  // epoch millis, null if never
)

sealed interface PickerState {
    data object Empty : PickerState
    data class Loading(val partialCount: Int = 0) : PickerState
    data class Results(
        val models: List<PickerModel>,
        val recentIds: List<String>,
        val pinnedIds: Set<String>,
        val sortMode: SortMode,
        val totalCount: Int,
    ) : PickerState
    data class Searching(
        val query: String,
        val filtered: List<PickerModel>,
        val totalCount: Int,
    ) : PickerState
    data object NoResults : PickerState
}

enum class SortMode {
    RECENT,       // recently used first (by lastUsed desc), then alphabetical
    ALPHABETICAL, // A–Z
    CONTEXT_LENGTH // descending (largest context first)
}
```

---

## 8. Replacing the Current ExposedDropdownMenu

**Current** (AgentChatActivity toolbar):
```kotlin
// Remove this:
val modelDropdown = ArrayAdapter(context, R.layout.item_dropdown, modelNames)
binding.toolbarModelDropdown.setAdapter(modelDropdown)
binding.toolbarModelDropdown.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
```

**New** (toolbar button → bottom sheet):
```kotlin
binding.toolbarModelButton.setOnClickListener {
    val sheet = ModelPickerBottomSheet(
        onModelSelected = { model ->
            viewModel.setSelectedModel(model)
        }
    )
    sheet.show(supportFragmentManager, "model_picker")
}
```

**Files to create/modify**:
| File | Action |
|------|--------|
| `res/layout/dialog_model_picker.xml` | Create — bottom sheet layout |
| `res/layout/item_model.xml` | Create — single model row |
| `ModelPickerBottomSheet.kt` | Create — `ModalBottomSheet` fragment + ViewModel |
| `model/PickerModel.kt` | Create — data class and `PickerState` sealed interface |
| `AgentChatActivity.kt` | Modify — replace dropdown with `onModelSelected` button |
| `activity_agent.xml` | Modify — replace `ExposedDropdownMenu` with text button/trigger |
| `res/values/colors.xml` | Add — provider chip background colors |
| `res/drawable/ic_star.xml` | Add — star filled icon (if not present) |
| `res/drawable/ic_star_outline.xml` | Add — star outline icon (if not present) |
| `res/drawable/ic_sort.xml` | Add — sort icon (if not present) |
| `res/drawable/bg_context_badge.xml` | Add — rounded rectangle for context badge |
