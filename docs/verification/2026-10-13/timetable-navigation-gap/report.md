# Timetable fade at bottom navigation — October 13, 2026

## Root cause and fix

The bottom bar included a 48 dp decorative gradient as a normal layout child. Scaffold counted that height in bottom clearance, so the timetable fade ended above the decorative region, creating a separate dark strip. The earlier timetable-only change did not fix this shared measurement problem.

The shared bottom bar now draws the gradient above its bounds in drawBehind, ending at the navigation surface. The decoration reserves no height. Scaffold therefore measures the actual navigation controls/system inset; the timetable fade meets that boundary. Other screens also receive the corrected measured clearance. No data, preference, backend or signing changes.

## Verification

- 400 JVM unit tests pass, zero failures/errors/skips. All optimized validation/release APK and release AAB builds succeed. Release guard PASS (649 renamed app types; matching APK/AAB DEX and embedded mapping). No new UI test code was authored for this drawing/layout-only correction.
- Installed matching-certificate releaseValidation with adb install -r on Waydroid Android 13, 480×1000 at density 180. Pulled APK hash matches the current validation artifact. No uninstall or data clear.
- ARTEMIS screenshots + grounded ADB actions verify the top timetable viewport, no isolated strip before navigation, fade joined to navigation, scrolling with bars hidden, and final rows reaching above system navigation. Returned to top grid view. Existing selected course remains; no course changes.
- Screenshots: before.jpg shows the elevated dark band; after.jpg shows continuous fade into the bar. scrolled.jpg and bottom.jpg show final grid rows with navigation hidden.
- Autonomous ARTEMIS navigation was cancelled after it stayed on Home; direct ADB navigation used observed screenshot positions. Advertised stdout/stderr files were absent; status.json confirms cancellation, not app failure. Recorder scrcpy emitted unsupported audio/opus encoder errors; screenshot observation continued. LingUBible's app-PID-scoped AndroidRuntime check was empty.
- Physical devices, rotated/split-screen windows, other themes, calendar-editor and full PNG export checks were not repeated in this focused fix.

Artifacts: build/timetable-navigation-gap-2026-10-13/. Version remains 1.0.3/code 4; no upload/publication.
