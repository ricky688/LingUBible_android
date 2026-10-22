# Divider retest — 2026-10-21

## Results

- All 554 existing unit tests passed (zero failures/errors/skips); Gradle testDebugUnitTest succeeded in 49 seconds.
- Installed optimized APK SHA-256 was read from Waydroid and matched the built divider release: ea38138ea971f19b9669064d70d67fe988c60acfb40bc45375fdaf4197c5a61f.
- Fresh direct Android CLI layout and screenshot checks: grouped profile separators in light/dark; calculator term separators and final Add Academic Year action; timetable popup at 200% text, with divider and all four action labels visible. No calendar/export/remove/reset/account actions were invoked.
- No recent AndroidRuntime fatal exception in the sampled log.
- Returned to the app's main feed. Original Dark mode, font scale 1.0, portrait rotation lock 0 and accelerometer rotation 0 restored. No app data clearing, reinstall, source fixes or preference migration.

## Findings outside divider styling

1. At 200% text size, the timetable credits label/value remain in a narrow row and the unavailable-credit value breaks awkwardly across lines. The divider and calendar/export/remove/close actions remain visible. See timetable-200-percent.png.
2. At 1000×480 landscape, timetable headers/controls and bottom navigation leave the grid almost no usable height. See timetable-landscape.png. This limits checking course-popup dividers in that configuration.

These were recorded without expanding the divider change into an adaptive-layout redesign. They were not compared against an older APK, so this report does not establish their introduction date. No divider-related defect was observed in the checked layouts.

## Method and limits

Android CLI layout inspection, semantic text/bounds, observed-coordinate ADB actions and visually inspected screenshots. ARTEMIS diagnose returned ready (5/5 checks); its history also contained provider 429 errors, and prior exploratory traces had stalled, so this retest used direct Android tools rather than launching another autonomous run. No new test scripts were authored. Screenshots of profile settings are cropped to exclude account information.

Physical Samsung/Pixel, Android 15/16, wallpaper-based dynamic colors, every divider screen, screen-reader traversal and screenshots of light-mode timetable/GPA were not checked in this retest. Earlier normal-size screenshots remain in the parent verification directory.
