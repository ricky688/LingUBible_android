# Compact timetable course popup — October 14, 2026

## Changes

Removed the highlighted Selected meeting card and duplicate title conflict badge. The popup uses the existing course-title/divider and compact detail-row styling throughout. Credits, CRN, instructor, meetings and weekly term dates appear once. The clicked meeting is marked with a check and primary color only when a section has several meetings; calendar/ICS callbacks still receive that exact clicked meeting. Detailed conflict warnings remain.

Calendar insertion and ICS export moved into the bottom footer, beneath a compact Remove/Close row. The body scrolls independently. Instructor values wrap within their column. Removal, close, native calendar/ICS behavior and selected courses are preserved. No backend, stored preference, version/signing change. The user's committed v1.0.5 work, including removal of the logo Easter egg, remains intact.

## Verification

- 400 unit tests pass, zero failures/errors/skips; no new tests for this layout-only change. Optimized validation APK built. Initial combined build was terminated with exit143 after those completed; logs contain no source compilation failure. Remaining signed release APK/AAB tasks succeeded on retry in 1m21s. Existing metadata warnings remain nonfatal. Local release guard PASS, matching DEX and embedded mapping (650 renamed app types).
- Updated the previously selected Waydroid with matching-certificate adb install -r, no uninstall/data clear. Installed APK hash matches output. Physical Samsung phone was not touched. Version remains1.0.5/code6; no publishing.
- ARTEMIS observed the original duplicate popup before edits, then observed the updated popup. Grounded ADB actions verified CLC9022/CRN381, credits3, Digital Economy&Social Media, CHUNG Yu Ho and WED14:30–17:29/MBG11/LEC, each listed once. Calendar and ICS are the bottom actions.
- Close dismisses without removing the selected course; tapping the same block reopens the popup. Left that popup open for review.
- Temporarily resized Waydroid from physical480×1000 to1000×480. Footer remains visible while the small detail viewport scrolls through to the term dates. Restored original size in finally; density/font settings unchanged. Screenshots: before.png, after.png, landscape.png, landscape-scrolled.png.
- No app-PID-scoped AndroidRuntime errors in recent logs. Large fonts, multi-meeting/conflict fixtures, native calendar/ICS handoff and physical Samsung/Pixel checks were not repeated.

Artifacts: build/timetable-popup-2026-10-14/. Unit XML, initial/retry logs and R8 mapping/configuration archived there.
