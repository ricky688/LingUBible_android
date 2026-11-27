#!/usr/bin/env bash
set -e

TRACK="${1:-alpha}"
STATUS="${2:-completed}"

echo "=========================================="
echo "🚀 Building Release AAB..."
echo "=========================================="
./gradlew bundleRelease

AAB_PATH="app/build/outputs/bundle/release/app-release.aab"

if [ ! -f "$AAB_PATH" ]; then
    echo "[-] Error: AAB file not found at $AAB_PATH"
    exit 1
fi

echo "=========================================="
echo "📤 Uploading to Google Play (Track: $TRACK)..."
echo "=========================================="
python3 tools/upload_to_play_store.py --aab "$AAB_PATH" --track "$TRACK" --status "$STATUS"
