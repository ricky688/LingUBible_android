# Google Play notice remediation — 2026-10-10

## Scope

Enable optimized release builds and correct shared system-bar/inset ownership. Keep AGP 8.5.2, Gradle 8.9, Kotlin 2.0.21, min SDK 26, target SDK 36, release signing, application ID, version, backend, and stored data unchanged.

## Appwrite query regression and fix

The initial optimized validation APK compiled and launched but could not load queried data: the backend returned `Invalid query method:`. ARTEMIS reproduced an empty course list with and without BUS1102 filtering. Bytecode inspection confirmed `Query.toString()` passes the object to Gson, and the failed build’s R8 `usage.txt` lists the removed `method`, `attribute`, and `values` fields (see `appwrite-query-before-mapping.txt`). Narrow member rules now preserve these wire fields on `io.appwrite.Query`, plus the same actual reflection contract on `io.appwrite.Operator`. Model package keeps remain unnecessary; Appwrite HTTP-field protection remains in place.

The failed initial APK/AAB are archived only as diagnostic evidence under `build/play-review-2026-10-10/initial-optimized-query-regression/`; do not install those artifacts. The query repair was installed at 19:23; final artifacts are rebuilt again for the resize fix described below.

## Implementation

- Release and `releaseValidation` enable R8 and resource shrinking with `proguard-android-optimize.txt`. Validation initializes from release, uses the existing debug certificate and is non-debuggable.
- Removed broad Appwrite-model, domain-model and Vico keeps and duplicate coroutine rules. Appwrite and Vico supplied no packaged consumer rules in the inspected AARs; serialization/coroutines supplied their own. Models use map conversion/direct references. The actual reflected `Client.http` member has a narrow keep rule; its original name is verified in the optimized mapping.
- MainActivity owns system-bar appearance based on rendered theme background/surface luminance. It applies explicit light/dark transparent bar styles through `enableEdgeToEdge`, then disables navigation contrast enforcement on API 29+. This supports selected, dynamic, OLED and inverted themes rather than the system theme alone.
- Root scaffold owns measured top padding and horizontal safe-drawing/cutout insets, consumes those once and applies IME padding once. Destinations receive the measured visible bottom-navigation clearance or system navigation inset. Backgrounds remain full-window.
- Lists and forms use `ScreenInsets.kt`; review FAB/list spacing includes measured FAB height; calculator snackbars use shared clearance. Planner owns its fixed viewport clearance; selector no longer adds an unrelated 140 dp spacer. Fullscreen PDF dialog keeps independent bar ownership and protects horizontal cutouts. `adjustResize` remains enabled.
- Localized configuration now refreshes when system configuration changes. Root scroll thresholds use density-scaled dp.

## Build verification

- Baseline signed release APK and AAB built successfully before enabling R8. Offline attempt first needed the uncached Android lint artifact; subsequent build downloaded the dependency and succeeded.
- Existing 371 unit tests passed with no failures/errors after inset changes; final build/test confirmation is recorded below.
- Release lint prints Kotlin metadata diagnostics: Appwrite 27.2.0, coroutines 1.11.0 and Kotlin stdlib 2.2.21 have 2.2 metadata while the compiler/toolchain expects 2.0. The project already uses `-Xskip-metadata-version-check`; these diagnostics did not fail baseline or optimized builds. No toolchain upgrade or lint suppression was introduced.
- Baseline artifacts and size JSON: `build/play-review-2026-10-10/before/`.
- Final optimized artifacts, R8 mapping/configuration/seeds/usage/resources and test reports: `build/play-review-2026-10-10/after/` (updated after the query, resize and focus-restoration fixes).

## Pending checks

- Android 15/16 isolated emulator edge-to-edge enforcement in gesture and three-button modes: no API 35/36 image installed. Waydroid is Android 13/API 33; the installed SDK emulator image is Android 14/API 34.
- Physical Samsung/Pixel keyboards, cutouts, wallpaper palettes and navigation modes.
- Successful Google OAuth/account sign-in and callback with a real eligible account; do not submit credentials or create accounts during unattended validation.
- Play Console recalculated optimization/shrinking/obfuscation coverage after a separate upload. Artifact size reductions are not Play coverage percentages.

## Device verification and final measurements

### Final build and artifact sizes

Command (offline, exit 0): `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleRelease :app:bundleRelease :app:assembleReleaseValidation :app:testDebugUnitTest --offline --console=plain -q`

**374 tests, 0 failures/errors/skips.** APK signatures verified with build-tools 36.0.0 `apksigner`.

| Artifact | Before (bytes) | After (bytes) | Reduction |
| --- | ---: | ---: | ---: |
| Signed release APK | 17,899,046 | 4,056,207 | 77.34% |
| Signed release AAB | 16,535,265 | 6,993,865 | 57.70% |
| Uncompressed DEX (APK and AAB) | 55,497,040 | 4,418,392 | 92.04% |

Validation APK: 4,056,073 bytes, with the same DEX size (4,418,392 bytes) and release optimization settings. Variant DEX hashes differ; both hashes are recorded in `dex-hashes.json`. Hashes: `baseline-sizes.json`, `optimized-sizes.json`. Release and validation R8 outputs are archived separately; `mapping.txt` preserves `io.appwrite.Client.http -> http` and the manifest callback activity.

### Install and callback checks

- Waydroid installed certificate and validation certificate both SHA-256 `2b6a1d140ce234273a72bc0b9031cbc7672ebf628dc3b58e1a491e93072fe269`. `adb install -r` succeeded; no uninstall or clear-data command was run. First install remains 2026-10-08 17:23:39; last update 2026-10-10 19:58:16, version 1.0.2/code 3, target SDK 36.
- Release certificate remains SHA-256 `9c01dcc10feaa3abe2696bff3d03080a53f2b7fd8e8ca160d3e7c16423ce707e`.
- Cold launch via `am start -W` succeeded (3014 ms once after the fix; this is a smoke result, not a benchmark).
- Android resolves the configured Appwrite callback scheme to `com.lingubible.app/io.appwrite.views.CallbackActivity`. No fabricated authentication token was sent.
- ARTEMIS live hierarchy/screenshot confirms home data loads (585 students, 1323 reviews, 1083 courses, 791 instructors), red/OLED appearance and legible white status/navigation icons.
- Initial ARTEMIS Pro exploration (trace `1206a514-127f-4d54-a91a-a4c5e63b78a6`) was cancelled after reproducing the query regression and verifying sign-in/register UI and keyboard Back dismissal. It did not complete the full matrix. Waydroid lacks the optional scrcpy audio/opus encoder; recording startup logs errors. Screenshot/hierarchy observations work; recorder-process errors are not app crashes.

### Verified after the query fix

ARTEMIS live hierarchy/screenshots with direct ADB interactions verified:

- Default course list loads 1083 courses; filtering BUS1102 returns one course with 3.89 average GPA and seven reviews. Details show bilingual title, reviews, grade chart and teaching records. Papers display the expected sign-in gate. No review was posted.
- Instructor list loads; Dr. ABREFA BUSIA Kwaku detail displays SOC4321/SOC4324/SOC2101 teaching records.
- Planner selector loads course sections; calendar screen and honours statistics load (2025: 900 graduates, 119 first-class honours, 13.2%). No timetable entries were changed.
- Calculator remains at its original ungraded/zero-credit snapshot. JSON and CSV exports succeeded through Android pickers. The freshly exported JSON was selected and its identical snapshot restored; ungraded GPA and zero credits remained unchanged. Files remain in Downloads: `LingUBible-GPA-20261010-192739.json` and `LingUBible-GPA-20261010-192813.csv`.
- Final calculator actions/footer can be scrolled into view in portrait and landscape. Original portrait rotation 0 was restored; other original device settings and app appearance were unchanged.
- `appwrite-fixed-course-log.txt` records successful retrieval and no `Invalid query method` errors after the fix. `crash-buffer-after-fix.txt` was empty at that checkpoint; subsequent rotation exploration found the scrollbar crash described below.
- Representative verified screenshots: `appwrite-fixed-course-search.png`, `appwrite-fixed-reviews.png`, `appwrite-fixed-teaching-records.png`, `appwrite-fixed-instructors.png`, `optimized-honours-statistics.png`, `optimized-gpa-restore.png`, `optimized-calculator-final-actions.png`, and `optimized-calculator-landscape.png`.

### Reusable optimized-bytecode regression check

Run `python3 tools/verify-optimized-appwrite.py --serial 192.168.240.112:5555` after building `assembleReleaseValidation`. To check signed release, also pass `--apk app/build/outputs/apk/release/app-release.apk --mapping app/build/outputs/mapping/release/mapping.txt`.

This compiles `tools/AppwriteQuerySmoke.java` to a temporary DEX, reads Query’s current obfuscated name from its matching R8 mapping and runs it with the actual optimized APK on Android via `app_process`. It asserts the server-facing JSON contract for equality, sorting and limit queries. It neither installs an APK nor opens app data; temporary device files are removed. Both variants **PASS** (`appwrite-optimized-smoke.txt`, `appwrite-release-smoke.txt`). Existing debug unit tests alone cannot catch a release-only shrinking regression.



## Continued validation: appearance, authentication and PDF rotation

ARTEMIS live observations with ADB actions on the same Waydroid device confirmed:

- Light, dark, OLED, Material You dynamic and inverted accent themes render with readable system icons after transitions settle. Inversion changes the accent hue, as the existing settings describe. The color sheet dismisses with Back, and the settings footer is reachable. Screenshots: `optimized-theme-light.png`, `optimized-theme-dark.png`, `optimized-theme-oled.png`, `optimized-theme-dynamic.png`, `optimized-theme-inverted-sheet.png`, `optimized-settings-footer.png`.
- Registration displays name/email/password/confirm-password fields. With the keyboard open, the final action can be scrolled above it; Back dismisses the keyboard while retaining registration. No credentials were entered or account created. Screenshot: `optimized-auth-keyboard.png`.
- Google OAuth launches the browser and reaches Google's LingUBible account chooser. Cancel returns to the app. Callback scheme resolution remains `io.appwrite.views.CallbackActivity`; no callback tokens were fabricated. A full eligible-account sign-in remains pending. The account chooser screenshot was not archived because it displays account information.
- BUS1102's public syllabus successfully downloads and renders in the native fullscreen viewer (`optimized-pdf-portrait.png`). Rotating revealed a real crash in the underlying shared lazy-list scrollbar: during a transient zero-height draw it called `coerceIn(40.5f, 0f)`. The mapped stack identifies `MouseModifiers.kt`, rather than an Appwrite or PDF parsing failure. Evidence: `pdf-rotation-crash.txt`; matching prior artifacts/mappings are retained under `build/play-review-2026-10-10/before-resize-fix/` for diagnosis only.
- Both lazy-list and regular scrollbars now cap their preferred minimum thumb height to the current viewport, including zero. Three regression unit tests cover zero-height rotation, short windows and normal proportional/minimum thumb sizes. PDF pages also reserve the navigation-bar region so light navigation icons cannot overlay a white page; horizontal safe-drawing insets protect landscape navigation rails as well as cutouts.
- Closing the PDF after rotation also restored platform light-bar flags over the dark app (`before-bar-focus-review-final-item.png`). MainActivity now reapplies its rendered-theme bar policy on configuration changes and window focus recovery. The final optimized APK passes the same PDF open → landscape → portrait → Back sequence. White status/navigation icons remain visible on dark surfaces, and `system-bars-after-pdf.txt` confirms the light-bar flags are cleared. Screenshot: `optimized-pdf-dismissed-bars.png`.
- Original red/dark/OLED preferences were restored (dynamic/inversion disabled); portrait rotation 0 was restored. No app data was cleared.

## Final acceptance checks

- Final build command exited 0: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleReleaseValidation :app:assembleRelease :app:bundleRelease :app:testDebugUnitTest --offline --console=plain -q`. Logs: `build/play-review-2026-10-10/after/final-bar-focus-build.log` (existing Kotlin metadata diagnostics remain non-fatal). All 374 unit tests pass, including the three scrollbar regressions.
- Both final APKs pass the optimized Appwrite equality/sort/limit wire-format smoke check again. Signed release certificate is unchanged; validation was installed at 19:58:16 with `adb install -r`, preserving first-install date and calculator data.
- Final PDF portrait/landscape rendering, final-page scrolling and Back dismissal pass. Navigation icons remain readable over the protected background. Screenshots: `optimized-pdf-portrait-fixed.png`, `optimized-pdf-landscape-fixed.png`, `optimized-pdf-final-page.png`, `optimized-pdf-dismissed-bars.png`.
- Final course review scroll collapses the FAB and keeps the last review's Read more/vote actions clear; the grade-distribution tab has no review FAB. Screenshots: `optimized-review-final-item.png`, `optimized-review-other-tab.png`.
- Calculator still shows the original ungraded GPA and zero credits after the final reinstall (`optimized-calculator-preserved.png`). Original portrait/density/font and red/dark/OLED preferences are restored. `waydroid-final-configuration.json` records final device values.
- `crash-buffer-final.txt` has no entries since the final installation. The previously captured crash is retained as pre-fix evidence, not a current failure.
- Current distributable artifacts and matching R8 outputs are in `build/play-review-2026-10-10/after/` and normal `app/build/outputs/` paths. Earlier diagnostic archives must not be used for installation.
- Remaining acceptance limits are the Pending checks above: API 35/36 navigation enforcement, physical Samsung/Pixel checks, full eligible-account OAuth, and Play Console recalculation. No upload or publication was performed.

## References

- [Google Play optimization requirement](https://support.google.com/googleplay/android-developer/answer/17492799?hl=en)
- [Enable app optimization](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization)
- [Compose inset ownership and consumption](https://developer.android.com/develop/ui/compose/system/insets-ui)
