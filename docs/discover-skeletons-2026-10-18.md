# Discover statistics skeletons — 2026-10-18

## Implementation

- The four Discover cards (verified students, course reviews, courses and instructors) render rounded skeleton bars for counts and trend pills during their initial statistics request. Icons and labels remain visible.
- `StatsCard.isLoading` drives a stiffness-only spring crossfade to real values. Placeholder text reserves the value's line metrics but is replaced at draw time and cleared from accessibility semantics; screen readers receive a bilingual loading state rather than fake numbers.
- Value text stays on one line, preserving card height through the transition. Skeletons use the selected Material color scheme and a subtle spring-driven opacity pulse. The pulse only runs for loading content and suspends when system animation scale is zero, including live changes of the setting.
- `HomeUiState.hasLoadedStats` distinguishes initial loading from refreshing loaded data, including legitimate zero counts. Refresh retains values; a failed initial request ends loading and allows retry. Repository requests and fallback behavior are unchanged.
- Design target: the user's [Material motion reference](https://m3.material.io/styles/motion/transitions/applying-transitions).

## Verification

- **550 unit tests passed**, including three new initial-load/refresh/failure tests and a pulse test covering animation disable/re-enable without a zero-duration busy loop. Runnable files: `HomeStatsLoadingTest.kt`, `StatsSkeletonPulseTest.kt`.
- ARTEMIS explored the four metric cards and a single pull-to-refresh before tests were authored: trace `9ad8893b-959e-4cde-91e6-6e67a806d122`. An earlier run hit provider 503 errors; diagnosis passed and the retry completed. Stored action history was inspected.
- Optimized release APK and AAB build succeeded; release guard PASS. Same-certificate `install -r` preserved app data; the pulled installed APK hash matches the build.
- Captured a real cold start on Waydroid Android 13 with normal animations, followed by an animations-off cold start. Restored the original animation scale afterward; no network settings were changed.
- Pixel checks confirm all four card border strips remain at the same positions while skeletons become values. A normal loading sample changes relative bar brightness from 92 to 99; the four bar crops are pixel-identical across animations-off frames 2, 3 and 5. Captures also show the completed statistics. No current-process AndroidRuntime fatal exception.
- `loading.png` and `loaded.png` are representative normal-mode frames; `loaded-final.png` shows the final device state. All raw frames, timings, pixel assertions, unit-test totals, source patch and build/install metadata are in `docs/verification/2026-10-18/discover-skeletons/`.

## Artifacts and limits

- Final APK SHA-256: `a779367702327229d9b59f0c7c4126f01bdc59db37e077b3eece9368bfe34817`.
- Final AAB SHA-256: `0b52d9e870c1adb8410d5f5f6fd7132cc5988c7270a98196913283763af83f3b`.
- APK, AAB, R8 mapping and build log: `build/discover-skeletons-2026-10-18/`.
- Version/signing/toolchain/backend settings unchanged. No user data cleared or edited; left on Discover with the four numeric metrics visible. Device size, density, font scale and animation scale match their original values.
- Physical Samsung/Pixel checks and a full device/font/window matrix were not repeated for this UI change. Existing nonfatal Kotlin/R8 metadata diagnostics remain.
