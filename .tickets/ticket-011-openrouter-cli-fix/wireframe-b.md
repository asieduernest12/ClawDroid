# Wireframe B — "Tab-Organized Sheet" Model Picker Modal

## Overview

A `BottomSheetDialogFragment` with Material Design tab layout. Two tabs — "All Models" (searchable scrollable list) and "Favorites" (pinned models only). A shared search bar filters across the active tab. Sort mode controlled by a dropdown chip in the header. Designed to handle 1000+ OpenRouter models without dropdown overflow, with proper multiline wrapping for long names like `nvidia/nemotron-4-340b-instruct`.

### Key Difference from Wireframe A

| Aspect | Wireframe A (Search-First Drawer) | Wireframe B (Tab-Organized Sheet) |
|--------|-----------------------------------|-----------------------------------|
| Container | `ModalBottomSheet` (80% height) | `BottomSheetDialogFragment` (100% height peek) |
| Organization | Single list with "Recently Used" section header | Two tabs: "All Models" + "Favorites" |
| Navigation | Scroll through monolithic list | Tab switch between full catalog and pinned subset |
| Favorites | Star toggle + pinned-first sort | Dedicated tab with only pinned models |
| Sort | Single chip cycling through 3 modes | Dropdown chip in header bar |
| Search | Hides recent section | Persists across tab switches |
| Empty state | Single "no models" page | Per-tab empty states |

---

## 1. ASCII Wireframe

```
┌──────────────────────────────────────────────────────────┐
│ ════════════════════════════════════════════════════════ │  ← Drag handle
│                                                            │
│  Select Model                                     [ X ]  │  ← Title bar
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  🔍  Search models or filter...               Sort▾ │ │  ← Search bar (pinned, shared)
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────┐  ┌──────────────────┐                   │
│  │  All Models  │  │  ★ Favorites (3) │                   │  ← MaterialTabLayout
│  └──────────────┘  └──────────────────┘                   │
│                                                            │
│  ────────────────────────────────────────────────────────  │  ← Tab indicator (active)
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  ☆  claude-sonnet-4-20250515                  200K  │ │ │
│  │     Anthropic                                [Select]│ │ │
│  │                                                     │ │ │
│  │  ★  gpt-5.4                                  128K   │ │ │  ← Pinned star (filled)
│  │     OpenAI                                  [Select] │ │ │
│  │                                                     │ │ │
│  │  ☆  nvidia/nemotron-4-340b-instruct          128K   │ │ │  ← Long name wraps
│  │     NVIDIA                                  [Select] │ │ │     naturally
│  │                                                     │ │ │
│  │  ☆  Qwen/Qwen3-235B-A22B-Instruct-Mao-      32K     │ │ │  ← Second wrap line
│  │     2507                                             │ │ │
│  │     ModelScope                              [Select] │ │ │
│  │                                                     │ │ │
│  │  ☆  deepseek/deepseek-r1-671b               128K    │ │ │
│  │     DeepSeek                                [Select] │ │ │
│  │                                                     │ │ │
│  │  ☆  meta-llama/llama-4-scout-103b          1,024K   │ │ │  ← 4-digit context
│  │     Meta                                     [Select]│ │ │
│  │                                                     │ │ │
│  │  ← RecyclerView (virtual scrolling) →               │ │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Showing 142 of 1,247 models   ·   OpenRouter API   │ │  ← Footer (sticky)
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
└──────────────────────────────────────────────────────────┘
```

### Favorites Tab (Active)

```
┌──────────────────────────────────────────────────────────┐
│ ════════════════════════════════════════════════════════ │
│                                                            │
│  Select Model                                     [ X ]  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  🔍  Search models or filter...               Sort▾ │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────┐  ┌──────────────────┐                   │
│  │  All Models  │  │  ★ Favorites (3) │                   │  ← Active tab
│  └──────────────┘  └──────────────────┘                   │
│  ─────────────────────────────────────────────────────    │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  ★  gpt-5.4                                  128K   │ │ │
│  │     OpenAI                                  [Select] │ │ │
│  │                                                     │ │ │
│  │  ★  claude-sonnet-4-20250515                 200K   │ │ │
│  │     Anthropic                                [Select]│ │ │
│  │                                                     │ │ │
│  │  ★  meta-llama/llama-4-scout-103b          1,024K   │ │ │
│  │     Meta                                     [Select]│ │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Showing 3 pinned models                             │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### Empty State (Favorites tab, no pinned models)

```
┌──────────────────────────────────────────────────────────┐
│ ════════════════════════════════════════════════════════ │
│                                                            │
│  Select Model                                     [ X ]  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  🔍  Search models or filter...               Sort▾ │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────┐  ┌──────────────────┐                   │
│  │  All Models  │  │  ★ Favorites (0) │                   │
│  └──────────────┘  └──────────────────┘                   │
│  ─────────────────────────────────────────────────────    │
│                                                            │
│                    ┌──────────────────────┐               │
│                    │  ★                    │               │
│                    │  No pinned models     │               │
│                    │                       │               │
│                    │  Star models from     │               │
│                    │  "All Models" tab     │               │
│                    │  to add them here     │               │
│                    └──────────────────────┘               │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  0 pinned models    ·   Tap ★ to pin from All tab   │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### Loading State

```
┌──────────────────────────────────────────────────────────┐
│ ════════════════════════════════════════════════════════ │
│                                                            │
│  Select Model                                     [ X ]  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  🔍  Search models or filter...               Sort▾ │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────┐  ┌──────────────────┐                   │
│  │  All Models  │  │  ★ Favorites     │                   │
│  └──────────────┘  └──────────────────┘                   │
│  ─────────────────────────────────────────────────────    │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  ┌──────────────────────────────────────────┐        │ │
│  │  │  ▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░   Loading…  │        │ │  ← LinearProgressIndicator
│  │  └──────────────────────────────────────────┘        │ │
│  │                                                       │ │
│  │  ┌────────────────────────────────────────────────┐  │ │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━  ░░░░░░░░░░░░░░░░░░░░  │  │ │  ← Shimmer rows (×3)
│  │  │  ━━━━━         ░░░░░░░░░                       │  │ │
│  │  └────────────────────────────────────────────────┘  │ │
│  │  ┌────────────────────────────────────────────────┐  │ │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━  ░░░░░░░░░░░░░░░░░░░░  │  │ │
│  │  │  ━━━━━         ░░░░░░░░░                       │  │ │
│  │  │  └────────────────────────────────────────────┘  │ │
│  │  ┌────────────────────────────────────────────────┐  │ │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━  ░░░░░░░░░░░░░░░░░░░░  │  │ │
│  │  │  ━━━━━         ░░░░░░░░░                       │  │ │
│  │  └────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Fetching models from OpenRouter API…          45%  │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### No Search Results

```
┌──────────────────────────────────────────────────────────┐
│ ════════════════════════════════════════════════════════ │
│                                                            │
│  Select Model                                     [ X ]  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  🔍  xyzzy                                     Sort▾ │ │
│  │                                     [ × Clear ]      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────┐  ┌──────────────────┐                   │
│  │  All Models  │  │  ★ Favorites     │                   │
│  └──────────────┘  └──────────────────┘                   │
│  ─────────────────────────────────────────────────────    │
│                                                            │
│                    ┌──────────────────────┐               │
│                    │  🔍                   │               │
│                    │  No models match      │               │
│                    │  "xyzzy"             │               │
│                    │                       │               │
│                    │  Try a different      │               │
│                    │  search term or       │               │
│                    │  clear the filter     │               │
│                    │                       │               │
│                    │    [ Clear Filter ]   │               │
│                    └──────────────────────┘               │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  No results for "xyzzy"    ·   1,247 total models   │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### Legend

| Symbol | Component | Description |
|--------|-----------|-------------|
| `════════` | Drag handle | `BottomSheetDialogFragment` built-in handle |
| `[ X ]` | Close button | `MaterialButton` with `@drawable/ic_close` |
| `🔍` | Search icon | `TextInputLayout` start icon |
| `Sort▾` | Sort chip | `MaterialChip` with dropdown arrow — tap to open `PopupMenu` |
| `All Models` | Tab 1 | `TabLayout.Tab` showing full model catalog |
| `★ Favorites (3)` | Tab 2 | `TabLayout.Tab` showing only pinned models + count |
| `─────────` | Tab indicator | Active tab underline (`tabIndicator`) |
| `☆` / `★` | Star toggle | Unfilled/filled star icon for pin state |
| `128K` | Context badge | Compact `TextView` with rounded background |
| `OpenAI` | Provider chip | Colored `MaterialChip` (assist style) keyed by provider slug |
| `[Select]` | Select button | `MaterialButton` (text style) — selects model, dismisses sheet |
| `━━━━` | Shimmer | Placeholder skeleton loading animation |
| `▓▓▓▓░░` | Progress | `LinearProgressIndicator` determinate |

---

## 2. Widget Hierarchy (XML Tree)

### Root Layout: `dialog_model_picker_bottom_sheet.xml`

```xml
<!-- dialog_model_picker_bottom_sheet.xml -->
<!-- Root: BottomSheetDialogFragment with extended max height -->
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    android:background="?attr/colorSurface">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipChildren="false">

        <!-- ── DRAG HANDLE (built into BottomSheetDialogFragment) ── -->

        <!-- ── HEADER: Title + Close ── -->
        <LinearLayout
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingHorizontal="20dp"
            android:paddingTop="12dp"
            android:paddingBottom="4dp">

            <TextView
                android:id="@+id/title"
                android:text="@string/model_picker_title"
                android:textAppearance="@style/TextAppearance.Material3.TitleLarge"
                android:layout_weight="1"
                android:layout_marginEnd="8dp" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_close"
                style="@style/Widget.Material3.Button.IconButton"
                android:src="@drawable/ic_close"
                android:contentDescription="@string/model_picker_close_desc"
                app:iconTint="?attr/colorOnSurfaceVariant" />

        </LinearLayout>

        <!-- ── SEARCH BAR (shared across tabs) ── -->
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingHorizontal="16dp"
            android:paddingVertical="8dp">

            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/search_layout"
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:endIconMode="clear_text"
                app:startIconDrawable="@drawable/ic_search"
                app:boxStrokeWidth="1dp"
                app:boxStrokeWidthFocused="2dp"
                app:hintEnabled="false">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/search_input"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:hint="@string/model_picker_search_hint"
                    android:inputType="textFilter"
                    android:maxLines="1"
                    android:imeOptions="actionSearch"
                    android:importantForAutofill="no" />

            </com.google.android.material.textfield.TextInputLayout>

            <!-- Sort chip overlaid on search bar end -->
            <com.google.android.material.chip.Chip
                android:id="@+id/sort_chip"
                style="@style/Widget.Material3.Chip.Filter"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="end|center_vertical"
                android:layout_marginEnd="48dp"  <!-- offset for search clear button -->
                android:text="@string/model_picker_sort_recent"
                app:chipIcon="@drawable/ic_sort"
                app:closeIconVisible="false"
                android:visibility="gone" />

        </FrameLayout>

        <!-- ── TAB LAYOUT ── -->
        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tab_layout"
            style="@style/Widget.Material3.TabLayout.Secondary"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingHorizontal="16dp"
            app:tabIndicatorColor="?attr/colorPrimary"
            app:tabIndicatorHeight="3dp"
            app:tabSelectedTextColor="?attr/colorPrimary"
            app:tabTextColor="?attr/colorOnSurfaceVariant"
            app:tabMode="fixed"
            app:tabGravity="start"
            app:tabMinWidth="0dp"
            app:tabMaxWidth="180dp">

            <com.google.android.material.tabs.TabItem
                android:text="@string/model_picker_tab_all"
                android:layout_width="wrap_content" />

            <com.google.android.material.tabs.TabItem
                android:text="@string/model_picker_tab_favorites"
                android:layout_width="wrap_content" />

        </com.google.android.material.tabs.TabLayout>

        <!-- ── TAB CONTENT (ViewPager2) ── -->
        <androidx.viewpager2.widget.ViewPager2
            android:id="@+id/view_pager"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            app:layout_behavior="com.google.android.material.behavior.HideBottomViewOnScrollBehavior" />

        <!-- ── STICKY FOOTER ── -->
        <TextView
            android:id="@+id/footer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingHorizontal="20dp"
            android:paddingVertical="12dp"
            android:gravity="center"
            android:textAppearance="@style/TextAppearance.Material3.BodySmall"
            android:textColor="?attr/colorOnSurfaceVariant"
            android:background="?attr/colorSurface"
            android:elevation="2dp" />

    </LinearLayout>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Tab Fragment: `fragment_model_list.xml` (Inflated per tab)

```xml
<!-- fragment_model_list.xml -->
<!-- Used by both "All Models" and "Favorites" tabs via ViewPager2 -->
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- ── EMPTY STATE (shown when list empty) ── -->
    <LinearLayout
        android:id="@+id/empty_state"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="48dp"
        android:visibility="gone">

        <ImageView
            android:id="@+id/empty_icon"
            android:layout_width="64dp"
            android:layout_height="64dp"
            android:src="@drawable/ic_star_outline"
            app:tint="?attr/colorOnSurfaceVariant"
            android:importantForAccessibility="no" />

        <TextView
            android:id="@+id/empty_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textAppearance="@style/TextAppearance.Material3.TitleMedium"
            android:textColor="?attr/colorOnSurface" />

        <TextView
            android:id="@+id/empty_subtitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textAppearance="@style/TextAppearance.Material3.BodyMedium"
            android:textColor="?attr/colorOnSurfaceVariant"
            android:gravity="center" />

    </LinearLayout>

    <!-- ── LOADING STATE (shimmer skeleton) ── -->
    <LinearLayout
        android:id="@+id/loading_state"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:visibility="gone">

        <com.google.android.material.progressindicator.LinearProgressIndicator
            android:id="@+id/progress_bar"
            android:layout_width="match_parent"
            android:layout_height="4dp"
            android:indeterminate="false"
            app:indicatorColor="?attr/colorPrimary" />

        <FrameLayout
            android:id="@+id/shimmer_container"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp">

            <!-- Shimmer rows generated programmatically or via layout -->

        </FrameLayout>

    </LinearLayout>

    <!-- ── MODEL LIST (RecyclerView) ── -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/model_list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:paddingHorizontal="12dp"
        android:paddingBottom="8dp"
        android:scrollbars="vertical"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

</FrameLayout>
```

### Item Layout: `item_model_picker.xml` (Single Model Row)

```xml
<!-- item_model_picker.xml -->
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="4dp"
    android:layout_marginVertical="2dp"
    app:cardElevation="0dp"
    app:cardCornerRadius="12dp"
    app:strokeWidth="0dp"
    android:background="?android:attr/selectableItemBackground"
    android:foreground="?android:attr/selectableItemBackground"
    android:minHeight="64dp">

    <LinearLayout
        android:orientation="horizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:paddingVertical="10dp"
        android:paddingHorizontal="12dp">

        <!-- ★ STAR TOGGLE -->
        <ImageButton
            android:id="@+id/btn_star"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:src="@drawable/ic_star_outline"
            android:contentDescription="@string/model_picker_pin_desc"
            android:background="?android:attr/selectableItemBackgroundBorderless"
            android:padding="8dp"
            app:tint="?attr/colorOnSurfaceVariant" />

        <!-- TEXT BLOCK (weight=1, wraps) -->
        <LinearLayout
            android:orientation="vertical"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginHorizontal="8dp">

            <!-- Model name (wrapping) -->
            <TextView
                android:id="@+id/model_name"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textAppearance="@style/TextAppearance.Material3.BodyLarge"
                android:textColor="?attr/colorOnSurface"
                android:maxLines="3"
                android:ellipsize="end"
                android:lineSpacingExtra="2sp"
                tools:text="nvidia/nemotron-4-340b-instruct" />

            <!-- Bottom row: provider chip + context badge -->
            <LinearLayout
                android:orientation="horizontal"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:gravity="center_vertical">

                <!-- Provider chip (colored) -->
                <com.google.android.material.chip.Chip
                    android:id="@+id/provider_chip"
                    style="@style/Widget.Material3.Chip.Assist"
                    android:layout_width="wrap_content"
                    android:layout_height="20dp"
                    android:text="OpenAI"
                    android:textSize="11sp"
                    android:textColor="?attr/colorOnSurface"
                    app:chipMinHeight="0dp"
                    app:chipMinTouchTargetSize="0dp"
                    app:chipBackgroundColor="@color/provider_chip_default"
                    app:chipStrokeWidth="0dp"
                    app:chipCornerRadius="4dp"
                    android:paddingHorizontal="6dp"
                    android:paddingVertical="0dp"
                    android:importantForAccessibility="no" />

                <!-- Context length badge -->
                <TextView
                    android:id="@+id/context_badge"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="6dp"
                    android:paddingHorizontal="6dp"
                    android:paddingVertical="1dp"
                    android:text="128K"
                    android:textSize="10sp"
                    android:textColor="?attr/colorOnSurfaceVariant"
                    android:background="@drawable/bg_context_badge"
                    android:importantForAccessibility="no"
                    tools:text="128K" />

            </LinearLayout>

        </LinearLayout>

        <!-- SELECT BUTTON -->
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_select"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:text="@string/model_picker_select"
            android:textSize="13sp"
            android:contentDescription="@string/model_picker_select_desc"
            android:paddingHorizontal="12dp" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

### Widget Hierarchy Diagram

```
BottomSheetDialogFragment
 └── CoordinatorLayout (root)
      └── LinearLayout (vertical)
           ├── LinearLayout (horizontal)                ← Header
           │    ├── TextView "Select Model"             ← Title (weight=1)
           │    └── MaterialButton [X]                  ← Close icon button
           │
           ├── FrameLayout                              ← Search + sort row
           │    ├── TextInputLayout (OutlinedBox)       ← Search bar
           │    │    └── TextInputEditText              ← Search input
           │    │         ├── startIcon: ic_search
           │    │         └── endIconMode: clear_text
           │    └── Chip (Filter) "Sort▾"               ← Sort mode chip
           │
           ├── TabLayout                                ← MD3 Secondary Tabs
           │    ├── TabItem "All Models"                ← Tab 0
           │    └── TabItem "★ Favorites (N)"           ← Tab 1 (dynamic count)
           │
           ├── ViewPager2                               ← Tab content (weight=1)
           │    ├── [Tab 0] FragmentModelList           ← "All Models"
           │    │    └── FrameLayout
           │    │         ├── LinearLayout (empty_state)
           │    │         │    ├── ImageView (empty_icon)
           │    │         │    ├── TextView (empty_title)
           │    │         │    └── TextView (empty_subtitle)
           │    │         ├── LinearLayout (loading_state)
           │    │         │    ├── LinearProgressIndicator
           │    │         │    └── FrameLayout (shimmer_container)
           │    │         └── RecyclerView (model_list)
           │    │              └── item_model_picker (repeated)
           │    │                   └── MaterialCardView
           │    │                        └── LinearLayout (horizontal)
           │    │                             ├── ImageButton (btn_star)
           │    │                             ├── LinearLayout (vertical, weight=1)
           │    │                             │    ├── TextView (model_name, maxLines=3)
           │    │                             │    └── LinearLayout (horizontal)
           │    │                             │         ├── Chip (provider_chip)
           │    │                             │         └── TextView (context_badge)
           │    │                             └── MaterialButton (btn_select)
           │    │
           │    └── [Tab 1] FragmentModelList           ← "Favorites"
           │         └── (same structure, different adapter data)
           │
           └── TextView (footer)                        ← Sticky footer
                └── Text: "Showing X of Y models · Provider"
```

### Resolve Key

| XML Name | Widget Type | Role |
|----------|------------|------|
| `dialog_model_picker_bottom_sheet.xml` | `CoordinatorLayout` | Root container for `BottomSheetDialogFragment` |
| `tab_layout` | `TabLayout` | MD3 secondary tab indicator |
| `view_pager` | `ViewPager2` | Swipeable tab content |
| `fragment_model_list.xml` | `FrameLayout` | Shared fragment inflated per tab |
| `model_list` | `RecyclerView` | Virtualized model list |
| `search_input` | `TextInputEditText` | Fuzzy search with 200ms debounce |
| `sort_chip` | `MaterialChip` (filter) | Sort mode: Recent, A-Z, Context |
| `btn_star` | `ImageButton` | Toggle pin (star_filled ↔ star_outline) |
| `model_name` | `TextView` | Model name, `maxLines=3`, wraps |
| `provider_chip` | `MaterialChip` (assist) | Provider label, color per slug |
| `context_badge` | `TextView` | Context window size, e.g. "128K" |
| `btn_select` | `MaterialButton` (text) | Select and dismiss |
| `btn_close` | `MaterialButton` (icon) | Dismiss without selecting |
| `empty_state` | `LinearLayout` | No-results / no-favorites illustration |
| `loading_state` | `LinearLayout` | Shimmer skeleton + progress bar |
| `footer` | `TextView` | Count, provider name, status |

---

## 3. Component Choices & Rationale

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Container** | `BottomSheetDialogFragment` (full height) over `AlertDialog` | Expandable to full screen for 1000+ model lists. Built-in drag-to-dismiss, `WindowInsets` handling for keyboard, smooth slide-up animation. `AlertDialog` would have fixed max height and no swipe gesture. |
| **Tab Layout** | `ViewPager2` + `TabLayout` with `FragmentStateAdapter` | `ViewPager2` supports swipe between tabs and recycling off-screen fragments. `TabLayout` with `tabMode="fixed"` keeps both tabs visible. `FragmentStateAdapter` destroys off-screen fragments to save memory when not needed. |
| **Search** | `TextInputLayout` (OutlinedBox) with `endIconMode="clear_text"` | Shared across both tabs — search query persists on tab switch. `clear_text` end icon gives one-tap clear. `textFilter` input type disables autocorrect/prediction. Debounce: 200ms `Flow.debounce()` on text changes. |
| **Sort** | `MaterialChip` (filter) with `PopupMenu` | Single tap opens a `PopupMenu` with 3 options: Recent, A–Z, Context Length. Selected mode shown as chip text. Positioned in the search bar end area, toggles visibility when search is empty vs active. |
| **Model Name** | `TextView` with `maxLines=3`, `ellipsize=end`, `lineSpacingExtra=2sp` | Must wrap names like `nvidia/nemotron-4-340b-instruct` (28 chars) and `Qwen/Qwen3-235B-A22B-Instruct-Mao-2507` (47 chars). 3 lines at `bodyLarge` (16sp) fits ~120 chars on a 360dp screen. Line spacing improves readability when wrapped. |
| **Provider Chip** | `MaterialChip` (assist, compact) with per-provider color tint | Compact variant (`chipMinHeight=0dp`, `chipMinTouchTargetSize=0dp`) avoids the 32dp default min height. Color tint set via `chipBackgroundColor` from a 12-color lookup by provider slug (see Provider Color Palette). |
| **Context Badge** | `TextView` with `GradientDrawable` background | Lighter weight than a `Chip`. No ripple, no extra padding. Rounded rectangle `bg_context_badge.xml` with `@dimen/badge_corner_radius=4dp`. |
| **Star/ Pin** | `ImageButton` with `selectableItemBackgroundBorderless` | 40×40dp touch target (≥48dp including padding). Toggle src between `ic_star` (filled, `colorPrimary` tint) and `ic_star_outline` (empty, `colorOnSurfaceVariant` tint). |
| **Select** | `MaterialButton` (text style) | Lower visual weight. No background fill. 40dp height to match star button. Triggered by tapping the button OR tapping the card row. |
| **List** | `RecyclerView` with `LinearLayoutManager` + `DiffUtil` | Required for 1000+ model count. `DiffUtil` enables animated insert/remove on filter and efficient rebind on pin toggle. `setHasFixedSize(false)` because item heights vary (wrapping text). |
| **Tabs Content** | `FragmentStateAdapter` with shared `FragmentModelList` | Single fragment class instantiated twice (tab 0, tab 1) with different arguments (`ARG_SHOW_FAVORITES_ONLY`). Adapter receives filtered list from parent `ModelPickerSheet`. |
| **Footer** | `TextView` with `elevation=2dp` | Sticky bottom bar showing model count + provider. Elevation creates subtle shadow separation from scrolling list. |
| **Shimmer** | Programmatic shimmer rows (3–5) | Custom `ShimmerDrawable` or simple animated gradient overlays on placeholder `MaterialCardView` rows. 3 skeleton rows shown during fetch. |

### Why Not…

| Alternative | Rejected Because |
|-------------|-----------------|
| **`AlertDialog`** | Fixed max height (~70% screen), no keyboard insets, no swipe-to-dismiss, feels jarring |
| **`ModalBottomSheet` (Material 3)** | Great but `BottomSheetDialogFragment` is more battle-tested with `ViewPager2` nesting. No significant advantage for this use case. |
| **`ExposedDropdownMenu`** | Current broken approach — dropdown overlay can't virtualize (1000 items → OOM), no multiline, fixed height |
| **`RecyclerView` without `ViewPager2`** | Single list with section headers would work but tabs give clearer separation of concerns and faster access to pinned models |
| **`SearchView` widget** | Deprecated in favor of `TextInputLayout`. No Material 3 theming support. |
| **`BottomNavigationView`** | Only 2 tabs — `TabLayout` is appropriate. `BottomNavigationView` is for 3+ top-level destinations. |
| **Single fragment with sections** | Works but tab UI is more discoverable for the "Favorites" concept. Users instantly see their pinned models without scrolling. |
| **Pull-to-refresh** | Not needed — models are fetched once on sheet open. Re-fetch via back-end trigger only. |
| **Lazy column (Jetpack Compose)** | Project uses XML layouts. Compose would add a 1.3MB dependency for a single sheet. |
| **Full-screen Activity** | Loses spatial context with the chat screen. Navigation feels disconnected. |
| **Bottom navigation within sheet** | Over-engineered for 2 destinations. `TabLayout` is simpler, lighter, standard. |

---

## 4. States

### 4.1 Empty (No Models Loaded)

```
┌──────────────────────────────────────────────┐
│ Select Model                          [X]    │
├──────────────────────────────────────────────┤
│ 🔍  Search models or filter...       Sort▾   │
├──────────────────────────────────────────────┤
│ All Models  │  ★ Favorites                   │
├──────────────────────────────────────────────┤
│                                              │
│         ┌────────────────────────┐           │
│         │    📡                    │           │
│         │    No models loaded     │           │
│         │                        │           │
│         │    Fetch models from    │           │
│         │    your provider first  │           │
│         │                        │           │
│         │   [ Fetch Models ]     │           │
│         └────────────────────────┘           │
│                                              │
├──────────────────────────────────────────────┤
│ No models loaded  ·  Tap "Fetch Models"      │
└──────────────────────────────────────────────┘
```

**Behavior**:
- Both tabs show the same empty state
- "Fetch Models" button triggers `fetchModelsFromProvider()` callback
- Search bar visible but disabled (non-interactive tint)
- Sort chip hidden
- Footer instruction: "No models loaded · Tap 'Fetch Models'"

### 4.2 Searching (Typing)

```
┌──────────────────────────────────────────────┐
│ Select Model                          [X]    │
├──────────────────────────────────────────────┤
│ 🔍  nemo                               Sort▾ │  ← "nemo" typed
│     ─────────────────────────────────────    │  ← underline focused
├──────────────────────────────────────────────┤
│ All Models  │  ★ Favorites (3)               │
├──────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────┐ │
│ │ ☆  nvidia/nemotron-4-340b-instruct  128K │ │
│ │    NVIDIA                         [Select]│ │
│ ├──────────────────────────────────────────┤ │
│ │ ☆  nvidia/nemotron-4-mini           64K  │ │
│ │    NVIDIA                         [Select]│ │
│ └──────────────────────────────────────────┘ │
│          ↓ 2 of 1,247 models                │
├──────────────────────────────────────────────┤
│ Showing 2 of 1,247 models                    │
└──────────────────────────────────────────────┘
```

**Behavior**:
- Debounced filter (200ms) on background `Flow`
- Tab stays on currently active tab
- Section header updates: `"All Models (2 of 1,247)"` or `"★ Favorites (0 of 3)"`
- Footer updates: `"Showing 2 of 1,247 models · OpenRouter API"`
- Keyboard visible — sheet expanded to full height via `STATE_EXPANDED`
- Clear button `[×]` appears in search end icon — tapping restores full list
- Empty rows animate out via `DiffUtil`
- Sort chip becomes `visibility="gone"` during active search to reduce clutter (sort doesn't affect filtered results meaningfully)

### 4.3 Results (Default State)

```
┌──────────────────────────────────────────────┐
│ Select Model                          [X]    │
├──────────────────────────────────────────────┤
│ 🔍  Search models or filter...       Sort▾   │
│                                         ↓    │
│                                     Recent   │  ← sort chip expanded
│                                     A–Z      │
│                                     Context  │
├──────────────────────────────────────────────┤
│ All Models  │  ★ Favorites (3)               │
├──────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────┐ │
│ │ ★  gpt-5.4                         128K  │ │  ← pinned first
│ │    OpenAI                          [Select]│ │
│ ├──────────────────────────────────────────┤ │
│ │ ☆  claude-sonnet-4-20250515        200K  │ │
│ │    Anthropic                       [Select]│ │
│ ├──────────────────────────────────────────┤ │
│ │ ☆  deepseek/deepseek-r1-671b       128K  │ │
│ │    DeepSeek                        [Select]│ │
│ ├──────────────────────────────────────────┤ │
│ │ ☆  meta-llama/llama-4-scout-103b  1,024K │ │
│ │    Meta                            [Select]│ │
│ ├──────────────────────────────────────────┤ │
│ │ ☆  nvidia/nemotron-4-340b-instruct 128K  │ │
│ │    NVIDIA                          [Select]│ │
│ └──────────────────────────────────────────┘ │
│      ↓ (virtual scrolling)                   │
├──────────────────────────────────────────────┤
│ Showing 1,247 models  ·  OpenRouter API      │
└──────────────────────────────────────────────┘
```

**Sort rules**:
1. **Recent**: Recently used (by `lastUsed` desc) → pinned → alphabetical
2. **A–Z**: Pinned first (sorted alphabetically among themselves) → unpinned alphabetical
3. **Context**: Descending context length → pinned first → alphabetical tiebreaker

**Visual**:
- Pinned items show filled star `★` with `colorPrimary` tint
- Pinned items have a subtle left border accent (`strokeWidth=0` but `RippleTheme` color hint)
- Section count in tab label updates: `"All Models (1,247)"`

### 4.4 No Results (Search Yields Nothing)

```
┌──────────────────────────────────────────────┐
│ Select Model                          [X]    │
├──────────────────────────────────────────────┤
│ 🔍  xyzzy                               [×]  │  ← clear button visible
├──────────────────────────────────────────────┤
│ All Models  │  ★ Favorites (3)               │
├──────────────────────────────────────────────┤
│                                              │
│         ┌────────────────────────┐           │
│         │    🔍                   │           │
│         │    No models match      │           │
│         │    "xyzzy"             │           │
│         │                        │           │
│         │    Try a different     │           │
│         │    search term or      │           │
│         │    clear the filter    │           │
│         │                        │           │
│         │   [ Clear Filter ]     │           │
│         └────────────────────────┘           │
│                                              │
├──────────────────────────────────────────────┤
│ No results for "xyzzy"  ·  1,247 total       │
└──────────────────────────────────────────────┘
```

**Behavior**:
- RecyclerView hidden, empty state shown
- "Clear Filter" button calls `searchInput.text.clear()`
- Footer shows `"No results for \"xyzzy\" · 1,247 total models"`
- Favorites tab: if user is on Favorites and search yields nothing, suggest: `"No pinned models match \"xyzzy\" · Try searching in All Models"`

### 4.5 Loading

```
┌──────────────────────────────────────────────┐
│ Select Model                          [X]    │
├──────────────────────────────────────────────┤
│ 🔍  Search models or filter...       Sort▾   │
│                                    (disabled) │
├──────────────────────────────────────────────┤
│ All Models  │  ★ Favorites                   │
├──────────────────────────────────────────────┤
│ ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░  45%                    │  ← LinearProgressIndicator
│                                              │
│ ┌──────────────────────────────────────────┐ │
│ │ ━━━━━━━━━━━━━━━━━        ░░░░░░░░░░░░░  │ │  ← shimmer row 1
│ │ ━━━━━━━        ░░░░░░░░░░               │ │
│ ├──────────────────────────────────────────┤ │
│ │ ━━━━━━━━━━━━━━━━━        ░░░░░░░░░░░░░  │ │  ← shimmer row 2
│ │ ━━━━━━━        ░░░░░░░░░░               │ │
│ ├──────────────────────────────────────────┤ │
│ │ ━━━━━━━━━━━━━━━━━        ░░░░░░░░░░░░░  │ │  ← shimmer row 3
│ │ ━━━━━━━        ░░░░░░░░░░               │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ 42 models loaded so far...                   │  ← incremental counter
├──────────────────────────────────────────────┤
│ Fetching models from OpenRouter API…  45%    │
└──────────────────────────────────────────────┘
```

**Behavior**:
- `LinearProgressIndicator` with determinate mode if total count known (from API)
- 3–5 shimmer skeleton rows animated via `ShimmerDrawable` or `AlphaAnimation` pulse
- If cached models exist, show immediately + shimmer only for new items
- Sort chip disabled (grayed out, no click)
- Search bar disabled (non-interactive)
- Tab switching disabled during load (user stays on current tab)
- Footer shows `"Fetching models from {provider}…  {percentage}%"`
- On completion: shimmer replaced with real list with `notifyItemRangeInserted` animation

### 4.6 Tab Switch

```
┌──────────────────────────────────────────────┐
│ Select Model                          [X]    │
├──────────────────────────────────────────────┤
│ 🔍  Search models or filter...       Sort▾   │
├──────────────────────────────────────────────┤
│ All Models  │  ★ Favorites (3)               │  ← ← ← swipe/finger
│                     ────────────              │  ← indicator slides
├──────────────────────────────────────────────┤
│ ★  gpt-5.4                             128K  │
│    OpenAI                            [Select] │
│ ★  claude-sonnet-4-20250515            200K  │
│    Anthropic                          [Select]│
│ ★  meta-llama/llama-4-scout-103b    1,024K   │
│    Meta                               [Select]│
├──────────────────────────────────────────────┤
│ Showing 3 pinned models                       │
└──────────────────────────────────────────────┘
```

**Behavior**:
- `ViewPager2` swipe gesture switches between tab fragments
- Search query text is persisted across tab switches (stored in parent fragment)
- `FragmentStateAdapter` may destroy off-screen fragment; parent caches search state in `SavedStateHandle`
- Tab label updates dynamically: `"★ Favorites (3)"` with live count
- Search re-applied on tab switch (filter switches context to the new tab's data set)
- Footer updates per tab: `"Showing 3 pinned models"` vs `"Showing 1,247 models · OpenRouter API"`

---

## 5. Edge Cases

### 5.1 Keyboard Showing

| Issue | Mitigation |
|-------|-----------|
| Sheet collapses on keyboard open | Override `onCreateDialog()` to set `behavior.state = STATE_EXPANDED` on show. Set `behavior.skipCollapsed = true`. |
| Search bar scrolled off | Search bar is in the **header**, not in the scrollable content — it stays pinned regardless of keyboard state. |
| RecyclerView hidden behind keyboard | Use `ViewCompat.setOnApplyWindowInsetsListener` on `model_list` to add `ime` bottom padding: `insets.getInsets(WindowInsetsCompat.Type.ime()).bottom + 16dp`. |
| TabLayout hidden | TabLayout is also in the fixed header — stays visible. |
| Input type mismatch | `inputType="textFilter"` disables autocorrect, emoji keyboard, and prediction. Returns `ACTION_SEARCH` on Enter (which dismisses keyboard, doesn't trigger filter). |

```kotlin
// Keyboard insets handling (in ModelPickerSheet.setupInsets)
ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
    val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
    val bottomPadding = ime.bottom + resources.getDimensionPixelSize(R.dimen.list_bottom_padding)
    modelList.updatePadding(bottom = bottomPadding)
    binding.footer.updatePadding(bottom = ime.bottom)
    insets
}
```

### 5.2 1000+ Models (OpenRouter Full Catalog)

| Concern | Solution |
|---------|----------|
| Memory | `RecyclerView` + `DiffUtil` handles 10,000+ items. ViewHolder recycling keeps ~20-30 inflated rows at any time. |
| Filter performance | Filter runs on `Default` dispatcher via `Flow.debounce(200).map { filter(it) }`. 1,247 model list filters in <2ms. Result emitted on `Main` dispatcher. |
| Sort performance | `Collections.sort()` on `ArrayList` of 1,247 items is <1ms for any sort mode. Not a bottleneck. |
| Scrollbar precision | `LinearLayoutManager` with `android:scrollbars="vertical"`. No custom scrollbar needed. |
| Footer count formatting | If model count > 999, display as "1.2K". If > 999,999, display as "1.2M". Formatted via `NumberFormat.getCompactNumberInstance()`. |
| Tab label count | Favorites tab label shows exact pinned count (never compact — expecting ≤50). All Models tab shows compact. |
| Initial load time | API fetch runs on background coroutine. Shimmer shown immediately. Cache models in `ViewModel` (scoped to sheet lifecycle) so re-opening is instant. |
| Memory on rotation | `ViewModel` survives config changes. `FragmentStateAdapter` re-creates fragments but data is preserved in parent's `SavedStateHandle`. |
| Search on large dataset | Filter is O(n) string matching. With 200ms debounce, typing "xyzzy" filters once, not per keystroke. No jank. |

### 5.3 Provider Switching Mid-Search

| Scenario | Behavior |
|----------|----------|
| User switches provider while sheet is open | Sheet dismisses → new `ModelPickerSheet` is created with new provider's model list. Search state is lost (different provider = different models). |
| Provider has zero models | Empty state shown with "No models loaded for {provider}" message. "Fetch Models" button calls the provider's endpoint. |
| Provider API returns errors | Footer shows `"Error fetching models from {provider}: {error}"`. Retry button appears in footer. Previously cached models remain visible. |
| Provider change during loading | If user dismisses while loading, coroutine is cancelled via `viewModelScope`. No orphaned network calls. |
| Search term applies to wrong provider | Not possible — search is local to the current model list. Tab switch doesn't change provider. |

### 5.4 Very Long Model Names

| Name Example | Length | Behavior |
|-------------|--------|----------|
| `gpt-5.4` | 7 chars | Single line, no wrapping |
| `nvidia/nemotron-4-340b-instruct` | 32 chars | Wraps to 2 lines on 360dp screen |
| `Qwen/Qwen3-235B-A22B-Instruct-Mao-2507` | 47 chars | Wraps to 2 lines |
| `CohereForAI/c4ai-command-r7b-12-2025` | 38 chars | Wraps to 2 lines |
| `deepseek-ai/DeepSeek-V3-0324` | 27 chars | Single line |
| `mistralai/Mixtral-8x22B-Instruct-v0.1` | 40 chars | Wraps to 2 lines |

**Mitigations**:
- `maxLines="3"` with `ellipsize="end"` — 3 lines at 16sp fits ~120 chars
- `lineSpacingExtra="2sp"` improves readability when multi-line
- No manual truncation — `TextView` handles wrapping naturally
- Provider chip is a separate element below the name, never overlaps

### 5.5 Star/Pin Persistence

| Scenario | Behavior |
|----------|----------|
| User stars model | Toggle `ImageButton` to filled star. Persist to `SharedPreferences` keyed by `providerSlug + ":" + modelId`. Max 50 pinned models per provider. |
| User unstars model | Toggle to outline. Remove from `SharedPreferences`. If on Favorites tab, item animates out via `DiffUtil.notifyItemRemoved()`. |
| Pinned model deleted from API | On next fetch, pinned model won't appear in API response. It is silently removed from pinned set. Favorites tab count decrements. |
| Max pinned limit (50) | Show `Snackbar`: "Maximum 50 pinned models. Unpin another to pin this one." |

```kotlin
// Pin persistence
private fun togglePin(model: PickerModel) {
    val key = "${provider.slug}:${model.modelId}"
    val prefs = preferences.edit()
    if (model.isPinned) {
        pinnedSet.remove(key)
        prefs.remove(key)
    } else {
        if (pinnedSet.size >= MAX_PINNED) {
            Snackbar.make(binding.root, R.string.max_pinned_warning, Snackbar.LENGTH_SHORT).show()
            return
        }
        pinnedSet.add(key)
        prefs.putStringSet(PINNED_KEYS, pinnedSet)
    }
    prefs.apply()
    adapter.notifyItemChanged(indexOf(model)) // update star icon
}
```

### 5.6 Empty Favorites Tab — First Use

| Element | Display |
|---------|---------|
| Empty icon | Hollow star `ic_star_outline` at 64dp |
| Title | "No pinned models" |
| Subtitle | "Star models from the 'All Models' tab to add them here" |
| Action | None (instructional only) |
| Footer | "0 pinned models · Tap ★ to pin from All tab" |

On "All Models" tab, if the user has never pinned anything, a subtle hint chip appears after scrolling past 20 items: `💡 Tip: Tap the star to pin a model for quick access`.

### 5.7 Accessibility

| Requirement | Implementation |
|-------------|---------------|
| TalkBack row | Each `MaterialCardView` is a single focusable unit. `contentDescription` reads: "Model {name}, Provider {provider}, Context {length}, {pinned status}. Tap to select." |
| Star toggle | `contentDescription` dynamically set: "Pin {model}" / "Unpin {model}" |
| Select button | "Select {model}" |
| Tabs | "All Models tab, {count} models" / "Favorites tab, {count} pinned models" |
| Sort chip | "Sort by {mode}. Tap to change." |
| Search | Auto-focus on sheet open — `TalkBack` announces "Search models. Edit box." |
| Touch targets | All interactive elements ≥48dp. `btn_star` is 40dp with `padding=8dp` = effective 48dp total. |
| Font scaling | All text in `sp` units. Layout accommodates up to 1.5× scaling (tested). |
| Keyboard nav | Focus order: Close → Search → Sort → Tabs → List (down/up arrows) |

---

## 6. Data Model

```kotlin
// Data class for display (separate from transport ModelProvider)
data class PickerModel(
    val modelId: String,            // "openai/gpt-5.4" (fully qualified)
    val displayName: String,        // "gpt-5.4" (short display)
    val providerName: String,       // "OpenAI"
    val providerSlug: String,       // "openai" (for color lookup)
    val contextLength: Int?,        // 131072, null if unknown
    val isPinned: Boolean = false,
    val lastUsed: Long? = null,     // epoch millis, null if never used
)

// Sealed state for the picker
sealed interface PickerUiState {
    data object Empty : PickerUiState
    data class Loading(
        val partialCount: Int = 0,
        val progress: Float = 0f,   // 0.0..1.0
    ) : PickerUiState
    data class Results(
        val allModels: List<PickerModel>,
        val pinnedModels: List<PickerModel>,
        val filteredAll: List<PickerModel>,
        val filteredPinned: List<PickerModel>,
        val pinnedCount: Int,
        val totalCount: Int,
        val sortMode: SortMode,
        val activeTab: Int,         // 0 = All, 1 = Favorites
    ) : PickerUiState
    data class Searching(
        val query: String,
        val filteredAll: List<PickerModel>,
        val filteredPinned: List<PickerModel>,
        val totalAll: Int,
        val totalPinned: Int,
    ) : PickerUiState
    data class NoResults(
        val query: String,
        val onTab: Int,             // which tab the search was on
    ) : PickerUiState
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
    ) : PickerUiState
}

enum class SortMode {
    RECENT,
    ALPHABETICAL,
    CONTEXT_LENGTH,
}

// ViewModel for the picker
class ModelPickerViewModel(
    private val provider: ModelProvider,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PickerUiState>(PickerUiState.Empty)
    val uiState: StateFlow<PickerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.RECENT)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val pinnedModels = MutableStateFlow<Set<String>>(emptySet())

    // Debounced search filter
    val filteredModels: StateFlow<PickerUiState> = combine(
        _uiState, _searchQuery.debounce(200), _sortMode, pinnedModels
    ) { state, query, sort, pinned ->
        // Filter + sort logic
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PickerUiState.Empty)

    fun fetchModels() { /* call provider API, emit Loading → Results/Error */ }
    fun togglePin(modelId: String) { /* update pinnedModels + persist */ }
    fun setSort(mode: SortMode) { _sortMode.value = mode }
    fun setSearch(query: String) { _searchQuery.value = query }
    fun setTab(index: Int) { _activeTab.value = index }
}
```

## 7. Sheet-to-Activity Contract

```kotlin
// Result callback interface
interface OnModelSelectedListener {
    fun onModelSelected(modelId: String, displayName: String)
}

// BottomSheetDialogFragment implementation
class ModelPickerBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_PROVIDER = "provider_json"

        fun newInstance(provider: ModelProvider): ModelPickerBottomSheet {
            return ModelPickerBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROVIDER, provider.toJson().toString())
                }
            }
        }
    }

    private var listener: OnModelSelectedListener? = null

    fun setOnModelSelectedListener(listener: OnModelSelectedListener) {
        this.listener = listener
    }

    // ... onCreateView, onViewCreated, setup tabs, search, etc.

    private fun onSelectModel(model: PickerModel) {
        listener?.onModelSelected(model.modelId, model.displayName)
        // Persist as recently used
        // Persist lastUsed timestamp
        dismiss()
    }
}

// Usage in AgentChatActivity:
binding.btn_model_picker.setOnClickListener {
    val sheet = ModelPickerBottomSheet.newInstance(currentProvider)
    sheet.setOnModelSelectedListener { modelId, displayName ->
        viewModel.setSelectedModel(modelId, displayName)
    }
    sheet.show(supportFragmentManager, "model_picker")
}
```

## 8. Files to Create / Modify

| File | Action | Description |
|------|--------|-------------|
| `res/layout/dialog_model_picker_bottom_sheet.xml` | **Create** | Root layout with header, search, tabs, ViewPager2, footer |
| `res/layout/fragment_model_list.xml` | **Create** | Per-tab fragment with RecyclerView + empty/loading states |
| `res/layout/item_model_picker.xml` | **Create** | Single model row: star, name, provider chip, context badge, select |
| `ModelPickerBottomSheet.kt` | **Create** | BottomSheetDialogFragment implementation |
| `ModelPickerViewModel.kt` | **Create** | ViewModel with state management, filtering, sorting |
| `FragmentModelList.kt` | **Create** | Fragment for each tab (reused with argument) |
| `model/PickerModel.kt` | **Create** | Data class + PickerUiState sealed interface + SortMode enum |
| `AgentChatActivity.kt` | **Modify** | Replace `ExposedDropdownMenu` with button that opens sheet |
| `activity_agent.xml` | **Modify** | Replace model TextInputLayout with `TextView` button + chevron |
| `res/values/colors.xml` | **Add** | 12 provider chip background colors |
| `res/drawable/ic_star.xml` | **Add** | Filled star icon for pinned state |
| `res/drawable/ic_star_outline.xml` | **Add** | Outline star icon for unpinned state |
| `res/drawable/ic_search.xml` | **Add** | Magnifying glass icon (if not present) |
| `res/drawable/ic_sort.xml` | **Add** | Sort icon for chip |
| `res/drawable/ic_close.xml` | **Add** | X icon for close button (if not present) |
| `res/drawable/bg_context_badge.xml` | **Add** | Rounded rectangle shape for context badge |
| `res/values/strings.xml` | **Add** | ~15 strings: labels, hints, descriptions, empty states |
| `ModelPickerBottomSheetTest.kt` | **Create** | Unit tests for ViewModel filter/sort logic |
| `ModelPickerBottomSheetUITest.kt` | **Create** | Instrumented test for sheet layout, tab switch, search |
