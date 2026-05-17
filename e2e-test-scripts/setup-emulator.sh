#!/bin/bash
set -euo pipefail

AVD_NAME="clawdroid_test"
API_LEVEL=21
ARCH="x86_64"
IMAGE="system-images;android-${API_LEVEL};default;${ARCH}"

if ! command -v avdmanager &>/dev/null; then
    echo "Error: avdmanager not found. Ensure Android SDK tools are in PATH."
    exit 1
fi

echo "Creating AVD '$AVD_NAME' (API $API_LEVEL, $ARCH)..."
echo no | avdmanager create avd -n "$AVD_NAME" -k "$IMAGE" -d "pixel_6" 2>/dev/null || {
    echo "AVD '$AVD_NAME' already exists or creation failed. Skipping creation."
}

echo "Starting emulator..."
emulator -avd "$AVD_NAME" -no-window -no-audio -no-boot-anim \
    -memory 2048 -cores 2 -gpu swiftshader_indirect &

echo "Waiting for device..."
adb wait-for-device

echo "Disabling animations..."
adb shell settings put global window_animation_scale 0.0
adb shell settings put global transition_animation_scale 0.0
adb shell settings put global animator_duration_scale 0.0

echo "Waiting for boot to complete..."
while [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do
    sleep 2
done

echo "Emulator '$AVD_NAME' is ready."
