# AGENTS.md - Project Context for AI Assistants

## Project Overview

- **Name**: ClawDroid
- **Type**: Native Android App (Kotlin)
- **Min SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: Latest stable
- **Build**: Gradle with Kotlin DSL
- **Purpose**: Run PicoClaw CLI application on Android via terminal emulator integration
- **PicoClaw**: Go-based AI assistant, <10MB RAM, static ARM64 binary at `app/src/main/assets/picoclaw/picoclaw-arm64` (bundled with app)

## Docker Development Container

**ALL development commands MUST be run inside the Docker build container.** The container is always running and accessed via `docker compose exec`.

```bash
# Start the container (keep running in background)
make up                                 # docker compose up -d

# Build APK
make build-debug                        # docker compose exec build ./gradlew assembleDebug
make build-release                      # docker compose exec build ./gradlew assembleRelease
make clean                              # docker compose exec build ./gradlew clean
```

### Testing

```bash
# Unit Tests (inside container)
make test-unit                          # docker compose exec build ./gradlew test
make test-unit-debug                    # docker compose exec build ./gradlew testDebugUnitTest

# Instrumented Tests (requires emulator connected)
make test-integration                   # docker compose exec build ./gradlew connectedAndroidTest

# Connect to remote emulator
make adb-connect                        # docker compose exec build adb connect <ip>:5555
make adb-install                        # build + adb install APK on emulator
```

### Code Quality

```bash
make lint                               # docker compose exec build ./gradlew lint
make quality-check                      # lint + test + assembleDebug
```

### Emulator

```bash
# Auto-discover emulator on the local network (scans for port 5555)
make adb-find                           # dynamically finds and connects

# Or connect to a specific known IP
make adb-connect EMULATOR_IP=192.168.204.107

# Or start a local emulator via Redroid (requires kernel modules)
make emulator-up                        # docker compose --profile emulator up -d
```

### Docker Management

```bash
# Interactive shell inside container
make shell                              # docker compose exec build bash

# Rebuild the container image
make build-image                        # docker compose build build

# View logs
make logs                               # docker compose logs -f
```

### ADB Inside Container

The container uses `network_mode: host`, so ADB can reach devices on the host network. Connect to a remote emulator:

```bash
make adb-connect EMULATOR_IP=192.168.204.107
adb devices                              # verify connection
```

## Code Style Guidelines

### Kotlin

**Naming**:
- Classes: PascalCase (e.g., `MainActivity`, `PicoClawConfig`)
- Functions/Properties: camelCase (e.g., `onCreate`, `configFilePath`)
- Constants: UPPER_SNAKE_CASE (e.g., `MIN_SDK_VERSION`)
- XML Layouts: snake_case matching activity/fragment (e.g., `activity_main.xml`)

**Imports**:
- AndroidX imports preferred over Android platform
- Group: Android SDK → AndroidX → Third-party → Project
- No wildcard imports (`import com.example.clawdroid.*` is forbidden)

**Formatting**:
- 4 spaces indentation
- One expression per line
- Opening brace at end of line (K&R style)
- Maximum line length: 120 characters

### Resources (XML)

**Naming**:
- Layouts: `{component_type}_{description}.xml` (e.g., `activity_main.xml`, `fragment_config.xml`)
- Drawables: `ic_{description}.xml` for icons, `bg_{description}.xml` for backgrounds
- Strings: Use `String.format` over concatenation in Kotlin code

### Testing

**Unit Tests** (`app/src/test/`):
- Framework: JUnit 4 + Mockito
- Naming: `{ClassUnderTest}Test.kt` (e.g., `ConfigViewModelTest.kt`)
- Method naming: `{methodName}_{scenario}_{expectedResult}`
- Use `@RunWith(MockitoJUnitRunner::class)` for Mockito

**Instrumented Tests** (`app/src/androidTest/`):
- Framework: Espresso
- Naming: `{ScreenName}Test.kt` (e.g., `MainActivityTest.kt`)
- Use `@RunWith(AndroidJUnit4::class)`
- Prefer `onView()` with matchers over `UiAutomator`

### Error Handling

- Use Kotlin Result or sealed class for operation outcomes
- Display user-friendly Toast/Snackbar for errors
- Log with `Log.e(TAG, message)` for debugging
- Never expose raw exceptions to UI

## Project Structure

```
app/src/
├── main/
│   ├── java/com/example/clawdroid/
│   │   ├── MainActivity.kt
│   │   ├── config/           # Configuration related
│   │   ├── server/           # Web server
│   │   ├── terminal/         # Terminal emulator integration
│   │   └── model/            # Data models
│   ├── res/
│   │   ├── layout/           # XML layouts
│   │   ├── values/           # Strings, colors, themes
│   │   └── drawable/         # Icons, backgrounds
│   └── AndroidManifest.xml
├── test/                     # Unit tests (JUnit + Mockito)
└── androidTest/              # Instrumented tests (Espresso)
```

## Environment

- **Development**: Local machine with Android Studio or command-line
- **Testing**: Android emulator (API 21+)
- **Build**: Gradle via command line or Android Studio

---

For ticket management framework, see [.tickets/AGENTS.md](.tickets/AGENTS.md)