# Native first-launch guide — 2026-10-16

## Behaviour

- A native Material 3 welcome sheet appears on the first eligible Home visit. It does not interrupt authentication, shared-timetable import, other destinations or an open keyboard.
- Finishing, closing, dismissing by Back/outside tap/swipe, or following a feature shortcut marks the guide seen. The `getting_started_seen` Boolean lives in the existing `app_settings` preferences. It survives restarts and updates; colour reset leaves it intact. Existing installations without this new key get the guide once on their next eligible Home visit.
- **User Settings → Guide**, its existing Guide footer link, and **Appearance & Settings → Guide** open the native replay page. Opening that page also counts as seen, matching the web version. Replay resets the current tour without clearing the saved seen flag.
- One guide component supplies both the welcome sheet and permanent page. Seven topics follow the web sequence: welcome/discovery, reviews, materials, planner, GPA, calendar and contributing/personalisation.
- Topic menu, progress, Previous/Next, Done/Close, Replay and native feature shortcuts are included. Current topic and list state use Compose saveable state; switching topics resets their reading position while recreation preserves the current position.
- Bilingual copy follows the selected primary language, with the other language below. Instructions describe actual Android controls and features. The calendar describes month/date selection; materials do not claim all instructor metadata is known. No web-only keyboard, Markdown or calendar-view promises were copied.
- Theme colours, native sheet insets, semantic headings/action labels, preference-aware haptics, native button shapes and pure spring transitions are used. Previous, Next/Done and Close live in a fixed footer outside the scroll body, so advancing never requires scrolling. Long instructions and the reopen hint remain scrollable; footer actions wrap at narrow widths or large fonts. No online images or guide API are required.

## Source reference

- Web: `../src/components/common/GettingStartedPrompt.tsx`, `GettingStartedGuide.tsx`, `../src/pages/GettingStarted.tsx`, and English/Traditional Chinese locale entries.
- Android: `ui/components/GettingStartedGuide.kt`, `GettingStartedSteps.kt`, `ui/screens/GuideScreen.kt`; `AppSettingsManager`, `MainActivity`, navigation, Profile and Settings entry points.

No existing preference is migrated or deleted. No backend, dependency, minimum SDK, version or signing changes.

## Fixed-footer follow-up

The later UX request moves Previous, Next/Done and Close outside the scroll body in both guide presentations. All 527 unit tests and signed optimized APK/AAB builds pass. Waydroid verified advancing all seven steps without scrolling, fixed button position while reading, Previous/Replay/Close, and footer reachability at 320 dp with 200% text. Device configuration was restored. See [report and screenshots](verification/2026-10-16/guide-footer/report.md) for detailed results and remaining checks.

## Original validation

- **513 JVM tests pass** (zero failures/errors/skips). Focused checks cover default first-launch state, dismissal persistence, restart, theme reset and other-preference preservation, eligible Home/keyboard/destination gating, seven bilingual topics and planner/calendar/GPA shortcut destinations.
- Signed optimized release APK (4,329,148 bytes) and AAB (7,672,848 bytes) built successfully. Release guard **PASS**; certificate SHA-256 `9c01dcc10feaa3abe2696bff3d03080a53f2b7fd8e8ca160d3e7c16423ce707e` is unchanged. Mapping files and build log are archived with the artifacts in `build/getting-started-2026-10-16/`. Sizes and SHA-256 hashes are recorded in `verification/2026-10-16/getting-started/results.json`.
- Existing nonfatal build-toolchain metadata diagnostics remain unchanged.

## Original device coverage

The user last selected **Keep device testing deferred**. No Waydroid installation, app-data reset, guide-preference mutation, screenshots or device interactions for this work. No new UI instrumentation tests were authored without device exploration.

Device checklist: first eligible launch; all seven topics and shortcuts; Previous/Next/Replay; Close/Back/outside/swipe dismissal; relaunch without another prompt; both Settings entry points; rotation/recreation; light/dark/OLED/dynamic colours; large fonts/short windows; sheet and dropdown insets/accessibility. Preserve the user's installed data; use an isolated test installation or a controlled test preference fixture when checking first launch. Live review publication remains pending and no reviews/votes/accounts were changed.
