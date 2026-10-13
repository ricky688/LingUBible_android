# Logo rain ease-out — October 13, 2026

Replaced the quadratic accelerating fall trajectory with Compose LinearOutSlowInEasing. Icons now move quickly at the start and slow toward their exit. The existing stiffness-only spring, staggering, rotation, repeat-tap behavior, system animation preference and touch-through overlay remain.

- 404 unit tests pass, zero failures/errors/skips. Added a regression check that displacement decreases across equal progress intervals; stagger, exit and monotonic movement checks still pass.
- Optimized validation APK, signed release APK and signed AAB build successfully. Local release guard PASS; details beside this report. Version remains 1.0.3/code 4; no publishing.
- Installed matching-certificate validation APK with adb install -r on Waydroid Android 13, preserving data. Pulled APK hash matches current output. App-scoped recent AndroidRuntime log is empty.
- ARTEMIS hierarchy confirmed logo/current app; timed grounded ADB captures verify live fall and cleanup. early.png, middle.png, late.png and finished.png have capture timings in capture-timing.json. The speed regression test confirms decreasing displacement independently of random icon positions.
- Physical Samsung/Pixel and rotated/split-screen runtime checks were not repeated. Prior repeat-tap/navigation/system-animation checks remain recorded in the icon-rain report; no unrelated flows were retested.

Artifacts: build/icon-rain-ease-out-2026-10-13/.
