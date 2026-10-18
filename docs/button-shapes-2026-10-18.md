# Shared button press shapes — 2026-10-18

## Behavior and integration

- App buttons morph their corners to **8 dp** on press and return to their resting shape on release. Their layout size and text size stay fixed.
- `AppButtonDefaults` centralizes the native Material 3 `ButtonShapes` and `IconButtonShapes`, plus the stiffness-only spring for custom Surface actions. No dependency or experimental Styles API migration.
- Wired every standard text/icon button call: 37 filled, 28 outlined, 9 tonal, 80 text, 37 icon and 1 tonal icon buttons (192 total). Includes dialogs, authentication, information pages, planner, GPA, reviews and PDF controls.
- Eleven custom press sites also share the policy: home CTA, course category grids, syllabus, review FABs and timetable export/preview controls. Removed the category selector scale-down and home/export shadow drops. Existing resting radii are retained.
- Connected groups and split buttons retain their existing complete quick-tap morph and light-only dual ripple; their four implementation files are byte-for-byte unchanged in this task.
- Theme colors, haptics preference, click handlers, disabled semantics and touch target sizing are preserved. Navigation entrance animations, selection/success bounces and grouped-card shapes remain separate.

For new controls, pass `shapes = AppButtonDefaults.shapes()` or `shapes = AppButtonDefaults.iconShapes()` to the Material button. Custom Surface controls use `AppButtonDefaults.pressCorner(interactionSource, restingCorner)` with the same interaction source as the clickable Surface. This uses the installed [Material 3 shape API](https://developer.android.com/reference/kotlin/androidx/compose/material3/ButtonDefaults), rather than changing the shape scale of unrelated components.

## Verification

- Unit tests: **546 passed**, no failures, errors or skips.
- Source audit confirms all 192 standard button calls use the shared defaults and the connected implementation is unchanged. See `source-audit.json` and task-only `source.patch` in the evidence folder.
- ARTEMIS explored Guide → Next → Previous (trace `337a66ce-b96e-487d-a50f-e7de1350718f`) and Academic tools → timetable → Export PNG/PDF → scroll to Cancel/Save (trace `1494490f-460c-4645-8cef-7c6b97cad770`). No save or publishing action was pressed.
- First-pass optimized build screenshots show the guide button in dark/OLED and light mode, during and after a hold. Its colored bounds stay **119 × 45 px** while the top edge widens from 81 to 105 px as the corners become squarer. Disabled Previous stays inactive; Next advances once on release; Previous returns to step 1. Light-mode check restored the original Dark/OLED settings.
- First-pass screenshots and JSON records are marked by the matching `installed-first-pass.json`; the shared defaults are unchanged in the final custom-button build. Final release/install and custom-button checks are recorded below.

Evidence: [verification folder](verification/2026-10-18/button-shapes/).

## Limits

Representative controls were checked on Waydroid Android 13; this is not an exhaustive visual pass through every screen. Physical-device haptics, Samsung/Pixel rendering and Android 15/16 remain unchecked for this change. ARTEMIS screen recording finalization failed, so animation evidence uses captured frames. One temporary UIAutomator query exited 137; ARTEMIS end-to-end diagnosis passed and subsequent checks continued normally. Existing nonfatal Kotlin/R8 metadata diagnostics remain; no build toolchain changes were made.

## Final optimized artifacts

- Optimized APK and AAB built successfully; release guard **PASS**. Version remains 1.0.6 (code 7), with no signing/version/toolchain migration.
- APK: 4,463,519 bytes; AAB: 7,974,612 bytes; DEX: 5,194,256 bytes. R8 mapping matches, with 992 renamed application types present. These are local checks, not Play Console coverage percentages.
- Installed with `adb install -r` after checking certificate compatibility. Pulled installed APK hash matches the built APK; app data was not cleared.
- APK SHA-256: `1b60c6fca26778b06b5609d4bb3caba7cd06f1618d88ee2270f4fcd96c0852eb`.
- AAB SHA-256: `c7ad9d67b6fee66d9d64d33613f31b7a94669dc34b74904d8fd3f6655f984c37`.
- Artifacts and mapping: `build/button-shapes-2026-10-18/`. Previous first-pass artifacts are retained separately in `build/button-shapes-2026-10-18-first-pass/`.

## Final installed-build UI check

- Timetable export: held Cancel visibly morphs to the shared corner shape, release dismisses the panel. Save/export was not invoked; timetable entries remain intact.
- Home CTA: held press morphs the corners, release opens the course list. Returned to Home afterward. Pixel comparison confirms its colored bounds are identical at rest and held.
- Captures: `export-final-rest.png`, `export-final-held.png`, `home-cta-final-rest.png`, `home-cta-final-held.png`, `home-final.png` in the verification folder, all from the final installed APK above.
- Original display, font, rotation and animation scales match after checking. Original app Dark/OLED preference restored during the light-theme check. No current-process AndroidRuntime fatal exception.
