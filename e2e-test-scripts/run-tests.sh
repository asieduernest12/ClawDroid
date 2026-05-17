#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SCREENSHOT_DIR="$SCRIPT_DIR/screenshots"
TEST_REPORT_DIR="$PROJECT_DIR/app/build/reports/androidTests/connected"

mkdir -p "$SCREENSHOT_DIR"
mkdir -p "$TEST_REPORT_DIR"

echo "Installing debug APK..."
adb install -r "$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk" 2>/dev/null || true
adb install -r "$PROJECT_DIR/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" 2>/dev/null || true

echo "Running instrumented tests..."
cd "$PROJECT_DIR"
if docker compose exec build ./gradlew connectedAndroidTest --no-daemon; then
    echo "All tests passed."
    exit 0
else
    echo "Some tests failed. Capturing screenshot..."
    adb exec-out screencap -p > "$SCREENSHOT_DIR/failure-$(date +%Y%m%d-%H%M%S).png"
    echo "Screenshot saved to $SCREENSHOT_DIR"
    echo "Test reports available at $TEST_REPORT_DIR"
    exit 1
fi
