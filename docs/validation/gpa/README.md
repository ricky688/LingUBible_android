# GPA & Honours update validation

## Reference and changes

Based on the parent web app’s `src/pages/GpaHons.tsx`, `src/utils/honours.ts`, and `src/data/firstClassHonours.ts`.

- Added calculator/statistics views, cumulative and term GPA chart, reference lines, target controls, and bilingual summaries.
- Added bundled 2024/2025 honours statistics for 30 programmes with search, faculty filtering, sorting, grouping, and year-over-year comparisons.
- Improved course editor layout, academic-year selection, undo/redo behavior, and reuse of deleted year slots.
- Kept the existing document storage and calculation interfaces. The Discover glass change remains in place.

## Checks

- Debug build and unit tests pass: 335 tests, zero failures/errors/skips.
- Command: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleDebug :app:testDebugUnitTest --offline --console=plain`
- Reference sync script successfully regenerates all 30 programme records.
- Waydroid Android 13: inspected calculator empty state and 2025 statistics through ARTEMIS screenshots in an isolated debug preview package. Confirmed 900 graduates, 119 first-class awards, and 13.2% share.
- Screenshots: [calculator](calculator.jpg), [statistics](statistics.jpg).
- Populated chart rendering and all filter/target interaction combinations have not been device-verified. Fixture injection was unavailable because Waydroid rejected run-as; no release data was changed.
- Screenshots precede a final small adjustment allowing the calculator/statistics selectors to wrap; the final source was rebuilt and retested.

## Deployment

The user-supplied release stays installed. The updated source cannot replace it without a matching release signing key. No signing configuration or stored-preference migration was introduced. Physical Samsung/Pixel validation remains pending.
