# Color and Theme Additions Needed for Dashboard Card Design

This document lists all color resources and theme attributes required for the new `activity_main_dashboard.xml` layout.

## Existing Colors (Keep)

These colors already exist in `app/src/main/res/values/colors.xml` and are sufficient for the MD3 theme base:

| Color Name | Hex Value | Role |
|------------|-----------|------|
| `primary` | #FF6750A4 | Theme primary (purple) |
| `primary_variant` | #FF7C4DFF | Primary variant |
| `secondary` | #FF03DAC6 | Theme secondary (teal) |
| `background` | #FFFFFBFE | Light background |
| `surface` | #FFFFFBFE | Light surface |
| `on_primary` | #FFFFFFFF | Text/icons on primary |
| `on_secondary` | #FF000000 | Text/icons on secondary |
| `on_background` | #FF1C1B1F | Text on background |
| `on_surface` | #FF1C1B1F | Text on surface |

## New Colors to Add

Add these to `app/src/main/res/values/colors.xml`:

```xml
<!-- Semantic Status Colors -->
<!-- These map to the status chip backgrounds and ensure consistency -->
<color name="status_running">#FF2E7D32</color>       <!-- Dark green for "Running" chip -->
<color name="status_stopped">#FFC62828</color>       <!-- Dark red for "Stopped" chip -->
<color name="status_loading">#FFF9A825</color>       <!-- Amber for "Loading" chip -->
<color name="status_offline">#FF757575</color>       <!-- Gray for "Offline" chip -->
<color name="status_online">#FF2E7D32</color>        <!-- Same green for "Online" chip -->
<color name="status_error">#FFD32F2F</color>        <!-- Bright red for error states -->

<!-- Surface variants for card depth -->
<color name="surface_variant">#FFE7E0EC</color>     <!-- M3 surface variant (light) -->
<color name="on_surface_variant">#FF49454F</color> <!-- M3 on-surface variant (light) -->
```

## New Theme Attributes to Add

Update `app/src/main/res/values/themes.xml` to include MD3 semantic error colors (already partially covered by Material3 parent, but explicit declarations ensure consistency):

```xml
<style name="Theme.ClawDroid" parent="Theme.Material3.DayNight.NoActionBar">
    <!-- Existing items -->
    <item name="colorPrimary">@color/primary</item>
    <item name="colorPrimaryVariant">@color/primary_variant</item>
    <item name="colorOnPrimary">@color/on_primary</item>
    <item name="colorSecondary">@color/secondary</item>
    <item name="colorOnSecondary">@color/on_secondary</item>
    <item name="android:colorBackground">@color/background</item>
    <item name="colorSurface">@color/surface</item>
    <item name="colorOnSurface">@color/on_surface</item>

    <!-- NEW: Explicit error palette (Material3) -->
    <item name="colorError">@color/status_error</item>
    <item name="colorOnError">@android:color/white</item>
    <item name="colorErrorContainer">#FFF9DEDC</item>   <!-- M3 light error container -->
    <item name="colorOnErrorContainer">#FF410E0B</item> <!-- M3 light on-error container -->

    <!-- NEW: Surface variants -->
    <item name="colorSurfaceVariant">@color/surface_variant</item>
    <item name="colorOnSurfaceVariant">@color/on_surface_variant</item>

    <!-- NEW: Status-aware color attributes (for programmatic chip tinting) -->
    <!-- These are custom attributes that Kotlin code can reference -->
    <item name="colorStatusRunning">@color/status_running</item>
    <item name="colorStatusStopped">@color/status_stopped</item>
    <item name="colorStatusLoading">@color/status_loading</item>
    <item name="colorStatusOffline">@color/status_offline</item>
    <item name="colorStatusOnline">@color/status_online</item>
</style>
```

> **Note**: `colorStatusRunning`, etc. are custom theme attributes. To use them in code, declare them in `res/values/attrs.xml`:
>
> ```xml
> <resources>
>     <attr name="colorStatusRunning" format="color" />
>     <attr name="colorStatusStopped" format="color" />
>     <attr name="colorStatusLoading" format="color" />
>     <attr name="colorStatusOffline" format="color" />
>     <attr name="colorStatusOnline" format="color" />
> </resources>
> ```

## Night/Dark Theme Values

The `Theme.Material3.DayNight.NoActionBar` parent handles most dark-theme inversion automatically. However, the semantic status colors should remain **semantically consistent** across themes:

- **Green** always means "running / online / healthy"
- **Red** always means "stopped / error"
- **Amber** always means "loading / transitional"
- **Gray** always means "offline / not applicable"

No dark-theme-specific color changes are needed for the semantic status colors because Chips use container tints that contrast well in both modes. If you want dark-theme variants, create `values-night/colors.xml`:

```xml
<!-- values-night/colors.xml -->
<resources>
    <!-- Dark surfaces -->
    <color name="background">#FF1C1B1F</color>
    <color name="surface">#FF1C1B1F</color>
    <color name="surface_variant">#FF49454F</color>
    <color name="on_surface_variant">#FFCAC4D0</color>

    <!-- Error container dark -->
    <color name="error_container">#FF8C1D18</color>
    <color name="on_error_container">#FFF9DEDC</color>
</resources>
```

## Drawable Icons Needed

The new layout references several icon drawables that must be created (or use Material Icons from `androidx.compose.material:material-icons-extended` or vector assets):

| Drawable Name | Description | Suggested Vector Asset |
|-------------|-------------|----------------------|
| `ic_claw_logo` | App logo in toolbar | Custom app icon (24dp) |
| `ic_bootstrap` | Gear / download icon | `ic_settings` or `ic_download` |
| `ic_picoclaw` | Robot / claw icon | Custom or `ic_smart_toy` |
| `ic_server` | Server / DNS icon | `ic_dns` or `ic_storage` |
| `ic_pending` | Clock / pending icon | `ic_pending` |
| `ic_play` | Play triangle | `ic_play_arrow` |
| `ic_stop` | Stop square | `ic_stop` |
| `ic_refresh` | Circular arrow | `ic_refresh` |
| `ic_globe` | Globe / web | `ic_public` |
| `ic_logs` | Document / list | `ic_description` or `ic_list` |
| `ic_settings` | Cog | `ic_settings` |
| `ic_help` | Question mark | `ic_help` |
| `ic_offline` | Cloud off | `ic_cloud_off` |
| `ic_error` | Alert triangle | `ic_error` |

**How to add**: In Android Studio, right-click `res/drawable` → **New** → **Vector Asset** → choose from Material Icons.

## Menu Resource Needed

Create `res/menu/menu_main.xml` for the toolbar overflow:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/action_help"
        android:title="@string/menu_help"
        android:icon="@drawable/ic_help"
        app:showAsAction="never" />
    <item
        android:id="@+id/action_about"
        android:title="@string/menu_about"
        app:showAsAction="never" />
</menu>
```

## Summary of Files to Modify

1. `app/src/main/res/values/colors.xml` — add semantic status colors + surface variants
2. `app/src/main/res/values/themes.xml` — add error palette + surface variant items
3. `app/src/main/res/values/attrs.xml` — create file for custom status color attributes
4. `app/src/main/res/values/strings.xml` — add all new strings (see `strings-needed.md`)
5. `app/src/main/res/values-night/colors.xml` — optional dark theme surface overrides
6. `app/src/main/res/menu/menu_main.xml` — create toolbar menu
7. `app/src/main/res/drawable/` — add vector icon assets (13 icons)
