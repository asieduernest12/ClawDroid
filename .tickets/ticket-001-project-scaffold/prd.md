# Ticket-001: Android Project Scaffold and Build System

## 1. Problem Statement

The ClawDroid project has no Android application structure. Before any feature work can begin, we need a working Android project with Gradle build system, correct SDK configuration, proper project layout, and a build that compiles. Without this foundation, no development, testing, or emulator validation is possible.

Business Impact:
- Zero starting point — no code can be written or tested until scaffolding exists
- Blocking all downstream tickets (configuration, web server, terminal integration)
- No CI/CD or emulator testing pipeline without a buildable artifact

## 2. Proposed Solution

Create a standard Android project structure using Gradle with Kotlin DSL, targeting min SDK 21 with compile SDK set to latest stable. Scaffold will include:
- Root `build.gradle.kts` and `settings.gradle.kts` with Kotlin DSL configuration
- `app/` module with `build.gradle.kts` including AndroidX, Espresso, JUnit dependencies
- Basic `AndroidManifest.xml` declaring application package and permissions
- Empty `MainActivity.kt` with themed application entry point
- Standard directory layout (`java/`, `res/`, `test/`, `androidTest/`)

## 3. Acceptance Criteria

### Functional Requirements
- [x] `./gradlew projects` produces output including `:app` project
- [x] `./gradlew assembleDebug` completes with BUILD SUCCESSFUL
- [x] APK artifact exists at `app/build/outputs/apk/debug/app-debug.apk` (6.1M)
- [x] AndroidManifest.xml declares `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE`
- [x] Application package is `com.example.clawdroid`
- [x] minSdkVersion is 21, targetSdkVersion is latest stable

### Quality Requirements
- [x] `./gradlew lint` produces zero errors
- [x] Build completes in under 60 seconds on development machine
- [x] No deprecated API usage in build configuration
- [x] Project follows standard Android Gradle project conventions

### Development Requirements
- [x] AndroidX dependencies declared for AppCompat, Core KTX, Activity KTX
- [x] JUnit 4 and Mockito declared in `test` dependencies
- [x] Espresso declared in `androidTest` dependencies
- [x] Kotlin standard library included via kotlin-android plugin
- [x] Material Design 3 (Material You) theme declared in `res/values/themes.xml`

## 4. Technical Considerations

### Implementation Constraints
- Must use Kotlin DSL for Gradle (not Groovy)
- Version catalog (libs.versions.toml) preferred for dependency management
- No Compose — use traditional XML layouts (unless explicitly required downstream)
- AGP (Android Gradle Plugin) version must be compatible with Kotlin version
- Gradle wrapper must be included in VCS

### Directory Structure
```
clawdroid/
├── build.gradle.kts              # Root build file
├── settings.gradle.kts           # Include :app module
├── gradle.properties             # AndroidX, JVM args, Kotlin settings
├── gradle/
│   └── libs.versions.toml        # Version catalog (optional)
├── gradlew                       # Gradle wrapper script
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts          # App module build file
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/clawdroid/
        │   │   └── MainActivity.kt
        │   └── res/
        │       ├── values/
        │       │   ├── strings.xml
        │       │   ├── colors.xml
        │       │   └── themes.xml
        │       └── drawable/
        │           └── ic_launcher_background.xml
        ├── test/java/com/example/clawdroid/
        │   └── ExampleUnitTest.kt
        └── androidTest/java/com/example/clawdroid/
            └── ExampleInstrumentedTest.kt
```

### Performance
- Build time should be minimal for clean build (< 60s)
- Gradle configuration cache should be enabled

## 5. Dependencies

- None — this is the foundational ticket

## 6. Subtask Checklist

- [x] Task 1: Create root Gradle project with Kotlin DSL
    - **Problem**: Need Gradle build root with Kotlin DSL configuration
    - **Test**: `./gradlew projects` lists `:app` subproject
    - **Subtasks**:
        - [x] Subtask 1.1: Create root `build.gradle.kts` with AGP and Kotlin plugin declarations
            - **Objective**: Declare Android Gradle Plugin and Kotlin plugins at root level
            - **Test**: `./gradlew buildEnvironment` shows AGP and Kotlin plugins
            - **Depends on**: None
        - [x] Subtask 1.2: Create `settings.gradle.kts` with `:app` module include
            - **Objective**: Register `:app` module in Gradle settings
            - **Test**: `./gradlew projects` output includes `:app`
            - **Depends on**: None
        - [x] Subtask 1.3: Create `gradle.properties` with AndroidX and JVM config
            - **Objective**: Enable AndroidX, set JVM args for Kotlin
            - **Test**: Build uses AndroidX and Kotlin JVM target 17
            - **Depends on**: None
        - [x] Subtask 1.4: Initialize Gradle wrapper
            - **Objective**: Generate `gradlew` and `gradle/wrapper/` files
            - **Test**: `./gradlew --version` succeeds
            - **Depends on**: None

- [x] Task 2: Create `app/` module with build configuration
    - **Problem**: Android application module needs build.gradle.kts with all dependencies
    - **Test**: `./gradlew :app:dependencies` resolves all declared dependencies
    - **Subtasks**:
        - [x] Subtask 2.1: Create `app/build.gradle.kts` with Android application plugin
            - **Objective**: Configure android block with namespace, compileSdk, minSdk, targetSdk, versionCode, versionName
            - **Test**: `./gradlew :app:assembleDebug` succeeds
            - **Depends on**: Task 1
        - [x] Subtask 2.2: Add AndroidX, JUnit, Mockito, and Espresso dependencies
            - **Objective**: Declare all required test and runtime dependencies
            - **Test**: `./gradlew :app:dependencies` includes AppCompat, JUnit, Mockito, Espresso
            - **Depends on**: Subtask 2.1

- [x] Task 3: Create AndroidManifest.xml and resource files
    - **Problem**: Need Android manifest with permissions and application declaration
    - **Test**: `./gradlew :app:assembleDebug` succeeds with manifest
    - **Subtasks**:
        - [x] Subtask 3.1: Create `AndroidManifest.xml` with `<application>` tag
            - **Objective**: Declare application with theme, label, and launcher activity intent filter
            - **Test**: Manifest passes AAPT validation during build
            - **Depends on**: Task 2
        - [x] Subtask 3.2: Create values resource files (strings.xml, colors.xml, themes.xml)
            - **Objective**: Define app name string, color palette, Material 3 theme
            - **Test**: Resource compilation succeeds with no missing resource errors
            - **Depends on**: Subtask 3.1
        - [x] Subtask 3.3: Add INTERNET and ACCESS_NETWORK_STATE permissions
            - **Objective**: Declare network permissions for web server functionality
            - **Test**: APK manifest includes both permissions
            - **Depends on**: Subtask 3.1

- [x] Task 4: Create MainActivity and verify build
    - **Problem**: Need a minimal launchable activity to verify the APK works
    - **Test**: `./gradlew :app:assembleDebug` produces a valid APK
    - **Subtasks**:
        - [x] Subtask 4.1: Create `MainActivity.kt` extending `AppCompatActivity`
            - **Objective**: Minimal launcher activity with `setContentView(R.layout.activity_main)`
            - **Test**: Activity compiles and is referenced in manifest
            - **Depends on**: Task 3
        - [x] Subtask 4.2: Create `activity_main.xml` layout with basic TextView
            - **Objective**: Simple layout showing "ClawDroid" welcome text
            - **Test**: Layout compiles without errors
            - **Depends on**: Subtask 4.1
        - [x] Subtask 4.3: Verify full build produces debug APK
            - **Objective**: Confirm end-to-end build produces a valid APK artifact
            - **Test**: `ls app/build/outputs/apk/debug/app-debug.apk` exists
            - **Depends on**: Subtask 4.2

- [x] Task 5: Create unit and instrumented test stubs
    - **Problem**: Need test infrastructure in place for downstream ticket testing
    - **Test**: `./gradlew test` succeeds (instrumented tests require emulator)
    - **Subtasks**:
        - [x] Subtask 5.1: Create `ExampleUnitTest.kt` with JUnit 4 test
            - **Objective**: Verify JUnit 4 works with a simple assertion
            - **Test**: `./gradlew testDebugUnitTest` passes
            - **Depends on**: Task 4
        - [s] Subtask 5.2: Create `ExampleInstrumentedTest.kt` with Espresso
            - **Objective**: Verify Espresso can launch app and find welcome text
            - **Test**: `./gradlew connectedAndroidTest` passes (on emulator)
            - **Depends on**: Task 4  <!-- Defer: requires emulator connection -->

- [x] Task 6: Run full quality check
    - **Problem**: Verify code quality before moving to next ticket
    - **Test**: `./gradlew lint && ./gradlew test && ./gradlew assembleDebug` all exit 0
    - **Subtasks**:
        - [x] Subtask 6.1: Run lint and fix any warnings
            - **Objective**: Zero lint errors
            - **Test**: `./gradlew lint` exit code 0
            - **Depends on**: Task 5
        - [x] Subtask 6.2: Verify clean build
            - **Objective**: Full clean build passes
            - **Test**: `./gradlew clean assembleDebug` succeeds
            - **Depends on**: Subtask 6.1