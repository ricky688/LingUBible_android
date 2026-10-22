# Daily review expansion — 2026-10-21

Added `animateContentSize(AppMotion.spatial())` to the comment Text in the shared ReviewCard. Expanding 閱讀更多 and collapsing 收起 now animate the text slot height, moving the disclosure control, footer and next card together. The existing Material 3 Expressive spring and platform animation scale apply. No new dependencies, timing constants or damping overrides.

Callers inspected: Discover feed, course reviews, instructor reviews, review directory and editor preview. All use the shared card. The change does not alter text, three-line collapsed limit, votes, detail navigation or stored data.

## Validation

- Optimized release APK build passed (2m33s). Existing nonfatal Kotlin metadata/R8 warnings remain.
- Matching release certificate retained. Installed as an update on Android 13 Waydroid (480×1000); installed APK bytes match the build.
- ARTEMIS trace `8ea9ae2b-b08d-450b-97b4-6708f190bd34` explored the disclosure tap. Stopped the delayed task after its expansion action; execution trace inspected. Its daemon stderr path is not accessible on this host.
- Final build checked using semantic 閱讀更多/收起 locators and a bounded screen recording: expansion showed the full comment, then collapse restored the three-line preview. The disclosure moved from y336 to y634 and returned to y341 (small list-anchor shift). The first check used an unnecessarily strict 2px position tolerance; state restoration passed, and the 5px displacement was reviewed in the recording.
- `expand-collapse.mp4` and inspected `transition-frames.png` show the interaction. Layout snapshots document each state. No reviews were submitted or voted on. App left on the Daily/Discover feed with comments collapsed.
- No new logic or test code was added for this cosmetic change. The unit suite was not rerun; all 554 tests passed on the immediately preceding scrollbar change, retained in this build. Physical-device and disabled-animation checks were not repeated.

APK SHA-256: `2a6d29bf2aafb723768dc36745759dd876da722e23da73c3bc9c54ba54581b8d`
