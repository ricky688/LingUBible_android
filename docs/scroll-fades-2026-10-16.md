# Scroll fade visibility — 2026-10-16

## Requested behaviour

- Keep each fade visible while content remains in that direction, including when scrolling stops.
- Clear the top edge immediately at the top and the bottom edge immediately at the bottom.
- Retain both fades when resting in the middle; smoothly reduce fade height from 34 dp while scrolling to 22 dp at rest.
- Content that fits its viewport receives no fade.

## Source changes

- `FadingBlurModifier.kt`: ScrollState, LazyListState and LazyGridState share one renderer and visibility policy. Spring opacity is clamped to 0–1; reached edges bypass any remaining animated opacity. Fade depth is bounded for short viewports. Views with no remaining content use normal compositing; visible fades retain offscreen compositing.
- `ScrollFadingContainers.kt`: shared lazy column and vertical-scroll integration. Main-page lazy lists use measured navigation clearance. Sheets, selection dialogs and already-inset viewports use zero extra clearance. The helpers add no padding and preserve native scrolling, item keys, existing list state and callbacks.
- Applied across Discover, course/instructor lists and details, review browsing/forms, favourites, readings/materials/papers, GPA calculator/statistics, calendar, settings/profile/authentication, timetable selection and dialogs, theme/review/filter sheets, avatar grids, PDF reading and the navigation drawer.
- Existing timetable/category-grid fades use the same policy. Horizontally scrolling connected button groups also retain fades at rest while more items remain and clear their corresponding reached edge immediately.
- Removed the permanent 48 dp scrim above bottom navigation; otherwise it would continue covering content while the page's fade was hidden. Bar glass, icons and divider remain. Hide travel no longer reserves the removed decorative scrim.
- PDF fade is outside its zoom/pan transform and inside its system navigation inset.

## Persistent-idle follow-up

The user's later request supersedes the original scroll-only behavior. The shared visibility helper now depends only on remaining content; horizontal button-group fades follow the same rule. All 527 unit tests and the optimized APK/AAB build pass. Waydroid checks confirmed persistent middle fades, independently cleared top/bottom boundaries, and preserved account data. See [verification report](verification/2026-10-16/persistent-fades/report.md) for screenshots, artifacts, exact results and remaining coverage.

## Original validation (before persistent idle fades)

- Core-policy increment: unit suite passed.
- Final source: 508 JVM tests pass with zero failures/errors/skips. New tests cover all idle positions, scrolling at top/middle/bottom, non-scrollable content, immediate boundary clearing and spring opacity overshoot. Existing bottom-fade/inset geometry tests remain passing.
- Final signed APK/AAB build successfully; release guard PASS with matching R8 mapping, 862 renamed app types and the unchanged release certificate. APK: 4,279,523 bytes; AAB: 7,623,859 bytes. Version remains 1.0.6/code7. Artifacts/mapping/logs: `../build/scroll-fades-2026-10-16/`. APK SHA-256: `babd24c95782f48a73c52be59f1ecb78a54dcaad659fd962a82fbab227747b22`. Results, certificate, coverage inventory and copied test XML: `verification/2026-10-16/scroll-fades/`. The final source was recompiled/tested after drawer integration; release tasks then remained up to date.
- No backend, stored data, preference, dependency, signing or version changes.

## Original device checks pending

No Waydroid installation or UI interaction: the user's earlier instruction was to finish source changes first while another app uses the device. No screenshots or on-device verification claims for this change. Check idle/top/middle/bottom, fling settling, short content, bottom navigation showing/hiding, keyboard, theme changes, sheets and PDF zoom on device before release. No UI instrumentation tests were authored for unverified device paths.

The existing effect is a gradient/alpha dissolve that simulates a frosted edge; this change does not introduce a new full-content blur pipeline. Existing build-toolchain metadata diagnostics are tracked in prior handoffs and do not change the reported build result.
