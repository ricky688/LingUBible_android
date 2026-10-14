# Connected-button timing — 2026-10-14

## Change

- A quick tap now completes the inward shape spring before the return starts. A hold reaches the same pressed outline and stays there until release, then returns to the current selected/rest outline. Click actions and haptics remain immediate.
- The helper observes native PressInteraction events. It finishes the inward Animatable, waits for release when necessary, then returns. Overlapping presses coalesce while the inward animation is running; a new press during recovery starts the next inward morph without accumulating stale queued animations.
- The Animatable is keyed to its interaction source, so rebuilding a category list resets a canceled/partial pose. The component interpolates the existing native Material 3 checked/unchecked/pressed outlines. A Shape wrapper bypasses ToggleButton's automatic release-driven retargeting. Rounded and rectangular (including zero-size collapsed-header) outlines are supported.
- Existing `gentleSpring()` drives shape and selection morphs, with no damping override. Native focus/hover layers, selection semantics, callbacks, label rendering and preference-aware haptics are retained. Standard and scrollable groups share the implementation.
- Light Mode perimeter focus and selected-button highlight use stiffness **800**, previously **200**. With the same spring settings this targets about half the old color duration. Dark/OLED's uniform color crossfade remains unchanged. Compose's system duration scale is retained.
- No new dependency, backend, preference, signing or version change. Current project version is **1.0.6 / code 7**; concurrent TopBar/campus-cat work was preserved.

## Verification

- The initial **424-test** run passed. The final locally captured suite contains **420 tests**, also with zero failures/errors/skips. Five new tests cover quick-versus-held completion timing, rapid-press recovery without a backlog, pressed/selected outline endpoints, zero-sized headers and faster color-spring duration. The animation tests use explicit 16 ms virtual frames and Compose snapshot notifications.
- Debug APK build and optimized `assembleReleaseValidation` passed. The first optimized process exited 143; retry passed. Existing toolchain metadata diagnostics did not prevent the successful build. Signed production APK/AAB were not rebuilt or published for this UI change.
- ARTEMIS observations grounded ADB gestures on Waydroid Android 13, 480×1000, density180. Debug and final optimized APKs were installed only after certificate checks, using `install -r` with no data clear. The device-verified APK is retained as `installed-base.apk`. The final local APK adds only an interaction-source reset guard; its reinstall was deferred after Waydroid switched to another dialog. Both hashes and validation stages are recorded in `build.json`.
- Final optimized Light recording contains 100 ms tap, 650 ms hold and another 100 ms tap on the selected leading button. Pixel checks found identical maximum pressed-corner coverage (**0.8813**) for all three; each returned to rest. The quick-tap shape continued visibly after release. Unit tests separately confirm that the inward spring completes at the same virtual time for 100 ms and 900 ms holds.
- Debug recordings verified the faster paired Light color motion and the same quick/held shape endpoint. Debug runtime checks: canceled drag generated zero haptic requests and restored the original header exactly; a normal 100 ms tap generated one request. Scrolling the header closed/reopening caused no fatal exception. Final optimized app also had no current-process fatal AndroidRuntime entry.
- System animator scale stayed at 1; no system animation setting was changed. Existing selected CLC9022 / 3-credit planner data remained visible. No login, logout, export or data-edit action was performed.

## Evidence

- [Final optimized quick/held recording](light-shape-timing.mp4), [quick-press frames](optimized-quick-shape-frames.png), [shape pixel checks](light-shape-pixels.json)
- [Debug quick/held recording](debug-shape-timing.mp4), [debug pixel checks](debug-shape-pixels.json)
- [Debug faster-color recording](light-motion.mp4), [faster-color frames](debug-fast-color-frames.png)
- [Build/install/tests](build.json), [latest captured suite](latest-unit-tests.json), [five motion tests](motion-unit-tests.json), [test XML](motion-unit-tests.xml), [debug runtime](debug-runtime.json), [optimized runtime](optimized-runtime.json)
- [Source/behavior guide](../../../connected-button-motion.md)

Local optimized APK, R8 mapping/configuration and build/test logs: `build/connected-button-timing-2026-10-14/`.

## Remaining checks

Waydroid switched to the campus-cats dialog during preparation for the final Dark Mode recheck. Further UI interaction stopped; the current Light appearance was left in place. The shared shape logic applies in both themes, and existing Dark color/focus unit tests pass, but this run did not complete a fresh Dark device recording. Scrollable groups share the helper but were not separately recorded. Physical Samsung/Pixel smoothness, custom/dynamic palette motion and a reduced-motion device check remain pending. Waydroid video cadence is variable; recordings are functional evidence rather than hardware-performance certification. No maintained instrumentation tests were added.
