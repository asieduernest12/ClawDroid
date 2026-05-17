# AGENTS.md - Ticket Management System Framework

> **Project-Specific Note**: Sections marked with ⚙️ contain project-specific commands/scripts that need updating when copying to a new project.

---

## 🚨 WORKSPACE ISOLATION POLICY (Claw-Agentique)

**STRICT PROJECT CONFINEMENT**

- ✅ All agents **MUST** operate exclusively within the project root: `/home/linuxdev/Desktop/workshop/studio/hustle/CooperGarage/HatMountainCMS/`
- ✅ The `--slug` and all path references **MUST** resolve to this absolute project path
- ❌ Operating outside this project directory is a **CONFIGURATION VIOLATION**
- ❌ Using `~/.openclaw/workspace` or `/tmp` for ticket artifacts is a **VIOLATION**

**Implementation:**

- `spawn_agents.py` and `supervisor.py` **must** derive `PROJECT_ROOT` from the ticket script location and verify it matches the expected HatMountain path
- Any spawned subagent task prompts **must** include: "Work in project root: /home/linuxdev/Desktop/workshop/studio/hustle/CooperGarage/HatMountainCMS/"
- Metrics and logs **must** be written under `.tickets/claw-agentique/` inside the project

---

## ⚠️ CRITICAL WORKFLOW COMPLIANCE WARNING ⚠️

**STRICT TICKET ORDERING MUST BE FOLLOWED RELIGIOUSLY!**

- ✅ Work MUST proceed in ascending numerical order: ticket-001 → ticket-002 → ticket-003 → ... → ticket-030
- ✅ Within each ticket, tasks MUST be completed in document order: Task 1 → Task 2 → Task 3
- ❌ DEVIATIONS ARE A SERIOUS OFFENSE WITH SEVERE CONSEQUENCES
- ❌ SKIPPING, REORDERING, OR WORKING OUT OF SEQUENCE IS STRICTLY FORBIDDEN

---

# Part 1: Generic Ticket Management Framework

## 1. Ticket Structure & Naming Convention

### Ignoring Tickets

Tickets may be marked with an ignore header to exclude them from workflow processing. This is useful for tickets that are deprecated, on hold, or not yet ready for implementation.

**Ignore Header Format:**

Add `<!-- ignore -->` or `<!-- IGNORE -->` at the very top of the `prd.md` file (first line).

**Example:**

```markdown
<!-- ignore -->

# Ticket: Some Deprecated Feature

- [ ] Task 1: ...
```

**Behavior:**

- Tickets with `<!-- ignore -->` in the first line are SKIPPED by all ticket querying scripts
- The verification script will report ignored tickets separately from pending/completed
- Agents MUST NOT work on ignored tickets
- To re-activate a ticket, remove the ignore header from the first line

---

## 2. PRD.md Content Structure

Each `prd.md` file must contain the following sections:

### 2.1 Problem Statement

- Clear description of the issue or feature request
- Business impact and urgency justification
- Current limitations and pain points

### 2.2 Proposed Solution

- High-level technical approach
- Architecture overview and component interactions
- Key implementation decisions

### 2.3 Acceptance Criteria

- Specific, measurable requirements for completion
- Success metrics and validation methods
- Quality standards and performance benchmarks

### 2.4 Technical Considerations

- Implementation constraints and limitations
- Performance requirements and scalability needs
- Security considerations and compliance requirements
- Integration points with existing systems

### 2.5 Dependencies

- Related tickets (reference by ticket number)
- External requirements and prerequisites
- Blocking issues that must be resolved first

#### Cross-Ticket Dependencies

When a ticket depends on work from another ticket, declare it explicitly:

```markdown
### Dependencies

- **Depends on ticket-005**: Authentication must be complete before payment integration
- **Depends on ticket-008**: Kitchen profiles needed for menu items
```

**Rules for Cross-Ticket Dependencies:**

- All dependent tickets MUST be completed (all tasks marked `[x]`) before starting this ticket
- Use the ticket number in the **Depends on** field (e.g., `Depends on: ticket-005`)
- If a task within this ticket can proceed before the full dependent ticket is done, specify the specific task/subtask
- Cross-ticket dependencies are enforced at the ticket ordering level - you cannot work on a ticket if its dependencies are incomplete

---

## 3. Task & Subtask Specification

### 3.1 Required Files

- **prd.md**: Primary ticket document containing all specifications
- **Optional**: Additional supporting files (diagrams, research, etc.)

### 3.2 Main Task Structure

```markdown
- [ ] Task 1: <Main objective>
  - **Problem**: <Specific issue to solve>
  - **Test**: <Verification method>
  - **Depends on**: <None, or list of prerequisite tasks/subtasks>
  - **Subtasks**:
    - [ ] Subtask 1.1: <Detailed implementation step>
      - **Objective**: <Specific goal>
      - **Test**: <How to verify completion>
      - **Depends on**: <None, or list of prerequisite subtasks>
      - **Subtasks** (optional - for complex subtasks):
        - [ ] Subtask 1.1.1: <Nested step>
          - **Objective**: <Specific goal>
          - **Test**: <How to verify>
    - [ ] Subtask 1.2: <Next implementation step>
      - **Objective**: <Specific goal>
      - **Test**: <Verification method>
      - **Depends on**: <None, or Subtask 1.1>

- [ ] Task 2: <Next main objective>
  - **Problem**: <Issue description>
  - **Test**: <Verification approach>
  - **Depends on**: <None, or Task 1>
  - **Subtasks**:
    - [ ] Subtask 2.1: <Implementation detail>
      - **Objective**: <Specific goal>
      - **Test**: <Verification method>
      - **Depends on**: <None, or Task 1 subtasks if cross-task dependency>
```

**Notes:**

- Use nested subtasks (e.g., 1.1.1, 1.1.2) when a subtask needs further breakdown
- The **Depends on** field enables parallel execution - subtasks with no dependencies can run in parallel

### 3.3 Task Status Workflow

#### Status Markers

- `[ ]`: Task is pending (not yet started)
- `[-]`: Task is in progress (actively being worked on)
- `[x]`: Task is completed (finished and verified)
- `[s]`: Task or acceptance criteria is **skipped** — intentionally not applicable or deferred with justification

#### Skipping Tasks & Acceptance Criteria

Sometimes a task or acceptance criterion is not relevant or should be deferred. Use `[s]` to mark it as skipped:

1. **When to skip:**
   - The task is made obsolete by architectural decisions
   - The acceptance criterion is not applicable to the current implementation
   - Work is intentionally deferred to a future ticket (must document why)

2. **How to skip:**
   - Replace the pending `[ ]` with `[s]`
   - Add an inline comment explaining the reason:
     ```markdown
     - [s] Subtask 3.4: Migrate to Spatie packages  <!-- Defer: current implementation adequate -->
     ```
   - For acceptance criteria, same format:
     ```markdown
     - [s] All tests pass (including invoice export tests from Ticket-018)  <!-- Waived: code changes not needed -->
     ```

3. **Documentation requirement:**
   - Skipped items MUST include a brief justification (inline comment or parenthetical)
   - The PRD should include a "Deferred" section listing all skipped items with reasons
   - Skipped tasks are considered resolved (won't appear as pending in scripts)

4. **Agent responsibility:**
   - When marking a task `[s]`, also update the `progress_report.sh` output by committing the PRD change
   - Do not skip tasks arbitrarily; always have a documented rationale
   - Skipped tasks may be revisited in a future ticket if circumstances change

#### Workflow Rules

1. AI agents must use the provided find script to identify `[ ]` pending tasks
2. Update status to `[-]` when work begins
3. Complete all subtask objectives and testing
4. Mark `[x]` only after successful verification
5. Never skip from pending to completed without verification
6. Use `[s]` for justified skips with clear documentation

---

## 4. Task & Subtask Refinement

Agents are ENCOURAGED to add new tasks or subtasks when gaps or missing work is identified. Before working on any task, agents MUST thoroughly review it to determine if it needs further breakdown.

### 4.1 Adding New Tasks/Subtasks

- If during task review, additional work is discovered that wasn't originally captured, agents MAY add new tasks or subtasks
- New tasks should be added at the appropriate location in the task list (maintain logical ordering)
- New subtasks should be added within their parent task's subtask list
- When adding tasks/subtasks, clearly document the reason for the addition
- Mark newly added work as `[ ]` (pending) before starting

### 4.2 Task Breakdown Review

**Before starting ANY task, agents MUST:**

1. Read the full task description and acceptance criteria
2. Identify all subtasks required to complete the task
3. Check if existing subtasks adequately cover all requirements
4. If gaps exist, add missing subtasks with clear objectives and test criteria
5. If a subtask is too large/complex, break it down into smaller actionable pieces

**While Working on Tasks/Subtasks:**

During implementation, continuously analyze the current task/subtask to determine if it needs further breakdown:

- If the subtask reveals additional required steps not captured, ADD new subtasks
- If the subtask is becoming too complex or taking longer than expected, ADD more granular subtasks
- If testing reveals edge cases that need separate handling, ADD subtasks for them
- If the existing subtask lacks specificity in implementation details, ADD clarifying subtasks

**Important Constraints:**

- You MAY NOT modify existing task or subtask descriptions
- You CAN add new subtasks to a task if it's lacking specific aspects
- You CAN add nested subtasks to existing subtasks if deeper breakdown is needed
- Document the reason when adding new subtasks during work

### 4.3 Dependency Declaration

Each task and subtask SHOULD declare its dependencies to enable parallel execution.

**Dependency Declaration:**

- Use a **Depends on** field to list prerequisite tasks/subtasks
- Only declare direct dependencies (what this work directly relies on)
- Tasks with no dependencies can be started immediately

**Example:**

```markdown
- [ ] Task 1: Set up authentication
  - **Problem**: Need secure user authentication
  - **Test**: Users can sign up and log in
  - **Subtasks**:
    - [ ] Subtask 1.1: Configure NextAuth.js providers
      - **Objective**: Set up OAuth providers
      - **Test**: Providers are configured correctly
      - **Depends on**: None
    - [ ] Subtask 1.2: Create login page
      - **Objective**: Build login UI
      - **Test**: Page renders and functions
      - **Depends on**: Subtask 1.1 (providers must be configured first)
    - [ ] Subtask 1.3: Add session management
      - **Objective**: Handle user sessions
      - **Test**: Sessions persist correctly
      - **Depends on**: Subtask 1.1, Subtask 1.2
```

### 4.4 Breaking Down for Parallelization

When a task has dependencies that block parallel work, review whether it can be broken down:

- Identify which parts of the dependent task can proceed independently
- Split into subtasks where some have no dependencies
- Example: "Add payment processing" depends on "Create checkout flow" - break into:
  - Subtask A: Build checkout UI (no deps) - can work in parallel
  - Subtask B: Add payment API (depends on A)

### 4.5 Documentation

- When adding tasks/subtasks, include a brief note explaining why the addition was made
- Use clear, actionable language for new task/subtask descriptions
- Ensure new work has proper **Objective** and **Test** fields

---

## 5. Graph Theory for Parallelization

The task dependency structure forms a **Directed Acyclic Graph (DAG)**. Leveraging graph theory principles improves parallel execution:

### 5.1 Concurrency Limits

**For direct agent work (no subagents):**

- Work on up to **3 parallel subtasks** at a time
- If more than 3 independent subtasks exist, queue the extras
- Too many parallel tasks causes context switching overhead

**For subagent work:**

- Maximum **6 concurrent subagents** (unless fewer than 6 independent tasks remain)
- Main agent must poll/check every 2 minutes (see **Subagent Execution Framework** below)
- Each subagent maintains its own agent-history file (see **Subagent Execution Framework**)

### 5.2 Level-Based Execution

Group subtasks by their **level** (distance from root nodes with no dependencies):

- **Level 0**: Subtasks with `Depends on: None` - ALL can run in parallel
- **Level 1**: Subtasks depending only on Level 0 - run after Level 0 completes
- **Level N**: Subtasks depending on Level N-1 - run after Level N-1 completes

**Example:**

```
Subtask A: Depends on: None          -> Level 0
Subtask B: Depends on: None          -> Level 0  (parallel with A)
Subtask C: Depends on: A              -> Level 1  (after A)
Subtask D: Depends on: A, B          -> Level 1  (after A,B)
Subtask E: Depends on: C, D          -> Level 2  (after C,D)
```

All Level 0 tasks can execute simultaneously. This maximizes parallelism.

### 5.3 Critical Path

The **critical path** is the longest dependency chain through the graph. It determines minimum total completion time:

- Tasks on the critical path MUST execute sequentially
- Tasks NOT on the critical path CAN be parallelized
- Identifying the critical path helps prioritize tasks that block progress

### 5.4 Algorithm for Parallelization

**Step 1: Build the DAG**

- Nodes = subtasks
- Edges = "depends on" relationships

**Step 2: Compute levels**

- Level(subtask) = max(Level of all dependencies) + 1
- Level(subtask with no deps) = 0

**Step 3: Execute by level**

- All Level 0 subtasks run in parallel
- When all Level 0 complete, run all Level 1 in parallel
- Continue until all levels complete

**Step 4: Identify critical path**

- Longest chain = critical path
- Focus resources here for maximum efficiency

### 5.5 Breakdown Guidelines

- Each subtask should be completable in a single focused effort
- Subtasks should have clear, testable completion criteria
- If a subtask takes more than 2-3 hours to complete, it likely needs breaking down
- Complex implementation steps should be separate subtasks
- Testing and verification should be separate subtasks from implementation

---

## 6. AI Agent Workflow

### 6.1 Task Processing

1. **Discovery**: Use the find script to identify pending tasks with `[ ]` marker
2. **Analysis**: Review problem statement and requirements
3. **Implementation**: Complete subtask objectives
4. **Testing**: Execute defined verification tests
5. **Validation**: Confirm all acceptance criteria met for this subtask
6. **Subtask Status**: Update this subtask to `[x]`
7. **Task Closeout** (MANDATORY):
   - After all subtasks within a Task are marked `[x]`, verify the parent Task's acceptance criteria are satisfied
   - Mark the parent Task `[x]`
   - Mark any acceptance criteria checkboxes in the PRD that are now fulfilled as `[x]`
   - This ensures progress reports accurately reflect functional completion and prevents "hanging" checklist items
8. **Commit**: Create atomic commit with message referencing ticket and task number IMMEDIATELY upon successful verification and closeout
9. **Report**: Log completion and move to next pending task

### 6.2 Completion and Exit Conditions

When no pending `[ ]` tasks exist across all tickets, agents MUST terminate gracefully. Do not enter loops or continuously search—log a completion message (e.g., 'All tasks completed; no further work required') and exit. This prevents resource waste and ensures agents do not hang indefinitely.

### 6.3 CRITICAL WORKFLOW RULES

**TICKET ORDERING:**

- Work MUST be performed in strict ascending numerical order by ticket number (ticket-001, ticket-002, ticket-003, etc.)
- Within each ticket, tasks MUST be completed in the exact order they appear (Task 1, then Task 2, then Task 3)
- DEVIATIONS FROM THIS ORDER IS A SERIOUS OFFENSE AND WILL RESULT IN IMMEDIATE TERMINATION

**SUBTASK EXECUTION:**

- Subtasks with **no dependencies** (`Depends on: None`) CAN run in parallel with each other
- Subtasks that have dependencies MUST wait for those dependencies to complete first
- Within subtasks that have dependencies, follow the exact numerical order (1.1, then 1.2, then 1.3)
- The Parallel Execution Policy takes precedence over sequential subtask ordering when no dependencies exist

**DEPENDENCY ENFORCEMENT:**

- All dependencies listed in tickets MUST be completed before starting dependent work
- Agents MUST verify dependency completion by checking for `[x]` status markers
- Starting work on tickets with unmet dependencies IS STRICTLY FORBIDDEN

**STATUS TRANSITIONS:**

- Status updates MUST follow the exact sequence: `[ ]` → `[-]` → `[x]`
- Skipping from pending `[ ]` directly to completed `[x]` IS ABSOLUTELY PROHIBITED
- Each status transition requires explicit verification and documentation

**COMPLETION CLOSEOUT (NEW - CRITICAL):**

After completing all subtasks for a Task, agents MUST perform a full closeout:

1. **Verify acceptance criteria** for the entire ticket are satisfied by the implemented work
2. **Mark parent Task checkbox** as `[x]` (the `- [ ] Task N:` line)
3. **Mark acceptance criteria checkboxes** as `[x]` if they are now fulfilled
4. **Only then** commit and report completion

*Rationale: Subtask completion alone does not equal Task completion. The parent Task and its acceptance criteria represent higher-level validation that must be explicitly confirmed and marked to prevent "hanging" items that appear pending in progress reports.*

---

## 7. Git Commit Process

### 7.1 Commit Timing

- ✅ **CRITICAL REQUIREMENT**: Commit IMMEDIATELY after tests pass - this is CORE to progress tracking
- ❌ NEVER delay commits or batch multiple tasks together
- ❌ NEVER continue to next task without committing current task
- ✅ Each task MUST have its own atomic commit upon test verification
- ✅ Commit is the OFFICIAL record of task completion

### 7.2 Commit Scope

- Atomic commits for complete tasks
- Reference ticket number in commit message
- Include all related files in single commit
- Avoid mixing unrelated changes

### 7.3 Conventional Commits Format

```
<type>(<scope>): <description>
```

**Common Types**:

- `feat`: New feature implementation
- `fix`: Bug fix or issue resolution
- `docs`: Documentation updates
- `test`: Test additions/updates
- `refactor`: Code restructuring
- `chore`: Maintenance tasks

**Examples**:

- `feat(ticket-001): implement user authentication flow`
- `fix(ticket-002): resolve memory leak in data processor`
- `test(ticket-003): add validation tests for API endpoints`

### 7.4 Commit Message Requirements

- Include ticket number in subject line
- Keep subject under 50 characters
- Use imperative mood ("add feature" not "added feature")
- Reference related issues when applicable
- Include detailed body for complex changes

---

## 8. Verification & Testing

### 8.1 Testing Requirements

- Each subtask must have defined test criteria
- Implement TDD (Test-Driven Development) approach
- Include unit tests for core functionality
- Add edge case handling tests
- Test integration scenarios
- Ensure tests are idempotent where possible

### 8.2 Browser-Based Testing (E2E) Requirements

**CRITICAL: All UI-related tasks MUST include browser-based E2E tests using Playwright.**

#### Mandatory Testing Workflow

1. **Write E2E tests BEFORE implementation** (TDD approach)
2. **Tests must verify actual browser behavior**, not just API responses
3. **Use the browser skill** or Playwright container on `hatmountaincms_default` network
4. **Test against real app** at `http://app:8000` (not mocked)

#### E2E Test Coverage Requirements

For any UI-related task, you MUST create tests that verify:

| Test Type | Requirement |
|-----------|-------------|
| Page Load | Page renders without console errors |
| Content Display | Expected elements are visible |
| Interactive Elements | Buttons, links, forms work correctly |
| Data Loading | Widgets/APIs load data within timeout |
| Error Handling | Graceful error states display properly |
| Styling | CSS loads and applies correctly |
| Livewire Components | Livewire initializes and functions |

#### Running E2E Tests

```bash
# Run all tests
docker run --rm -v $(pwd):/workspace -w /workspace/.tickets/ticket-025-e2e-coverage \
  --network hatmountaincms_default -e BASE_URL=http://app:8000 \
  mcr.microsoft.com/playwright:v1.58.0 npx playwright test --reporter=list

# Run specific test file
docker run --rm -v $(pwd):/workspace -w /workspace/.tickets/ticket-025-e2e-coverage \
  --network hatmountaincms_default -e BASE_URL=http://app:8000 \
  mcr.microsoft.com/playwright:v1.58.0 npx playwright test e2e/dashboard.spec.ts
```

#### Test File Naming Convention

- Place E2E tests in `.tickets/ticket-XXX-name/e2e/` directory
- Name files: `feature-name.spec.ts`
- Use descriptive test names that explain what is being verified

### 8.3 Verification Process

1. Execute all defined tests for subtask (unit AND E2E)
2. Validate against acceptance criteria
3. Confirm no regressions introduced
4. Verify code quality standards
5. Document test results
6. **Run E2E tests to verify UI functionality**
7. Update status to `[x]` only after ALL tests pass

### 8.4 Ticket Testing Requirements

Every ticket MUST include testing appropriate to its tasks. The required test types depend on what the ticket implements:

| Task Type | Required Test Type | Example |
|-----------|-------------------|---------|
| Data models, sealed classes | Unit tests | Validate default values, serialization, equality |
| Repositories, data persistence | Unit tests | Save, load, reset round-trips |
| ViewModels, state management | Unit tests | State transitions, action handlers |
| UI layouts, Activities, Fragments | Instrumented tests (Espresso) | Layout inflates, views display, buttons click |
| Navigation between screens | Instrumented tests | Intent fires, target activity opens |
| API endpoints, HTTP routing | Unit tests + Instrumented tests | Response bodies, status codes |
| Static asset serving, MIME types | Unit tests | Correct content type, 404 handling |
| Process lifecycle, session management | Unit tests | Start/stop transitions, error states |
| Download/extraction operations | Unit tests (mock network) | Progress callbacks, error handling, retry |
| Architecture detection, platform queries | Unit tests | Correct value returned for each platform |
| Configuration persistence | Instrumented tests | Values survive process death |
| Full user journeys | Instrumented tests (E2E) | Multi-screen flows, button-to-result |
| Manual verification flows | QA checklist | Step-by-step pass/fail criteria in `qa-checklist.md` |

**Rules:**

1. **No test-free tickets.** Every ticket must have at least one automated test covering its core functionality.
2. **Match test type to task type.** A ticket that adds a ViewModel must include ViewModel unit tests. A ticket that adds a screen must include instrumented tests.
3. **Skipped/deferred tests MUST have justification.** If a test is marked `[s]`, the inline comment must explain why (e.g., `<!-- requires emulator with specific HW feature -->`). A skipped test without justification is a violation.
4. **Integration-critical tests cannot be deferred indefinitely.** If a test requires an emulator or external service, the ticket must either:
   - Provide a manual QA checklist step as a fallback, OR
   - Document a timeline/deferred ticket for later implementation
5. **Unit tests are mandatory** for all business logic (repositories, ViewModels, managers, sessions). Only pure UI configuration (layouts, themes, colors) is exempt.
6. **Instrumented tests are mandatory** for any UI that the user interacts with (activities, fragments, dialogs, navigation).
7. **Full quality check must pass** before a ticket is marked complete: `lint + test + assembleDebug` must all exit 0.
8. **Tickets implementing new features must add new test files** — modifying existing tests is insufficient unless the task scope is a refactor of existing code.
9. **Build verification alone is NOT testing.** A task that says "verify it compiles" does not satisfy the testing requirement — functional assertions are needed.

---

## 9. Code Quality Standards

### 9.1 Implementation Guidelines

- Follow existing code conventions and patterns
- Maintain consistent code style
- Use appropriate libraries already in codebase
- Follow security best practices
- Include comprehensive documentation
- Add meaningful comments where needed

### 9.2 Review Process

- Self-review code before marking complete
- Verify against acceptance criteria
- Check for potential edge cases
- Ensure proper error handling
- Validate performance requirements

### 9.3 Debugging Techniques

When troubleshooting issues:

- **Inspect container logs**: `docker compose logs app` or `docker compose exec app tail -f storage/logs/laravel.log`
- **Check Laravel logs**: View `storage/logs/laravel.log` inside the app container
- **Use Artisan Tinker**: `docker compose exec app php artisan tinker` for interactive testing
- **Debug rate limiting**: Verify cache store with `config('cache.default')` and test `RateLimiter::attempts($key)`
- **Test Redis connectivity**: `docker compose exec app php artisan tinker --execute="Cache::store('redis')->put('test','ok',60); echo Cache::store('redis')->get('test');"`
- **Review middleware**: Ensure tenant identification middleware is configured correctly for test environments
- **Clear caches**: `php artisan optimize:clear`, `php artisan route:clear`, `php artisan config:clear`
- **Dump and die**: Use `dd()` or `dump()` to inspect data within controllers/requests

---

## 10. Continuous Improvement

### 10.1 Enhancement Tracking

- Document workflow improvements
- Track dates, authors, and changes
- Include verification and prevention measures
- Reference related tickets and files

### 10.2 Best Practices

- Maintain transparency in workflow
- Enforce proper status transitions
- Document solutions for future reference
- Ensure thorough testing and validation
- Follow git commit conventions strictly

---

# Part 2: Project-Specific Configuration ⚙️

> **Template**: Copy this section and modify paths/commands for your specific project

## ⚠️ VERIFICATION COMMAND

**Run before starting ANY work:**

```bash
bash .tickets/scripts/verify_tickets.sh
```

---

## Pair Programming Workflow

### Default Implementation Method

**All ticket implementation work SHOULD be done using pair programming** with 2 agents following common pair programming practices, respecting all existing rules in this document and @AGENTS.md:

1. **Driver Agent**: Implements the code changes
2. **Navigator Agent**: Reviews and provides feedback

### Pair Programming Process

1. **Driver Implementation**:
   - Receives task details and implements the solution
   - Writes code following project conventions
   - Runs tests to verify implementation

2. **Navigator Review**:
   - Reviews Driver's code changes
   - Checks for security, UX, error handling
   - Provides feedback or requests fixes

3. **Iteration**:
   - If issues found, Navigator requests fix
   - Driver implements fixes
   - Repeat until approved

### Using Task Tool for Pair Programming

```bash
# Start Driver agent
task --description "Driver for ticket-XXX" --prompt "..." --subagent_type general

# Start Navigator agent for review
task --description "Navigator review ticket-XXX" --prompt "..." --subagent_type explore
```

### Best Practices

- Driver should focus on implementation
- Navigator should review for quality and security
- Both should respect existing project rules (workspace isolation, ticket ordering, etc.)
- Commit after navigator approval
- Use Docker containers for all testing

### When Not to Use Pair Programming

- Simple tasks that can be completed directly
- Research-only tasks (no implementation)
- Quick fixes or documentation updates

> **Update these paths for your project**

```bash
# List all completed tasks
bash .tickets/scripts/list_completed_tasks.sh

# List tickets and their number of backlog tasks
bash .tickets/scripts/list_backlog_tasks.sh

# List the next ticket to work on (first ticket with backlog tasks)
bash .tickets/scripts/find_next_ticket.sh
```

**Note**: Find and grep scripts should be run as presented without omitting characters like backslashes. Be aware that some bash scripts contain escape characters and special sequences that may cause issues when copied directly. Always verify the script content and test execution in a safe environment before running. If scripts fail due to escape characters, manually retype the command or use proper escaping/quoting.

---

## Available Scripts

> **Update for your project's script files**

- `verify_tickets.sh`: Verify ticket status across all tickets
- `list_completed_tasks.sh`: List all completed tasks
- `list_backlog_tasks.sh`: List tickets and their number of backlog tasks
- `find_next_ticket.sh`: Find the next ticket to work on

**Usage**: Run scripts using `bash .tickets/scripts/script_name.sh`

---

## Permission Management

> **Update for your project's Docker/container setup**

```bash
# Fix Docker-related permission issues
docker run --rm -v /path/to/worktree:/workspace --user root alpine chown -R 1000:1000 /workspace
```

---

## Commit Verification

> **Update for your project's path structure**

```bash
# Verify last commit matches current task
git log -1 --oneline | grep -q "$(grep -A5 -B5 '\[-]' .tickets/*/prd.md | grep -E 'Task [0-9]+:' | tail -1 | sed 's/.*Task \([0-9]\+\):.*/ticket-\1/')"

# Check for uncommitted changes (should be empty)
git status --porcelain | wc -l

# Verify commit exists for completed task
git log --oneline | grep -c "$(grep -B2 '\[x\]' .tickets/*/prd.md | grep -E 'Task [0-9]+:' | tail -1 | sed 's/.*Task \([0-9]\+\):.*/ticket-\1/')"
```

---

## Example Ticket Structure

> **Update for your project's conventions**

```
.tickets/
└── ticket-001-user-authentication/
    ├── prd.md                         # Main ticket specification
    ├── diagrams/                     # Optional: Architecture diagrams
    │   └── auth-flow.png
    ├── research/                     # Optional: Research documents
    │   └── oauth-comparison.md
    ├── sub-agent-histories/          # Subagent execution logs (created when subagents run)
    │   └── agent-history-ticket-001-task-1.2-subtask-name.md
    └── test-results/                 # Optional: Test output logs
        └── integration-test.log
```

---

## Project-Specific Workflow Configuration

## ClawDroid Project Settings

### Technology Stack

- **Language**: Kotlin
- **Build**: Gradle with Kotlin DSL
- **Min SDK**: 26 (Android 8.0)
- **Testing**: JUnit 4 + Mockito (unit), Espresso (instrumented)
- **Web Server**: NanoHTTPD embedded HTTP server
- **Terminal**: Embedded Termux bootstrap (self-hosted, no external app required)

### Development Commands

- Build debug APK: `./gradlew assembleDebug`
- Build release APK: `./gradlew assembleRelease`
- Run unit tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Run lint: `./gradlew lint`
- Clean build: `./gradlew clean`
- Full quality check: `./gradlew lint && ./gradlew test && ./gradlew assembleDebug`

### Emulator/Environment

- Emulator AVD name: `clawdroid_test`
- Minimum API level: 21
- ABI: x86_64 (for emulator)
- ADB path: `$ANDROID_HOME/platform-tools/adb`

## Subagent Execution Framework

### When to Use Subagents

Main agents MAY spawn subagents to achieve parallel execution when:
- A task has multiple independent subtasks (no dependencies between them)
- The main agent needs to coordinate work on multiple subtasks simultaneously
- The complexity or volume of work exceeds what a single agent context can handle efficiently

### Subagent Requirements

**Naming Convention:** Subagents must follow the naming convention for their agent-history files:
```
sub-agent-histories/agent-history-ticket-001-task-1.2-subtask-name.md
```
- `ticket-001`: Ticket ID (with zero-padding)
- `task-1.2`: Task and subtask identifier
- `subtask-name`: Slug derived from subtask description
- No agent ID in filename (agents identify themselves within file content)

**File Location:** `sub-agent-histories/` directory MUST be placed in the parent ticket folder (the same folder as the `prd.md`).

**Mandatory Creation:** Subagents MUST create their agent-history file at the START of their work. If a subagent fails or gets stuck before creating the file, the main agent must create it on their behalf.

**Work Scope:** Subagents must ONLY work on the specific task/subtask assigned to them. They MUST NOT deviate to other tasks or initiate new work beyond their assignment.

**Policy Compliance:** Subagents MUST respect all AGENTS.md policies, including ticket ordering, task dependencies, and status transitions.

**Reporting:** Subagents must report completion or failure back to the main agent through their agent-history file and direct response.

**Main Agent Responsibilities:**

- **Spawn:** Use the Task tool to create subagents, assigning specific pending tasks
- **Monitor:** Poll all subagents every 2 minutes to ensure no stalls or failures
- **Respawn:** If a subagent fails or gets stuck, immediately create a replacement
- **Coordinate:** Ensure subagents don't conflict on shared resources or files
- **Merge:** Integrate completed subagent work and update ticket status

### Agent-History File Structure

Each agent-history file should document the subagent's work chronologically:

```markdown
# Subagent History

**Agent ID:** kilo_kilo_auto_free (or other unique identifier)  
**Ticket:** ticket-001  
**Task:** Task 1.2 - Implement login validation  
**Started:** 2025-03-17 10:15:00  
**Status:** in_progress | completed | failed  
**Completed:** 2025-03-17 11:45:00 (if applicable)

## Work Log

### 2025-03-17 10:15:00 - Start ( Agent: kilo_kilo_auto_free )
- Assigned task: Implement login validation subtask
- Read prd.md and reviewed task requirements
- Reviewed existing validation code in app/Http/Controllers/AuthController.php

### 2025-03-17 10:30:00 - Implementation ( Agent: kilo_kilo_auto_free )
- Added validateLogin method with email and password validation
- Included rate limiting check using Laravel's ThrottleRequests
- Updated login form with CSRF token

### 2025-03-17 11:00:00 - Testing ( Agent: kilo_kilo_auto_free )
- Ran php artisan test tests/Feature/LoginTest.php
- All tests passed (5/5)
- Manually tested login form in browser

### 2025-03-17 11:45:00 - Completion ( Agent: kilo_kilo_auto_free )
- Verified all acceptance criteria met
- Created git commit: feat(ticket-001): add login validation with rate limiting
- Updated prd.md status to [x]
- Reported completion to main agent

## Errors/Issues Encountered
- None

## Files Modified
- app/Http/Controllers/AuthController.php
- resources/views/auth/login.blade.php
- database/migrations/2025_03_17_110000_add_rate_limit_to_logins_table.php
```

### Agent-History File Content Requirements

Each agent-history file MUST include:

1. **Header Block:** Agent ID, ticket, task, timestamps, status
2. **Work Log:** Chronological entries with timestamps for each significant action
3. **Verification:** Document test execution and results
4. **Commit Reference:** Git commit message or hash upon completion
5. **Files Modified:** List all files touched
6. **Errors/Issues:** Any problems encountered and how they were resolved
7. **Status:** Final status (completed/failed) and reporting to main agent

### Subagent Lifecycle

1. **Creation:** Main agent spawns subagent via Task tool, specifying exact task/subtask
2. **Initialization:** Subagent creates agent-history file with start timestamp
3. **Execution:** Subagent works independently, logging all actions in real-time
4. **Monitoring:** Main agent polls every 2 minutes (reads agent-history to check progress)
5. **Completion:** Subagent verifies work, creates git commit, updates prd.md, writes final status, reports back
6. **Failure:** Subagent marks status as failed, documents reason, reports to main agent
7. **Respawn:** Main agent creates new subagent if failure occurred

### Concurrency Limits

- Maximum **6 concurrent subagents** per main agent (unless fewer than 6 independent tasks remain)
- Subagents should be assigned tasks that do not conflict (different files, different database concerns)
- Database schema changes should be done sequentially to avoid migration conflicts

### Respawn Policy

If a subagent:
- Fails with an error
- Times out or becomes unresponsive
- Produces no log entries for > 5 minutes
- Reports dependency blockage that could be resolved by another approach

The main agent MUST respawn it with a fresh agent, preserving the same task assignment. The new agent should read the existing agent-history file to understand prior attempts before continuing.

This policy supersedes any implicit sequential ordering in PRD documents; always analyze for independence first.

## 11. HatMountain Queue Workflow

### 11.1 Overview
The HatMountain system processes tickets in a strict pipeline. A queue tick (triggered regularly or manually) checks for the next available subtask and spawns sub‑agents to work on them, up to a maximum of 6 concurrent agents.

### 11.2 Finding Next Subtask
- The script `.tickets/scripts/find_next_ticket.sh` scans all PRDs for the first unstarted item marked `[ ]` (unchecked) and prints its ticket and line number.
- Only `[ ]` items are considered "pending". Items marked `[-]` are in‑progress and are ignored by the scanner.

### 11.3 Spawning Agents
- Main agent (this session) calls `sessions_spawn` with:
  - `label`: a unique identifier like `task-4.2-impact-ui`
  - `task`: a clear description of the subtask, including dependencies and acceptance criteria
  - `mode=run` or `session` as appropriate
  - Timeout: typically 1800 seconds (30 minutes)
- After spawning, the corresponding PRD entry is changed from `[ ]` to `[-]` to indicate it is now in‑progress.
- The label is also recorded as an HTML comment next to the line: `<!-- label: <label> -->` for health‑checking.

### 11.4 Health Checks and Auto‑Recovery
- On each queue tick, the main agent parses all PRDs for `[-]` entries and extracts their labels.
- It then compares this set against the list of currently active sub‑agents.
- If a label is missing (i.e., an in‑progress task has no running agent), it is considered a failure. The failure count for that label (stored in `.tickets/agent_retries.json`) is incremented.
- **After 3 failures**, auto‑respawn stops. The main agent will take over that subtask directly in the next tick.
- Successful completion resets the failure count to 0 (or is logged separately; we keep cumulative counts).

### 11.5 Commit Policy
- Every sub‑agent **must commit** its changes before announcing completion.
- The task template includes a final step:
  ```bash
  git add -A
  git commit -m "Brief message describing the subtask"
  ```
- This ensures all work is versioned and traceable.

### 11.6 Acknowledgment
- Upon spawning, each sub‑agent must create an `agent-history-<task>-<id>.md` file with an **Acknowledgment** section that states:
  - Label
  - Task description
  - Dependencies
  - Acceptance criteria
- This establishes traceability from the start, even if the agent later fails.

### 11.7 Retry Tracking
- The file `.tickets/agent_retries.json` maintains per‑label counters:
  ```json
  {
    "label": {
      "failures": <int>,
      "last_failure": "<timestamp or null>",
      "successes": <int>,
      "last_success": "<timestamp or null>"
    }
  }
  ```
- Counts are cumulative and not cleared on success (for process review and refinement).

### 11.8 Dependent Ticket Order
- Tickets must generally be processed in ascending numerical order because later tickets depend on earlier foundations (e.g., multi‑tenancy before contract management).
- The queue scanner respects this by not offering subtasks from higher‑numbered tickets if any `[ ]` remains in a lower‑numbered ticket **unless** those lower tickets are blocked by true dependencies (e.g., missing code). In practice, we manually track dependency readiness and may need to mark subtasks as `[ ]` only when their prerequisites are truly met.

### 11.9 Notes
- Sub‑agents write their final `agent-history` only on successful completion. If they fail early, they may not produce a history file; the health‑check and retry mechanism ensures the task eventually completes.
- The main agent is responsible for overall coordination, dependency validation, and final PRD updates.

