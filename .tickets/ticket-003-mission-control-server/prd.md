# Ticket-003: Mission Control Local Web Server

## 1. Problem Statement

Users need a visual interface to monitor and control PicoClaw operations in real-time. Running a standalone web browser to access a remote dashboard is impractical on a mobile device. ClawDroid must embed a lightweight HTTP server that serves a local Mission Control web page, providing status monitoring and basic controls without external dependencies.

Business Impact:
- No real-time visibility into PicoClaw status without this server
- Users forced to use external tools to monitor operations
- Missed opportunity for mobile-native control experience

## 2. Proposed Solution

Integrate a lightweight embedded HTTP server (NanoHTTPD) within ClawDroid that serves:
- **Mission Control HTML page** with live status dashboard (HTML/CSS/JS served from assets)
- **REST API** endpoints for PicoClaw status, start, stop, and configuration reload
- **Real-time updates** via a simple polling mechanism from JS fetch requests
- **Server lifecycle** tied to application lifecycle (start on app launch, stop on app close, configurable port)

### Architecture Overview
```
┌──────────────────────┐     HTTP (localhost:8080)     ┌──────────────────┐
│   Embedded Server    │ ◄──────────────────────────► │  Mobile Browser  │
│   (NanoHTTPD)        │                               │  / WebView       │
│                      │                               │                  │
│  GET / -> index.html │  ────►                         │  Mission Control │
│  GET /api/status     │  ◄────                         │  Dashboard       │
│  POST /api/start     │                               │                  │
│  POST /api/stop      │                               └──────────────────┘
│  GET /api/health     │
└──────────────────────┘
```

## 3. Acceptance Criteria

### Functional Requirements
- [x] Embedded HTTP server starts when application launches (configurable)
- [x] Server listens on configurable port (default 8080) from Ticket-002 config
- [x] `GET /` returns Mission Control HTML page with status dashboard
- [x] `GET /api/health` returns `{"status": "ok", "uptime": 123}`
- [x] `GET /api/status` returns current PicoClaw process status (running/stopped)
- [x] `POST /api/start` triggers PicoClaw launch and returns 200 on success
- [x] `POST /api/stop` triggers PicoClaw shutdown and returns 200 on success
- [x] Server responds within 5 seconds of app startup
- [x] Server stops when application is destroyed
- [x] Mission Control page refreshes status data every 3 seconds (auto-poll)

### Quality Requirements
- [x] Server overhead < 5 MB additional memory when idle
- [x] API endpoints respond in under 100ms
- [x] Server does not block main thread (runs on background coroutine/thread)
- [x] Graceful shutdown — no socket leaks or dangling threads
- [x] Handles port conflicts gracefully (logs warning, tries next port)

### Development Requirements
- [x] Unit tests for all API endpoint handlers
- [x] Instrumented test verifies server start, health endpoint, and shutdown
- [x] HTML/CSS/JS assets bundled as Android assets

## 4. Technical Considerations

### Implementation Approach
- Use NanoHTTPD library (single-JAR embedded Java HTTP server)
- Server runs on `AsyncTask` or `CoroutineScope` (Dispatchers.IO)
- API handlers implemented as NanoHTTPD `serve()` routing
- HTML/JS/CSS stored in `app/src/main/assets/mission-control/`
- Read configuration from ConfigRepository (port from Ticket-002)

### Files to Create
```
app/src/main/java/com/example/clawdroid/
├── server/
│   ├── MissionControlServer.kt     # NanoHTTPD subclass with API routes
│   ├── ServerManager.kt            # Server lifecycle manager (start/stop)
│   ├── ApiHandler.kt              # API route handlers
│   └── model/
│       ├── ServerStatus.kt         # Server status data class
│       └── ApiResponse.kt          # Standard JSON response format
app/src/main/assets/mission-control/
├── index.html                      # Mission Control dashboard
├── styles.css                      # Dashboard styling
└── app.js                          # Status polling and UI logic
app/src/test/java/com/example/clawdroid/server/
├── MissionControlServerTest.kt
└── ApiHandlerTest.kt
app/src/androidTest/java/com/example/clawdroid/server/
└── MissionControlServerInstrumentedTest.kt
```

### Dependencies
- NanoHTTPD (`org.nanohttpd:nanohttpd:2.3.1`) or similar

### Performance
- Background thread for HTTP listener (never on main thread)
- Minimal request/response parsing overhead
- Static file serving with MIME type detection
- JSON responses serialized with `org.json` (built-in Android)

### Security
- Server binds to `127.0.0.1` (localhost) only — never accessible from network
- No authentication required (localhost-only access)
- Sanitize any user input reflected in responses
- CORS headers set to allow only same-origin

## 5. Dependencies

- **Depends on ticket-001**: Project scaffold and build system
- **Depends on ticket-002**: Configuration screen for server port setting

## 6. Subtask Checklist

- [x] Task 1: Add NanoHTTPD dependency and create server infrastructure
    - **Problem**: Need HTTP server library and core server class
    - **Test**: Server starts and responds to HTTP requests
    - **Subtasks**:
        - [x] Subtask 1.1: Add NanoHTTPD dependency to `app/build.gradle.kts`
            - **Objective**: Declare NanoHTTPD library in dependencies
            - **Test**: `./gradlew :app:dependencies` includes nanohttpd
            - **Depends on**: ticket-001 Task 2
        - [x] Subtask 1.2: Create `MissionControlServer.kt` extending NanoHTTPD
            - **Objective**: Subclass NanoHTTPD with route dispatching
            - **Test**: Server starts on specified port and accepts connections
            - **Depends on**: Subtask 1.1

- [x] Task 2: Implement server lifecycle management
    - **Problem**: Server needs to start/stop with app lifecycle
    - **Test**: Server starts on app launch, stops on app destroy
    - **Subtasks**:
        - [x] Subtask 2.1: Create ServerManager with start/stop methods
            - **Objective**: Lifecycle-aware server management using LifecycleObserver
            - **Test**: Server starts and stops correctly with lifecycle events
            - **Depends on**: Task 1
        - [x] Subtask 2.2: Integrate ServerManager in Application class
            - **Objective**: Auto-start server on app creation, stop on termination
            - **Test**: Server is running after app launch, stopped after app kill
            - **Depends on**: Subtask 2.1

- [x] Task 3: Implement API endpoints and routing
    - **Problem**: Need REST API for PicoClaw control
    - **Test**: API endpoints return correct responses
    - **Subtasks**:
        - [x] Subtask 3.1: Implement /api/health endpoint
            - **Objective**: Return JSON with server status and uptime
            - **Test**: `curl http://localhost:8080/api/health` returns valid JSON with status "ok"
            - **Depends on**: Task 1
        - [x] Subtask 3.2: Implement /api/status endpoint
            - **Objective**: Return PicoClaw process status (placeholder for Ticket-004)
            - **Test**: Endpoint returns running/stopped status in JSON
            - **Depends on**: Subtask 3.1
        - [x] Subtask 3.3: Implement /api/start endpoint
            - **Objective**: Accept POST to trigger PicoClaw start
            - **Test**: POST returns 200 and status changes to running
            - **Depends on**: Subtask 3.2
        - [x] Subtask 3.4: Implement /api/stop endpoint
            - **Objective**: Accept POST to trigger PicoClaw stop
            - **Test**: POST returns 200 and status changes to stopped
            - **Depends on**: Subtask 3.3

- [x] Task 4: Create Mission Control web interface
    - **Problem**: Need HTML/CSS/JS dashboard for users
    - **Test**: Dashboard renders correctly and shows live status
    - **Subtasks**:
        - [x] Subtask 4.1: Create index.html with dashboard layout
            - **Objective**: Status cards for PicoClaw state, uptime, controls
            - **Test**: Page loads from server `GET /` without errors
            - **Depends on**: Task 1
        - [x] Subtask 4.2: Create styles.css for mobile-responsive design
            - **Objective**: Clean, dark-theme dashboard optimized for mobile screens
            - **Test**: CSS loads and applies correctly (check computed styles)
            - **Depends on**: Subtask 4.1
        - [x] Subtask 4.3: Create app.js with status polling and control buttons
            - **Objective**: Fetch /api/status every 3s, wire Start/Stop buttons
            - **Test**: Status updates without page reload, buttons trigger API calls
            - **Depends on**: Subtask 4.2, Task 3

- [x] Task 5: Serve static assets from server
    - **Problem**: Server needs to serve HTML/CSS/JS from Android assets
    - **Test**: Static files are served with correct MIME types
    - **Subtasks**:
        - [x] Subtask 5.1: Add asset serving to MissionControlServer
            - **Objective**: Read from `assets/mission-control/` and serve with MIME detection
            - **Test**: `GET /` returns index.html, `GET /styles.css` returns CSS
            - **Depends on**: Task 4
        - [x] Subtask 5.2: Configure port from ConfigRepository (Ticket-002)
            - **Objective**: Read port from user configuration
            - **Test**: Changing port in config and restarting app uses new port
            - **Depends on**: Subtask 5.1, ticket-002

- [x] Task 6: Write tests and verify
    - **Problem**: Ensure server works correctly end-to-end
    - **Test**: All unit and instrumented tests pass
    - **Subtasks**:
        - [x] Subtask 6.1: Write server start/stop unit tests
            - **Objective**: Test lifecycle management
            - **Test**: `./gradlew testDebugUnitTest --tests "com.example.clawdroid.server.*"` passes
            - **Depends on**: Task 2
        - [x] Subtask 6.2: Write API endpoint unit tests
            - **Objective**: Test health, status, start, stop responses
            - **Test**: All endpoint tests pass with expected status codes
            - **Depends on**: Task 3
        - [x] Subtask 6.3: Write instrumented test for server integration
            - **Objective**: HTTP client fetches from embedded server on emulator
            - **Test**: `./gradlew connectedAndroidTest --tests "com.example.clawdroid.server.MissionControlServerInstrumentedTest"` passes
            - **Depends on**: Task 5
        - [x] Subtask 6.4: Run full quality check
            - **Objective**: Zero lint errors, all tests pass
            - **Test**: `./gradlew lint && ./gradlew test && ./gradlew assembleDebug` exit 0
            - **Depends on**: Subtasks 6.1-6.3