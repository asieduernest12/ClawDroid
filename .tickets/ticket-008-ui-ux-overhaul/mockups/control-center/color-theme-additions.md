# Color and Theme Additions for Control Center Design

These color and theme additions must be merged into the existing resource files.

---

## 1. Colors (`app/src/main/res/values/colors.xml`)

Add the following color definitions:

```xml
<!-- ========================================= -->
<!-- Control Center Tile Backgrounds           -->
<!-- ========================================= -->

<!-- Status tile: shifts dynamically in code per state -->
<color name="tile_status_stopped">#FFECEFF1</color>   <!-- cool gray -->
<color name="tile_status_starting">#FFFFF3E0</color>  <!-- warm amber -->
<color name="tile_status_running">#FFE8F5E9</color>   <!-- cool green -->
<color name="tile_status_error">#FFFFEBEE</color>     <!-- soft red -->

<!-- Fixed tile backgrounds -->
<color name="tile_bootstrap">#FFF3E5F5</color>         <!-- light purple -->
<color name="tile_server">#FFE3F2FD</color>           <!-- light blue -->
<color name="tile_mission_control">#FF6750A4</color>  <!-- primary purple -->
<color name="tile_logs">#FF0277BD</color>              <!-- deep blue -->

<!-- ========================================= -->
<!-- Toggle Button Backgrounds                 -->
<!-- ========================================= -->
<color name="toggle_start">#FF00C853</color>           <!-- emerald green -->
<color name="toggle_stop">#FFD50000</color>            <!-- crimson red -->
<color name="toggle_restart">#FFFFAB00</color>         <!-- amber -->
<color name="toggle_disabled">#FFBDBDBD</color>        <!-- gray 400 -->

<!-- ========================================= -->
<!-- Status Indicators                         -->
<!-- ========================================= -->
<color name="status_dot_stopped">#FFD50000</color>
<color name="status_dot_starting">#FFFFAB00</color>
<color name="status_dot_running">#FF00C853</color>
<color name="status_dot_error">#FFD50000</color>

<!-- ========================================= -->
<!-- Signal Strength Bars                      -->
<!-- ========================================= -->
<color name="signal_active">#FF00C853</color>
<color name="signal_inactive">#FFBDBDBD</color>

<!-- ========================================= -->
<!-- Progress Track                            -->
<!-- ========================================= -->
<color name="progress_track">#FFE0E0E0</color>

<!-- ========================================= -->
<!-- Text / Surface Variants (Material 3)    -->
<!-- ========================================= -->
<color name="on_surface_variant">#FF49454F</color>
```

### Keep Existing Colors
The existing colors should remain as they are used by the application theme:
```xml
<color name="black">#FF000000</color>
<color name="white">#FFFFFFFF</color>
<color name="primary">#FF6750A4</color>
<color name="primary_variant">#FF7C4DFF</color>
<color name="secondary">#FF03DAC6</color>
<color name="background">#FFFFFBFE</color>
<color name="surface">#FFFFFBFE</color>
<color name="on_primary">#FFFFFFFF</color>
<color name="on_secondary">#FF000000</color>
<color name="on_background">#FF1C1B1F</color>
<color name="on_surface">#FF1C1B1F</color>
```

---

## 2. Theme Additions (`app/src/main/res/values/themes.xml`)

The existing `Theme.ClawDroid` can remain unchanged as it already uses `Theme.Material3.DayNight.NoActionBar`, which provides the typography and component styles we rely on.

However, add the following **optional overlay** for the Control Center if you want to force rounded shapes globally:

```xml
<style name="Theme.ClawDroid.ControlCenter" parent="Theme.ClawDroid">
    <!-- Enforce rounded corners on all MaterialCardViews by default -->
    <item name="materialCardViewStyle">@style/Widget.ClawDroid.Card</item>
    <!-- Ensure ripple uses the accent color -->
    <item name="colorControlHighlight">@color/primary_variant</item>
</style>

<style name="Widget.ClawDroid.Card" parent="Widget.Material3.CardView.Elevated">
    <item name="cardCornerRadius">16dp</item>
    <item name="cardElevation">2dp</item>
</style>
```

> **Note:** The overlay is optional. The mockup layout already sets `app:cardCornerRadius` and `app:cardElevation` explicitly on each card, so the theme overlay is a convenience for future screens.

---

## 3. Drawable Resources Needed

Create the following drawables in `app/src/main/res/drawable/`:

### `bg_status_dot.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/status_dot_stopped" />
</shape>
```

### `bg_signal_bar.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/signal_inactive" />
    <corners android:radius="2dp" />
</shape>
```

### Vector Icons (as Vector Drawables)
You will need the following icons. Use Material Icons from `com.google.android.material:material` or create custom vector drawables:

| File Name | Material Icon Name | Usage |
|-----------|--------------------|-------|
| `ic_play.xml` | `play_arrow` | Start button |
| `ic_stop.xml` | `stop` | Stop button |
| `ic_restart.xml` | `refresh` | Restart button |
| `ic_dashboard.xml` | `dashboard` | Mission Control tile |
| `ic_logs.xml` | `description` | View Logs tile |
| `ic_settings.xml` | `settings` | Settings button |
| `ic_bootstrap.xml` | `build_circle` | Bootstrap tile |
| `ic_server.xml` | `dns` or `storage` | Server tile |

Example vector drawable skeleton:
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M8,5 ..." />
</vector>
```

---

## 4. Night Mode Colors (`values-night/colors.xml`)

Create `app/src/main/res/values-night/colors.xml` for dark theme variants:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Dark variant of tile backgrounds -->
    <color name="tile_status_stopped">#FF37474F</color>
    <color name="tile_status_starting">#FF4E342E</color>
    <color name="tile_status_running">#FF1B5E20</color>
    <color name="tile_status_error">#FF4A0000</color>
    <color name="tile_bootstrap">#FF311B92</color>
    <color name="tile_server">#FF0D47A1</color>
    <color name="tile_mission_control">#FF6750A4</color>
    <color name="tile_logs">#FF01579B</color>

    <!-- Toggles stay high-contrast in dark mode -->
    <color name="toggle_start">#FF00E676</color>
    <color name="toggle_stop">#FFFF5252</color>
    <color name="toggle_restart">#FFFFD740</color>
    <color name="toggle_disabled">#FF757575</color>

    <!-- Status dots -->
    <color name="status_dot_stopped">#FFFF5252</color>
    <color name="status_dot_starting">#FFFFD740</color>
    <color name="status_dot_running">#FF00E676</color>
    <color name="status_dot_error">#FFFF5252</color>

    <!-- Signal bars -->
    <color name="signal_active">#FF00E676</color>
    <color name="signal_inactive">#FF616161</color>
    <color name="progress_track">#FF424242</color>

    <!-- Text -->
    <color name="on_surface_variant">#FFCAC4D0</color>
</resources>
```

---

## 5. Dependencies Check

Ensure the following dependencies are present in `app/build.gradle.kts`:

```kotlin
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
```

The `SwipeRefreshLayout` is new compared to the original layout; verify it is included.
