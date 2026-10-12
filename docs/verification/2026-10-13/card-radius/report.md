# Card radius adjustment — October 13, 2026

Changed the shared card press shape from resting 16 dp / pressed 28 dp to **resting 28 dp / pressed 16 dp**. The existing spring, card sizes, borders, colors and actions remain the same. This applies to academic shortcuts, statistics cards, course/instructor cards and clickable review cards through `rememberCardPressShape`.

## Verification

- Optimized validation APK, signed release APK and release AAB built successfully. Existing Kotlin metadata diagnostics remain non-fatal.
- Installed the validation APK over the current Waydroid app after comparing signing certificates. No uninstall or data clearing (`installed.txt`).
- ARTEMIS hierarchy and ADB held/cancelled touch exploration verified both academic shortcuts and a course card. Screenshots show larger resting corners, smaller pressed corners and restored rounding after cancellation.
- Pixel checks in `press-bounds.json` confirm constant card bounds. At a fixed row near the top-left corner, the outside inset changes 12 → 4 → 12 px for both academic shortcuts and 11 → 3 → 11 px for the course card (rest/pressed/released), consistent with decreased rounding on press.
- Release artifact guard and `git diff --check` pass. No new tests were authored or full unit suite rerun for this numeric-only adjustment; the preceding card implementation passed 382 tests and its full navigation checks.

Current APKs, signed AAB, R8 outputs, build log and evidence are archived under `build/card-radius-2026-10-13/`, superseding `build/daily-card-shapes-2026-10-13/` for distribution. Normal output paths match. Version stays 1.0.3/code 4; nothing was uploaded or published.

Physical devices, other window/font configurations and disabled-animation settings were not rechecked in this follow-up.
