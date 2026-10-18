# Material 3 Expressive motion consistency — 18 October 2026

## Changes

- One native `MotionScheme.expressive()` now supplies the theme and custom motion: slow spatial springs for page travel, default spatial springs for tabs/layout/headers, fast spatial springs for press shapes, and non-overshooting effect springs for opacity and color.
- Root navigation, top/bottom bars, Academic Tools, Calculator/Statistics, honours filters/values, Discover categories, authentication switching, calendar paging and timetable preview crossfades use those roles. Transition directions, selected themes, state and measured insets are retained.
- Academic tab direction now uses the transition’s own initial and target states, avoiding a separate remembered previous-tab value during rapid selection.
- Honours programme cards morph their corners instead of shrinking; popular cards and review actions no longer lift on press. Hover feedback remains.
- Course/instructor directories use theme-aware grouped skeleton cards and an effects-spring crossfade on initial loading. Discover and directories share the reduced-motion-safe pulse. Cached data stays visible during refresh. Placeholder fields expose a loading description rather than fake content/actions.
- Connected-group light-only inward/outward color ripples, dark crossfades and complete quick-tap shape cycles are unchanged. Bespoke chart/zoom/illustration motion remains separate; this is not a blind rewrite of every animation.

## Validation

- ARTEMIS explored actual directory navigation and Calculator/Statistics switching before test changes: trace `a8691632-3a16-4bfc-9bc7-188eaa88fab2`, completed on Waydroid `192.168.240.112:5555`.
- `testDebugUnitTest`: **551 passed**, zero failures/errors/skips. Includes both directions of effects-spring opacity bounds, reduced-motion skeleton behavior, existing connected-button timing and grouped-shape tests.
- Optimized release APK and release AAB built successfully, version 1.0.6 (7), min SDK 26, target SDK 36. Release guard passed; matching R8 mapping embedded. Existing nonfatal Kotlin-metadata/R8 warnings remain.
- APK 4,463,521 bytes; AAB 7,987,377 bytes; uncompressed DEX 5,199,612 bytes; 999 renamed application types. These are local checks, not Play Console coverage percentages.
- Signing certificate matches the previously installed release. The earlier candidate update preserved data; final source cleanup only changes indentation, unused imports and trailing whitespace. The final artifacts are rebuilt and kept locally.

Final source cleanup passed all 551 tests again; optimized APK/AAB build completed in 9m 6s and the release guard passed. The user subsequently authorized Waydroid testing, and the exact final APK was installed and verified. The source whitespace check passes.

## Waydroid validation — completed after user authorization

The final APK’s installed SHA-256 matches `94734e7dd3b5dc210a30b2a4e7668ef9d9dba3ec80a29bd43e7779525b770c83`. Android’s shared installer first waited behind a VibeFinance dex2oat process, then held LingUBible frozen during its own optimization. The Android CLI install timed out. Restarting the reserved Waydroid user session released the lock; the installed package opened normally. Post-restart hash, live screens and account/timetable checks confirm the update. No uninstall or data clear was used.

| Check | Result |
| --- | --- |
| Discover counts and cached refresh | Real counts retained during refresh; returned successfully |
| Course list/detail/back | 1083 courses; ACT1110 detail loaded; back worked |
| Instructor list | Loading semantics observed, then populated rows (450 / 791 under existing defaults) |
| Academic tabs/calendar | Timetable/calendar/GPA opened; month changed forward and back |
| Export preview | Opened and dismissed; no export/calendar insertion |
| Calculator/Statistics | Repeated switching recorded and inspected, including an in-flight slide/fade |
| Honours comparison | Comparison wording and deltas appeared and collapsed; original off state restored |
| Honours card press | Outer corners morph; width/text stay in place; grouped inner corners preserved |
| Light/dark appearance | Light checked, original Dark restored; red palette/OLED settings untouched |
| Data/settings | Account remains signed in; timetable still has 3 courses / 9 credits; device settings restored |
| Runtime | No fatal exception logged for the tested app process |

Screenshots: `final-home.png`, `final-statistics.png`, `final-instructors.png`, `final-light-gpa.png`, `final-card-rest.png`, `final-card-held.png`, `final-restored-home.png`. Recordings: `final-tabs.mp4`, `final-card-press.mp4`, `final-directory.mp4`. Full check details and limits are in `device-checks.json`; installation/settings evidence is recorded separately. No private settings-page screenshots or account email were added to the evidence.

ARTEMIS diagnosis passed all five required checks after reconnecting. Its optional accessibility helper loopback was not answering after the session restart, so final inspections used Android CLI layout and ADB. Optional repair guidance, if needed later: `uv run artemis helper install --serial 192.168.240.112:5555 --force`. No helper reinstall was needed for this run.

## Artifacts

- Release archive: `build/expressive-motion-2026-10-18/` (APK, AAB, mapping/configuration, build log).
- Evidence: `docs/verification/2026-10-18/expressive-motion/` (unit results, release guard, source patch/hashes, exploration record and final device evidence).
- APK SHA-256: `94734e7dd3b5dc210a30b2a4e7668ef9d9dba3ec80a29bd43e7779525b770c83`.
- AAB SHA-256: `a2f9866d5a3c9ad9716110f81dc2b8f7a3101312841f9257be7ea862d6399f95`.

## Design reference

Uses the already installed Material3 1.5.0-alpha01 API; no dependency or toolchain upgrade. Android CLI documentation search/fetch and the installed API were checked. The official [Androidify Compose guidance](https://android-developers.googleblog.com/2025/05/androidify-building-delightful-ui-with-compose.html) recommends shared MotionScheme tokens; see also [Material motion](https://m3.material.io/styles/motion/overview/how-it-works) and [MotionScheme API](https://developer.android.com/reference/kotlin/androidx/compose/material3/MotionScheme).

## Limits

Waydroid runs Android 13. Physical Samsung/Pixel, Android 15/16, high-refresh performance, broader size/font/orientation coverage and signed-out Sign-in/Register transitions remain pending. Only the current 480×1000, density 180, 100% font environment was checked in this run; dual-ripple timings were not quantified. No publishing, account/data changes or new backend behavior is included.
