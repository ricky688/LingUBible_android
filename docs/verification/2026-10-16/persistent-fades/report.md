# Persistent scroll fades — 2026-10-16

## Behavior

- Each vertical edge stays faded whenever more content remains in that direction, even after dragging or flinging stops.
- Top: bottom fade only. Middle: both fades. Bottom: top fade only. Content that fits: neither fade.
- Horizontal connected button groups follow the same remaining-content rule.
- Existing spring transitions, 22 dp resting / 34 dp scrolling height, selected colors, inset geometry and immediate boundary clearing are retained.
- Shared ScrollState, LazyListState and LazyGridState integration reaches existing pages, dialogs, sheets and PDF views without changing their saved state or padding.

## Validation

- 527 JVM tests passed; zero failures, errors or skipped tests. Updated visibility regression and existing geometry results are included here.
- Scoped whitespace check passed. An unrelated pre-existing trailing blank line in TimetableCreditsViewModelTest.kt was left unchanged.
- Signed optimized APK and AAB built successfully; release guard PASS. APK/AAB DEX and packaged R8 mapping match, and the navigation enum remains preserved. Existing Kotlin/R8 metadata diagnostics remain a baseline toolchain limitation.
- Installed the signed release on Waydroid Android 13 after checking certificate compatibility. Installed APK SHA-256 matches the release guard. No uninstall, data clear or sign-out; account remains signed in.
- ARTEMIS hierarchy observation and grounded ADB interactions verified the actual course-list and native changelog paths. Screenshots were captured after 2–3 seconds of idle time: course top has only its bottom fade; course middle retains both; changelog top has only its bottom fade; changelog bottom retains its top fade and clears its bottom fade. An extra swipe at the changelog bottom produced identical content pixels, confirming the reached boundary; footer actions remain reachable.
- Final device screen: course list at its restored middle position, with persistent fades. No UI instrumentation tests were added.
- Artifacts, mapping and build logs: `build/persistent-fades-2026-10-16/`. Detailed hashes and results accompany this report.

## Not checked in this pass

Physical Samsung/Pixel hardware, Android 15/16, alternate themes, orientation/font-size changes, each individual screen/sheet/PDF, and horizontal-category scrolling were not re-tested. They use the shared policy or the matching horizontal visibility rule. The existing gradient/alpha effect is retained; no new blur pipeline or performance certification is claimed.
