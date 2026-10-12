# Daily/main page card press shapes — October 13, 2026

## Changes

- Applied a shared Compose spring corner animation to every clickable card category on the Daily/main (Explore) page: Planner and Calendar academic shortcuts, course/instructor statistics links, popular/top-rated course and instructor cards, and latest review cards.
- Resting corners remain 16 dp; held presses morph to 28 dp and return on release/cancel. Card size and text size stay fixed. Surface/Card borders, clipping and shadows use the animated shape. No damping overrides or dependency changes.
- Course/instructor cards now use the native Material 3 clickable Card interaction source. Their previous whole-card press/hover scale is removed; hover border/elevation feedback remains.
- StatsCard and ReviewCard accept their existing navigation callbacks directly, so the same interaction source observes presses. Home and public reviews callers were updated; non-clickable statistics/review containers stay passive, and nested controls keep their own actions. Shared course/instructor components also give the lists matching shape feedback.
- Current selected red/dark/OLED palette, content, layouts and routes retained. No data, settings, authentication or backend changes.

## Validation

- Explored the original and updated Waydroid app through ARTEMIS hierarchy/screenshots and ADB touch actions (single connected device 192.168.240.112:5555, API 33, 480 × 1000, density 180).
- Captured resting, held and cancelled press states for Planner, Calendar, linked statistics, course, instructor and review cards. Visual inspection confirms corner morphs. Pixel checks find identical outer bounds and restored corner pixels for all six examples (`press-bounds.json`). Held presses were cancelled by moving outside the target, with UP sent in a finally block.
- Verified taps open Planner, Calendar, course and instructor details, the course/instructor lists from their statistics links, and the associated course from a latest review. Verified Back returns to the main page. The app remains on the main page with academic cards visible.
- All **382 existing unit tests pass**, with zero failures/errors/skips. No new UI tests were needed for this visual change.
- Optimized validation APK, signed release APK and release AAB build successfully. Existing Kotlin metadata diagnostics remain non-fatal; toolchain unchanged.
- Both optimized Appwrite bytecode smoke checks PASS; release guard PASS (matching APK/AAB DEX and embedded R8 mapping, 635 renamed application types in shipped DEX). Local type count is not Play Console coverage.
- Installed the optimized validation APK using replacement installation after checking its certificate matches the installed APK. No uninstall or data clearing. `installed.txt` records first install/update times. `git diff --check` passes.

## Evidence and artifacts

- `planner-*`, `calendar-*`, `stats-*`, `course-*`, `instructor-*`, `review-*`: rest, held, released and navigation screenshots; `before-tools-*` shows the earlier static academic shortcut shape.
- `press-bounds.json`, `build-results.json`, `release-verification.json`, both Appwrite smoke outputs and `installed.txt` record checks.
- Current APKs, release bundle, release/validation R8 outputs, build log, unit test XML and this evidence are archived in `build/daily-card-shapes-2026-10-13/`. Normal `app/build/outputs/` artifacts match. This is newer than the previous buttons/My reviews archive.

## Limits

Verified on Waydroid with the current theme and portrait configuration. Physical Samsung/Pixel behavior, other orientations/font scales and disabled-animation settings were not rechecked in this visual-only follow-up. No votes, favorites or review submissions were performed; no publishing/upload. Version remains 1.0.3/code 4; a future Play upload must use a higher code if code 4 was already uploaded externally.

API references: [Material 3 Card](https://developer.android.com/reference/kotlin/androidx/compose/material3/Card.composable), [Compose interaction sources](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/handling-interactions).
