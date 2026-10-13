# Logo rain Easter egg — October 13, 2026

## Behavior and design

A single tap on the existing top-left LingUBible app logo starts a burst of 36 falling logos across the current screen. Uses the existing red book artwork, randomized sizes/rotation/drift and staggered entry. Repeated taps restart a bounded burst rather than accumulating particles. The effect leaves no persisted state or animation loop after completion.

The top-level logo is now an IconButton with a 48 dp touch target and existing brand accessibility label. Detail pages retain their back button. A root drawWithContent modifier paints above app content and bars without a pointer handler or new accessibility nodes; navigation remains usable during the effect. It uses one stiffness-only spring, reads animation progress in drawing rather than recomposing every frame, and uses current viewport dimensions. ValueAnimator.areAnimatorsEnabled suppresses the effect with system animations off; Compose's animation clock respects duration scaling. No sound/haptic changes, dependencies, preferences, backend or signing changes.

ImageToolbox was the user's interaction inspiration; this implementation uses this app's existing artwork and repository motion rules. No external assets or code were copied.

## Checks completed

- 403 JVM tests pass, zero failures/errors/skips. Three new tests check above-screen stagger, complete exit for short/tall viewports and monotonic fall without re-entry.
- Optimized validation APK, signed release APK and signed AAB build successfully. Local release guard results are alongside this report. Version remains 1.0.3/code 4; no upload/publication.
- Matching certificate verified before adb install -r; no uninstall/data clear. Pulled installed validation APK SHA-256 matches build output.
- ARTEMIS hierarchy verified app/logo state before grounded ADB taps. Timed ADB screenshots capture the transient effect without model latency. burst.png and repeated-tap.png show live falling icons; finished.png shows cleanup. navigation-during-rain.png shows Academic Tools reached while the same burst remains visible, verifying touch-through navigation.
- Temporarily set animator_duration_scale to 0; animations-disabled.png shows no falling icons after tapping. Restored original value 1 in finally and verified restoration.
- No app-PID-scoped AndroidRuntime errors in the recent log window. Existing selected timetable course remains untouched.

## Pending

Physical Samsung/Pixel checks, TalkBack/keyboard manual activation and rotated/split-screen runtime views were not repeated. Unit trajectory checks cover short/tall viewport geometry. No new instrumentation tests were authored.

Artifacts and R8 mappings: build/icon-rain-2026-10-13/.
