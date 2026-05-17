# ClawDroid Project Requirements Document

## Project Overview
ClawDroid is an Android application designed to run PicoClaw (an ultra-lightweight Go-based AI assistant) on Android devices by leveraging existing Android terminal solutions like Termux and LibreTerm.

## Functional Goals
1. Application startup and initialization on Android devices
2. Configuration of PicoClaw within the Android environment
3. Ability to open a configuration page or simple Mission Control web page locally on the Android device
4. Testing will be performed using Android emulators

## Non-Functional Requirements
- Compatibility with Android API level 21 (Lollipop) and above
- Efficient resource usage (memory, battery)
- Secure handling of any local web servers or network communications
- User-friendly interface for configuration and mission control

## Dependencies
- Termux or similar Android terminal emulator
- LibreTerm (LibTurmox) for terminal functionality
- PicoClaw ARM64 binary (bundled with app or downloaded on first launch)
- Local web server capability for Mission Control page

## Success Criteria
- Application launches successfully on Android emulator/device ✅
- PicoClaw can be configured and executed within the app ✅
- Mission Control web page is accessible locally on the device ✅
- No critical errors or crashes during normal operation ✅
- **15 BDD acceptance tests pass across 4 scenario classes** ✅

## Testing Implementation
- **Framework**: JUnit 4 + Espresso (fallback from JGiven)  
- **Structure**: 3 stage classes + 4 scenario classes
- **Coverage**:
  - AppLaunchScenario: 2 tests (app launch, navigation)
  - ConfigScenario: 5 tests (config save, validation, reset, persistence)
  - ServerScenario: 3 tests (HTTP endpoints, health check, dashboard)
  - TerminalScenario: 5 tests (bootstrap, start/stop, status, caching)
- **Runner**: `AcceptanceTestSuite.kt` (JUnit 4 @Suite)
- **Build Integration**: `make test-e2e` target

## Future Enhancements
- Integration with Android notifications for alerts
- Background service operation
- Enhanced UI for mission control
- Support for multiple claw configurations

## Architecture Decision
**PicoClaw** was chosen over NanoClaw/OpenClaw because:
- Static Go binary, no runtime dependencies — ideal for Android
- <10MB RAM footprint, <1s startup
- Native ARM64 support
- Single binary can be bundled or downloaded
- MIT licensed (github.com/sipeed/picoclaw)
