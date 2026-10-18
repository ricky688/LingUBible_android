# Guide fixed navigation footer — 2026-10-16

## Change

Previous, Next/Done and Close are outside the weighted scrolling body in the shared GettingStartedGuide. Both the first-launch sheet and Settings guide page use this layout. The footer reserves its measured height; actions wrap naturally while the instructions, progress/topic selector and reopen hint remain scrollable. Existing caller-owned bottom insets, bilingual text, theme colors, spring topic transitions, haptics and saveable state are preserved.

## Validation

- 527 JVM tests pass, with zero failures/errors/skips; existing guide preference, topic and route tests are included. Scoped whitespace check passed. No new UI test code was authored for this layout-only change.
- Optimized signed release APK/AAB built successfully; release guard PASS with matching DEX/mapping and preserved navigation enum. Existing Kotlin/R8 metadata diagnostics remain unchanged. Archived artifacts/mapping/log: `build/guide-footer-2026-10-16/`.
- ARTEMIS observations and grounded ADB interactions on Waydroid Android 13 confirmed the old problem (Next absent at topic top) and the new fixed footer.
- Advanced all seven topics without any scrolling; Next/Done labels remained visible at every step. Previous 7→6, Replay 6→1, and Close/reopen passed. Scrolling the instructions did not change Next's bounds.
- At 320 dp width (360×800 px / density 180) and 200% text, all three footer actions were within the visible screen; Next 2→3 and Previous 3→2 worked without scrolling. The guide body's oversized topic label is tight against the progress indicator at this extreme scale; this change only addresses footer reachability.
- Restored original 480×1000 window, density 180, font scale 1.0. Installed APK hash matches the release guard; certificate compatibility checked before replacement, with no uninstall or data clear. Final screen is Guide step 1.

## Remaining coverage

The first-launch sheet uses the same changed component but was not re-triggered, preserving the user's seen-guide preference. Physical devices, landscape, alternate themes, TalkBack and an actual Done tap were not re-tested. The Done control was visible on step 7 and retains its existing close callback. Existing guide-body layout at extreme font scales remains separate from this footer change.
