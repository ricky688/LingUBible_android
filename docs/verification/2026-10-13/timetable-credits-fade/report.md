# Course timetable credits and fading edge — October 13, 2026

## Changes

- The tapped course popup displays bilingual credits from the existing cached CourseRepository. Code matching ignores surrounding whitespace and case. Unavailable credit metadata is labelled as unavailable; zero credits are preserved.
- The timetable total now sums catalog credits. Uncatalogued entries retain the existing three-credit estimate; there is no preference or backend migration.
- Removed the fixed bottom reservation around the weekly grid. Its bottom clearance now scrolls with the body, keeping the last rows reachable above navigation. Course selection and the empty-state card retain their viewport clearance.
- Reused the app's scroll-responsive frosted fade with stiffness-only spring animation. The bottom fade ends above measured navigation clearance and clamps in short windows. Other callers keep the existing zero-inset behavior. This is the existing gradient/alpha dissolve effect, not a new platform backdrop-blur implementation.
- PNG capture remains on the raw header/body, inside the scroll containers and after the padding modifier. Navigation spacing and viewport fading are excluded from the captured grid.

## Local verification

- 400 unit tests pass, zero failures/errors/skips. Six new tests cover normalized catalog credits, zero credits, catalog failure without losing selected sections, retaining metadata across term changes, normal/overlaid navigation fade geometry, and short/fully covered viewports.
- No UI tests were authored for unverified flows. Existing ARTEMIS exploration of Planner/course selection is recorded in the export report.
- Debug tests, optimized validation APK, signed release APK and signed release AAB built successfully in one run (5m17s). Existing dependency Kotlin metadata/deprecation warnings remain nonfatal.
- Local release guard PASS: APK/AAB DEX match, embedded R8 mapping matches, 648 renamed application types present in DEX. This count is not a Play Console coverage percentage.
- Artifacts, R8 mapping/configuration, unit-test XML and build log are archived under `build/timetable-credits-fade-2026-10-13/`. Version remains 1.0.3/code 4. No installation, upload or publication.
- `git diff --check` passes.

## Pending device checks

Waydroid remains in use by another workflow; the user requested source work first. No new APK has been installed, no device interaction performed, and no new screenshot claimed as verified.

1. Verify popup credit values against known 0/2/3/6-credit catalog entries and unavailable metadata, including after term changes.
2. Scroll Direct and Scroll grids at top/middle/bottom, with navigation visible/hidden, portrait/landscape, short windows and light/dark/OLED themes. Confirm no fixed black gap, smooth edge response, reachable final course blocks and no double bottom padding.
3. Verify empty state and course selector actions remain above navigation. Export PNG at several scroll positions and check that full rows/columns are retained without fading or navigation spacing.
4. Complete the remaining meeting export/calendar insertion/device checks in the timetable-export report.

Catalog failure leaves the timetable usable with unavailable popup credits and the legacy total estimate. Credit loading currently runs once per timetable ViewModel, using the existing repository cache; a fresh screen instance retries after a failed load.

## Subsequent Waydroid deployment

On user request, installed the optimized releaseValidation APK with the matching existing debug certificate using `adb install -r` on Waydroid Android 13 (`192.168.240.112:5555`). No uninstall or data clear. Pulled installed APK SHA-256 matches the archived validation artifact. MainActivity launched successfully; ARTEMIS hierarchy confirmed Home content and live catalog statistics. No AndroidRuntime errors in the latest 400 log entries. The production release certificate differs, so the signed release APK was not used for this data-preserving update.

This deployment supersedes the earlier installation deferral. Detailed popup/fade/PNG and calendar interaction checks above remain pending; only installation/startup were checked during deployment.
