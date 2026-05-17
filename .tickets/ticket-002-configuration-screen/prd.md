# Ticket-002: PicoClaw Configuration Screen

## 1. Problem Statement

PicoClaw requires user-specific configuration (paths, preferences, credentials) to function. Without a configuration interface, users cannot provide the necessary settings for PicoClaw execution. The configuration must persist across application restarts and provide validation to prevent invalid states.

Business Impact:
- No way to supply PicoClaw with required runtime parameters
- Poor user experience — users need a UI, not raw file editing
- Configuration errors lead to runtime failures that are hard to diagnose

## 2. Proposed Solution

Implement a configuration screen accessible from the main activity that allows users to view and modify PicoClaw settings. The solution includes:
- **ConfigActivity** with form fields for all PicoClaw parameters
- **SharedPreferences-based persistence** for simple key-value storage
- **LiveData/ViewModel** architecture for reactive UI updates
- **Input validation** providing immediate user feedback
- **Default values** for all settings with a "Reset to Defaults" action

### Configuration Parameters
| Field | Type | Default | Description |
|-------|------|---------|-------------|
| Binary Path | String | `/data/data/com.example.clawdroid/files/picoclaw/picoclaw-arm64` | Path to PicoClaw binary |
| Config Dir | String | `/data/data/com.example.clawdroid/files/picoclaw` | PicoClaw config directory |
| Server Port | Integer | 8080 | Mission Control web server port |
| Auto-start | Boolean | false | Start PicoClaw on app launch |
| Log Level | Enum (DEBUG, INFO, WARN, ERROR) | INFO | Logging verbosity |

## 3. Acceptance Criteria

### Functional Requirements
- [x] User can navigate from MainActivity to ConfigActivity via button/menu
- [x] Configuration screen displays all PicoClaw parameters with current values
- [x] User can modify each parameter (text input, switch, dropdown)
- [x] Changes persist after configuration screen is closed and reopened
- [x] "Reset to Defaults" restores all fields to default values
- [x] Invalid port numbers (outside 1024-65535) show validation error
- [x] Empty path fields show validation error
- [x] Configuration is available to other application components via a repository/manager class

### Quality Requirements
- [x] Configuration loads in under 200ms
- [x] No data loss on configuration save (transactional writes)
- [x] UI updates reactively when configuration changes
- [x] Validation errors displayed inline next to the relevant field
- [x] Configuration manager thread-safe (read/write from any thread)

### Development Requirements
- [x] ConfigActivity uses ViewModel + LiveData/StateFlow pattern
- [x] Configuration stored via SharedPreferences (EncryptedSharedPreferences preferred)
- [x] Unit tests for ConfigViewModel cover save, load, reset, and validation
- [x] Instrumented test verifies ConfigActivity opens and displays defaults

## 4. Technical Considerations

### Implementation Approach
- Use MVVM architecture: ConfigActivity (View) -> ConfigViewModel -> ConfigRepository
- ConfigRepository wraps SharedPreferences for persistence
- Form validation in ViewModel using Kotlin Result or sealed class
- Factory pattern for ViewModel to inject ConfigRepository

### Files to Create
```
app/src/main/java/com/example/clawdroid/
├── config/
│   ├── ConfigActivity.kt         # Configuration screen activity
│   ├── ConfigViewModel.kt        # ViewModel with form state
│   ├── ConfigRepository.kt       # Persistence layer
│   └── model/
│       ├── PicoClawConfig.kt     # Data class for configuration
│       └── ConfigValidationResult.kt  # Sealed class for validation
app/src/main/res/layout/
└── activity_config.xml           # Form layout
app/src/test/java/com/example/clawdroid/config/
├── ConfigViewModelTest.kt
└── ConfigRepositoryTest.kt
app/src/androidTest/java/com/example/clawdroid/config/
└── ConfigActivityTest.kt
```

### Performance
- SharedPreferences access is synchronous and fast (< 5ms)
- ViewModel survives configuration changes
- Lazy loading of preferences on first access

### Security
- Use EncryptedSharedPreferences for sensitive values if needed
- Never log configuration values at DEBUG level

## 5. Dependencies

- **Depends on ticket-001**: Project scaffold must compile before UI can be added

## 6. Subtask Checklist

- [x] Task 1: Create PicoClawConfig data model and ConfigValidationResult
    - **Problem**: Need data classes for configuration parameters and validation
    - **Test**: Data class serializes/deserializes correctly with SharedPreferences
    - **Subtasks**:
        - [x] Subtask 1.1: Create PicoClawConfig data class with all fields and defaults
            - **Objective**: Define configuration model with sensible defaults
            - **Test**: Default config object has expected default values
            - **Depends on**: None
        - [x] Subtask 1.2: Create ConfigValidationResult sealed class
            - **Objective**: Define validation success/failure with field-level errors
            - **Test**: Validation results correctly report field errors
            - **Depends on**: Subtask 1.1

- [x] Task 2: Implement ConfigRepository
    - **Problem**: Need persistence layer for configuration
    - **Test**: ConfigRepository saves, loads, and resets configuration
    - **Subtasks**:
        - [x] Subtask 2.1: Create ConfigRepository with SharedPreferences
            - **Objective**: Implement save, load, reset operations
            - **Test**: Load returns saved values, reset restores defaults
            - **Depends on**: Task 1
        - [x] Subtask 2.2: Add validation logic to ConfigRepository
            - **Objective**: Validate port range and non-empty paths
            - **Test**: Invalid port returns PortOutOfRange error
            - **Depends on**: Subtask 2.1

- [x] Task 3: Create ConfigViewModel
    - **Problem**: Need ViewModel to bridge repository and UI
    - **Test**: ViewModel exposes config state, handles save/reset/validation
    - **Subtasks**:
        - [x] Subtask 3.1: Create ConfigViewModel with StateFlow
            - **Objective**: Reactive state management for configuration form
            - **Test**: ViewModel emits correct initial state from repository
            - **Depends on**: Task 2
        - [x] Subtask 3.2: Add save, reset, and validate actions to ViewModel
            - **Objective**: Handle user actions with validation
            - **Test**: Save persists via repository, reset restores defaults
            - **Depends on**: Subtask 3.1

- [x] Task 4: Create ConfigActivity UI
    - **Problem**: Need visual configuration form
    - **Test**: ConfigActivity displays all fields and responds to user input
    - **Subtasks**:
        - [x] Subtask 4.1: Create activity_config.xml layout
            - **Objective**: Form with text inputs, switch, dropdown, save/reset buttons
            - **Test**: Layout inflates without errors
            - **Depends on**: None
        - [x] Subtask 4.2: Create ConfigActivity with ViewModel binding
            - **Objective**: Wire UI to ViewModel and handle navigation
            - **Test**: Activity shows default values on launch
            - **Depends on**: Subtask 4.1, Task 3
        - [x] Subtask 4.3: Add validation error display in UI
            - **Objective**: Show inline error messages for invalid fields
            - **Test**: Invalid input shows error text next to field
            - **Depends on**: Subtask 4.2

- [x] Task 5: Add navigation from MainActivity to ConfigActivity
    - **Problem**: Users need a way to reach the configuration screen
    - **Test**: Button in MainActivity opens ConfigActivity
    - **Subtasks**:
        - [x] Subtask 5.1: Add "Settings" button to MainActivity layout
            - **Objective**: Visible button that launches ConfigActivity via Intent
            - **Test**: Button exists and is clickable
            - **Depends on**: Task 4
        - [x] Subtask 5.2: Implement Intent-based navigation
            - **Objective**: StartActivityForResult or simple startActivity
            - **Test**: Clicking button navigates to ConfigActivity
            - **Depends on**: Subtask 5.1

- [x] Task 6: Write tests and verify quality
    - **Problem**: Ensure configuration system works correctly
    - **Test**: All tests pass
    - **Subtasks**:
        - [x] Subtask 6.1: Write ConfigViewModel unit tests
            - **Objective**: Test save, load, reset, validation scenarios
            - **Test**: `./gradlew testDebugUnitTest --tests "com.example.clawdroid.config.ConfigViewModelTest"` passes
            - **Depends on**: Task 3
        - [x] Subtask 6.2: Write ConfigRepository unit tests
            - **Objective**: Test persistence and validation logic
            - **Test**: `./gradlew testDebugUnitTest --tests "com.example.clawdroid.config.ConfigRepositoryTest"` passes
            - **Depends on**: Task 2
        - [x] Subtask 6.3: Write ConfigActivity instrumented test
            - **Objective**: Verify UI displays defaults and responds to input
            - **Test**: `./gradlew connectedAndroidTest --tests "com.example.clawdroid.config.ConfigActivityTest"` passes
            - **Depends on**: Task 4
        - [x] Subtask 6.4: Run full quality check
            - **Objective**: Zero lint errors, all tests pass
            - **Test**: `./gradlew lint && ./gradlew test && ./gradlew assembleDebug` exit 0
            - **Depends on**: Subtasks 6.1-6.3