# ClawDroid E2E Acceptance QA Checklist

> Manual verification checklist for stakeholders. Each scenario maps to a ticket acceptance criterion.
> Run against a real device or emulator (API 26+).

---

## 1. App Launch (ticket-001)

- [ ] **Welcome screen displayed**: Launch ClawDroid — "Welcome to ClawDroid" text is visible
- [ ] **Settings button navigates to config**: Tap "Settings" — "PicoClaw Configuration" screen opens

## 2. Configuration Screen (ticket-002)

- [ ] **Default values shown**: Config screen shows default port (8080), binary path, config directory
- [ ] **Modify and save**: Change server port to 9090, tap Save — "Configuration saved" indicator appears
- [ ] **Invalid port validation**: Enter port 80, tap Save — "Port must be between 1024 and 65535" error shown
- [ ] **Reset defaults**: Change port, tap "Reset Defaults" — port reverts to 8080
- [ ] **Persistence across restart**: Set port to 9090, force-stop app, relaunch — port is still 9090

## 3. Mission Control Server (ticket-003)

- [ ] **Health endpoint**: `GET http://127.0.0.1:8080/api/health` returns `{"status":"ok","uptime":N}`
- [ ] **Status endpoint**: `GET http://127.0.0.1:8080/api/status` returns JSON with status, uptimeSeconds, nanoClawRunning, port
- [ ] **Dashboard HTML**: `GET http://127.0.0.1:8080/` returns HTML containing "Mission Control"

## 4. Terminal / Embedded Termux (tickets 004-006)

- [ ] **Bootstrap completes**: First launch shows "Termux environment ready" after bootstrap
- [ ] **Start PicoClaw**: `POST http://127.0.0.1:8080/api/start` returns `{"success":true}`
- [ ] **Status reflects running**: After start, `GET /api/status` shows `nanoClawRunning: true`
- [ ] **Stop PicoClaw**: `POST http://127.0.0.1:8080/api/stop` returns `{"success":true}`
- [ ] **Status reflects stopped**: After stop, `GET /api/status` shows `nanoClawRunning: false`
- [ ] **Bootstrap cached**: Force-stop app, relaunch — "Termux environment ready" appears immediately (no re-download)

---

## Automation

The same scenarios are automated in `app/src/androidTest/java/com/example/clawdroid/acceptance/`:

| Scenario class | Covers |
|---------------|--------|
| `AppLaunchScenario` | #1 Welcome, #2 Settings navigation |
| `ConfigScenario` | #3-6 Config modification, validation, reset, persistence |
| `ServerScenario` | #7-9 Health, status, dashboard endpoints |
| `TerminalScenario` | #10-14 Bootstrap, start/stop, cached bootstrap |

Run: `make test-e2e`
