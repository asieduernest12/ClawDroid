help:
	cat ./Makefile

# ── Docker Compose ──────────────────────────────────────────

up:
	docker compose up -d

down:
	docker compose down

build-image:
	docker compose build build

rebuild:
	docker compose build --no-cache build
	docker compose up -d

logs:
	docker compose logs -f

shell:
	docker compose exec build /bin/bash

# ── Build ──────────────────────────────────────────────────

build-debug:
	docker compose exec build ./gradlew assembleDebug

build-release:
	docker compose exec build ./gradlew assembleRelease

clean:
	docker compose exec build ./gradlew clean

# ── Tests ──────────────────────────────────────────────────

test-unit:
	docker compose exec build ./gradlew test

test-unit-debug:
	docker compose exec build ./gradlew testDebugUnitTest

test-integration:
	docker compose exec build ./gradlew connectedAndroidTest

test-e2e:
	docker compose exec build sh -c './gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="com.example.clawdroid.acceptance.AcceptanceTestSuite"'

test-all:
	docker compose exec build sh -c './gradlew test && ./gradlew connectedAndroidTest'

lint:
	docker compose exec build ./gradlew lint

quality-check:
	docker compose exec build sh -c './gradlew lint && ./gradlew test && ./gradlew assembleDebug'

# ── Emulator (remote) ─────────────────────────────────────

EMULATOR_IP ?= 192.168.204.107
EMULATOR_PORT ?= 5555

adb-connect:
	docker compose exec build adb connect $(EMULATOR_IP):$(EMULATOR_PORT)

adb-find:
	docker compose exec build bash scripts/find-emulator.sh

adb-wait:
	docker compose exec build sh -c 'adb wait-for-device && \
		while [ "$$(adb shell getprop sys.boot_completed | tr -d '\''\r'\'')" != "1" ]; do \
			sleep 2; \
		done'

adb-shell:
	docker compose exec build adb shell

adb-install: build-debug
	docker compose exec build adb install -r app/build/outputs/apk/debug/app-debug.apk

adb-logcat:
	docker compose exec build adb logcat -v time | grep -E "(clawdroid|AndroidRuntime|System\.err)"

# ── Project ──────────────────────────────────────────────

gradle-wrapper:
	docker compose exec build gradle wrapper --gradle-version=8.9

# ── Cleanup ──────────────────────────────────────────────

prune:
	docker system prune -af --volumes

.PHONY: help up down build-image rebuild logs shell \
	build-debug build-release clean \
	test-unit test-unit-debug test-integration test-e2e test-all lint quality-check \
	adb-connect adb-wait adb-shell adb-install adb-logcat \
	gradle-wrapper prune
