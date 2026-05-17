#!/bin/bash
# build-picoclaw.sh — resolve, clone, cross-compile PicoClaw for Android
# Runs inside the build container. Output to $OUT_DIR (default: ./app/src/main/assets/picoclaw)
set -euo pipefail

# Determine output dir: use env override or default to project assets
# The project root is the current working dir (set by compose working_dir)
PROJECT_ROOT="$(pwd)"
if [ -z "${OUT_DIR:-}" ]; then
    OUT_DIR="${PROJECT_ROOT}/app/src/main/assets/picoclaw"
fi
echo "Output directory: $OUT_DIR"
TMP_DIR="/tmp/picoclaw-build"

rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR"

# Use project dir for Go build cache (container /tmp can be small)
export GOTMPDIR="$TMP_DIR/go-tmp"
mkdir -p "$GOTMPDIR"
mkdir -p "$OUT_DIR"

# ---- Resolve release tag ----
resolve-ref.sh > "$TMP_DIR/TARGET_REF"
REF_TYPE=$(cat /tmp/ref-type.txt)
TARGET_REF=$(cat "$TMP_DIR/TARGET_REF")
echo "Selected ref: $TARGET_REF (type=$REF_TYPE)"

# ---- Clone ----
cd "$TMP_DIR"
if [ "$REF_TYPE" = "tag" ]; then
    echo "Cloning picoclaw tag $TARGET_REF..."
    git clone --depth 1 --branch "$TARGET_REF" https://github.com/sipeed/picoclaw.git picoclaw-src
else
    set -- $TARGET_REF
    HEAD_SHA="${2:-}"
    HEAD_BRANCH="${3:-master}"
    if [ -n "$HEAD_SHA" ]; then
        echo "Cloning picoclaw branch $HEAD_BRANCH at $HEAD_SHA..."
        git clone --depth 1 --branch "$HEAD_BRANCH" https://github.com/sipeed/picoclaw.git picoclaw-src && \
            cd picoclaw-src && git fetch --depth=1 origin "$HEAD_SHA" && git checkout "$HEAD_SHA"
    else
        echo "Cloning picoclaw default branch..."
        git clone --depth 1 https://github.com/sipeed/picoclaw.git picoclaw-src
    fi
fi

# ---- Setup workspace embed ----
cd "$TMP_DIR/picoclaw-src"
if [ -d cmd/picoclaw/internal/onboard ]; then
    cp -r workspace cmd/picoclaw/internal/onboard/workspace 2>/dev/null || true
fi

# ---- Determine build target ----
if [ -f main.go ]; then
    BUILD_TARGET="."
elif [ -f cmd/picoclaw/main.go ]; then
    BUILD_TARGET="./cmd/picoclaw"
else
    BUILD_TARGET=$(find . -name main.go -not -path "*/vendor/*" -exec dirname {} \; | head -1)
fi
echo "Build target: $BUILD_TARGET"

# ---- Cross-compile ----
# Go arch -> CC env var naming
declare -A CC_VAR
CC_VAR[arm64]="aarch64-linux-android21-clang"
CC_VAR[amd64]="x86_64-linux-android21-clang"
CC_VAR[386]="i686-linux-android21-clang"
CC_VAR[arm]="armv7a-linux-androideabi21-clang"

# Go arch -> asset filename
declare -A OUT_NAME
OUT_NAME[arm64]="picoclaw-arm64"
OUT_NAME[amd64]="picoclaw-x86_64"
OUT_NAME[386]="picoclaw-x86"
OUT_NAME[arm]="picoclaw-arm"

for arch in arm64 amd64 386 arm; do
    cc="${CC_VAR[$arch]}"
    out="$OUT_DIR/${OUT_NAME[$arch]}"
    echo "=== Building for android/$arch -> ${OUT_NAME[$arch]} ==="
    cd "$TMP_DIR/picoclaw-src"
    if GOOS=android GOARCH=$arch CGO_ENABLED=1 CC="$cc" \
        go build -trimpath -ldflags="-s -w" -o "$out" "$BUILD_TARGET" 2>&1; then
        echo "  OK: ${OUT_NAME[$arch]} ($(stat -c%s "$out" 2>/dev/null || echo ?) bytes)"
    else
        echo "  FAILED: android/$arch"
    fi
done

echo "=== Build complete ==="
ls -la "$OUT_DIR/"
