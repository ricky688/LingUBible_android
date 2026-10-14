# Connected-button dual-ripple verification — 2026-10-14

## Behavior

This supersedes the earlier fill/text color-merging interpretation. In Light Mode the primary fill enters from all perimeter edges and converges into the center. On the former selection, the empty center expands outward while the remaining primary wash fades at the boundaries. The shared press index starts incoming and outgoing focus together before selection commits. Cancellation restores the original selected button; re-pressing it replays a subtle inward highlight. Dark/OLED uses a uniform color crossfade, with no spatial color drawing.

The shared component retains native animated shape clipping, selected/checkmark semantics, callbacks and one preference-aware haptic per activation. Both standard and scrollable groups use it. Native keyboard-focus and hover layers remain; only the default press ripple is suppressed. Colors come from the selected Material scheme. Incoming fill is opaque; only the outgoing wash fades. Label tint follows the filled region rather than merging the label into its background.

## Results

- 411 JVM tests passed; zero failures, errors or skips. Three focus-state tests cover coordinated press focus, cancellation restoration and strict Dark Mode selection focus. Updated palette assertions cover Light Mode focus colors, released colors and unchanged Dark Mode press colors in red, blue and green palettes.
- Optimized `assembleReleaseValidation` passed on the existing toolchain. Existing nonfatal R8/Kotlin metadata warnings remain. No dependency, min SDK, signing or version changes.
- ARTEMIS screenshots/hierarchy observations grounded ADB interactions on Waydroid Android 13 (480×1000, density180). The final validation APK was installed over the compatible certificate using `install -r`; no data clear. Installed APK hash matches the final artifact.
- Recorded normal-speed Light and Dark/OLED selection changes, a 650 ms held Light press, re-presses and rapid Light selections. Returned to the planner with the retained CLC9022 selection/3-credit summary.
- Pixel checks exclude label pixels by comparing settled endpoint colors. At 1.511533s in the Light recording, primary-color coverage at the perimeter exceeded center coverage by 0.485 on the outgoing button and 0.997 on the incoming button. This confirms both perimeter rings in the same frame. In Dark Mode the maximum comparable edge/center difference was 0.016, below the 0.12 spatial-effect threshold.
- A canceled press generated zero requests and restored the original group exactly (0 changed pixels in the header crop). A selected-button tap still generated exactly one app-attributed Android haptic request. No current-process fatal AndroidRuntime exception was found.
- Restored **Light Mode**, the theme visible immediately before this task's device checks. Chinese, Lingnan Red, OLED preference, haptics and course data retained. System animator scale remained 1 throughout; no system setting was changed.

## Evidence

- [Light recording](light-motion.mp4), [Light motion frames](light-inward-outward-frames.png)
- [Dark/OLED recording](dark-motion.mp4), [Dark crossfade frames](dark-crossfade-frames.png)
- [Restored Light/planner](restored-light-planner.png), [cancellation before](cancel-before.png), [after](cancel-after.png)
- [Pixel checks](pixel-motion-check.json), [build/tests/install](build.json), [runtime checks](runtime.json)
- [Behavior/source guide](../../../connected-button-motion.md)

Local optimized APK, mapping/configuration and build log: `build/connected-button-motion-2026-10-14/`. This task produced a debug-certificate validation APK; the signed release APK/AAB from the earlier task were not rebuilt or published here.

## Limits

Waydroid's recording has variable frame cadence and cannot certify physical-device smoothness or 60/90/120 Hz behavior. Samsung/Pixel hardware, non-red custom/dynamic palette visual motion and reduced-motion device behavior were not exercised. Unit tests cover palette targets; Compose's standard duration scale controls the animations. Scrollable groups share the renderer but were not separately recorded. No new maintained instrumentation tests were authored.
