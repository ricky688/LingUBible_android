# Discover scrollbar correction — 2026-10-21

## Change

The shared lazy-list scrollbar now uses the actual first scrolling item, excluding pinned headers. Fractional visible rows account for scrolling inside tall sections and avoid whole-row jumps in thumb size. Non-scrollable lists hide the indicator; scroll boundaries anchor it to the ends. The track excludes measured trailing content padding, keeping it above navigation and reserved actions. The ordinary ScrollState overload is unchanged. No dependency, backend, preference, signing or version changes were made.

## Validation

- ARTEMIS trace `1ff4a9c4-6203-4805-b995-d4e653d011f1` verified the four requested scroll interactions on the original release. The task was stopped after completing those actions because its final response stalled. Execution summary inspected; the daemon-provided raw stderr path was unavailable on this host.
- Original release: a slow 200-pixel upward finger swipe moved the reviews but left the thumb at y=311–585 in both before screenshots.
- Final optimized release: the same short swipe advanced the thumb. See `results.json` for measured pixel bounds; final bottom thumb ends above the system navigation area at y=946.
- Manually inspected the final top, two middle, and bottom screenshots on Android 13 Waydroid, 480×1000. Pinned category tabs remain available. The app was returned to Discover's start afterward.
- All 554 unit tests passed, including six scrollbar geometry tests. Release APK build passed. Installed APK bytes match the build; existing release certificate retained, installation performed as an update without clearing app data.
- Existing Kotlin metadata/R8 warnings remain nonfatal. No toolchain changes were made.

## Scope of checks

Physical Samsung/Pixel, landscape and large-font device checks were not repeated for this change. No AAB or upload was produced.

## Evidence

- `before-1.png`, `before-2.png`: original stuck indicator.
- `after-top.png`, `after-1.png`, `after-2.png`, `after-bottom.png`: final release.
- `baseline.json`, `results.json`, `unit-tests.json`, `scrollbar-unit-tests.xml`, `build.log`.

APK: `app/build/outputs/apk/release/app-release.apk`
SHA-256: `7558cad89cc154bdb256070b9092701806a6b1755c04e7b0e19e249018756290`
