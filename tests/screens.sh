#!/usr/bin/env bash
# Walks the app on an Android TV emulator with the remote's keys and takes a screenshot of each
# screen (run by CI inside android-emulator-runner). Fails if the app doesn't install, crashes or
# dies; whether the screens look right is for a person to judge from the screenshots.
# Usage: tests/screens.sh <apk> [out-dir]
set -uo pipefail

APK="$1"
OUT="${2:-screens}"
PKG="tv.hackertube.zh"
mkdir -p "$OUT"
SUMMARY="$OUT/summary.md"
: > "$SUMMARY"
FAIL=0
N=0

log()  { echo "$*"; echo "$*" >> "$SUMMARY"; }
key()  { for k in "$@"; do adb shell input keyevent "$k"; sleep 1; done; }
shot() { N=$((N + 1)); adb exec-out screencap -p > "$OUT/$(printf '%02d' $N)-$1.png" 2>/dev/null || true; log "- screenshot $N: $1"; }
alive() { [ -n "$(adb shell pidof "$PKG" 2>/dev/null | tr -d '\r')" ]; }
check() {
  if adb logcat -d -b crash 2>/dev/null | grep -q "Process: $PKG"; then
    log "- ❌ crashed after: $1"; FAIL=1
  elif ! alive; then
    log "- ❌ not running after: $1"; FAIL=1
  fi
}

log "### Screens"
adb wait-for-device
adb shell settings put global package_verifier_enable 0 >/dev/null 2>&1 || true
if ! adb install -r "$APK" > "$OUT/install.txt" 2>&1; then
  log "- ❌ install failed: $(tail -1 "$OUT/install.txt")"; exit 1
fi
adb logcat -c
adb shell monkey -p "$PKG" -c android.intent.category.LEANBACK_LAUNCHER 1 >/dev/null 2>&1
sleep 25
shot home; check "launch"

# Never press OK on a video: it would leave for SmartTube or YouTube.
# Home: focus starts on the first video; Up goes to the type chips.
key DPAD_UP DPAD_RIGHT
shot home-type-chip
key DPAD_CENTER; sleep 8
shot home-talks; check "type chip"

# Sidebar: entering it lands on Home; below Playlists come the topic areas.
key DPAD_LEFT DPAD_LEFT DPAD_LEFT; sleep 2
shot sidebar; check "sidebar"
key DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN
shot sidebar-areas
key DPAD_CENTER; sleep 10
shot area; check "area"
key DPAD_UP DPAD_UP DPAD_UP DPAD_UP; sleep 1
shot area-chips
key DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN; sleep 2
shot area-videos

# Details: hold OK on a video.
adb shell input keyevent --longpress DPAD_CENTER; sleep 10
shot details; check "details"
key DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN; sleep 2
shot details-more

# Back to Home (details -> area -> Home), then the sidebar's Playlists (six below Home).
key BACK BACK; sleep 3
key DPAD_LEFT DPAD_LEFT DPAD_LEFT; sleep 2
key DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN
shot sidebar-playlists
key DPAD_CENTER; sleep 10
shot playlists; check "playlists"
key DPAD_CENTER; sleep 10
shot playlist; check "playlist"
key DPAD_DOWN DPAD_DOWN; sleep 2
shot playlist-videos

# Categories (one above Playlists); playlist -> Playlists -> Home.
key BACK BACK; sleep 3
key DPAD_LEFT DPAD_LEFT DPAD_LEFT; sleep 2
key DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN DPAD_DOWN
key DPAD_CENTER; sleep 10
shot categories; check "categories"

adb logcat -d > "$OUT/logcat.txt" 2>/dev/null || true
adb logcat -d -b crash > "$OUT/crash.txt" 2>/dev/null || true
if [ "$FAIL" = 0 ]; then log "**Result: PASS**"; else log "**Result: FAIL**"; fi
exit $FAIL
