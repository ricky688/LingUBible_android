# Google Play notices — release 1.0.3 (code 4)

## Result

The optimized release and edge-to-edge changes from October 10 remain in place. Rebuilt them as **1.0.3 / version code 4**, so the affected Play release (1.0.2 / code 3) can be replaced. The code fixes and local verification are complete; Play Console reassessment requires a separate upload and processing. No upload or publication was performed.

## Fixes carried into this release

- Release enables R8 optimization/obfuscation and resource shrinking with `proguard-android-optimize.txt`. Broad package keeps were removed; actual Gson wire fields on Appwrite Query/Operator and reflected `Client.http` remain protected. This preserves the October 10 data-loading repair.
- `enableEdgeToEdge()` remains active. MainActivity applies transparent system bars and icon brightness from the selected rendered theme; navigation contrast is configured afterward. The policy is reapplied after rotation and window-focus recovery.
- Shared inset ownership uses measured scaffold/bottom-navigation clearance, system/cutout insets and IME avoidance. Content consumes padding once. Lists, forms, FABs, snackbars and fullscreen PDFs use the policy. `adjustResize` remains enabled.
- Existing appearance preferences, release certificate, app ID, min SDK 26, target SDK 36 and build toolchain are unchanged. The validation variant is optimized and non-debuggable, signed with the installed debug certificate to preserve Waydroid data.

The detailed implementation and original full device matrix are in [October 10 verification](../2026-10-10/play-review.md). Its earlier artifacts are historical evidence; use the code-4 artifacts below for this release.

## New release guard

Run after building the signed APK and AAB:

```sh
python3 tools/verify-play-release.py
```

The guard verifies the effective R8 configuration, actual renamed application descriptors in shipped DEX, matching APK/AAB DEX, the exact mapping embedded in the AAB, and version code >= 4. It passed with **627 renamed application types present in DEX**. It also rejected disabled obfuscation, stale mapping and an outdated version in focused negative checks. This count is evidence of obfuscation, not a Play Console coverage percentage.

`release-verification.json` and `release-guard-negative-checks.json` record the results. Older AGP 8.5.2 does not emit the newer `r8.json` metrics; the matching mapping is embedded in the AAB. No toolchain migration was introduced.

## Build and artifacts

Offline build exited 0:

```sh
ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleReleaseValidation :app:assembleRelease :app:bundleRelease :app:testDebugUnitTest --offline --console=plain -q
```

**374 unit tests passed; 0 failures, errors or skips.** Existing Kotlin metadata diagnostics remain non-fatal. Actual optimized Appwrite equality/sort/limit wire-format checks pass for both release and validation APKs (`appwrite-release-smoke.txt`, `appwrite-validation-smoke.txt`). This validates the release-only reflection contract without accessing user data.

| Artifact | Unoptimized baseline, Oct 10 | Code-4 optimized release |
| --- | ---: | ---: |
| Signed APK | 17,899,046 bytes | 4,056,202 bytes |
| Signed AAB | 16,535,265 bytes | 6,993,880 bytes |
| Uncompressed DEX | 55,497,040 bytes | 4,418,392 bytes |

Validation APK: 4,056,077 bytes. The release APK/AAB DEX are identical. Size reductions do not establish Play Console percentages.

Archived distributable artifacts:

- `build/play-review-2026-10-12/artifacts/app-release-1.0.3.aab` — signed bundle for the next Play release.
- `build/play-review-2026-10-12/artifacts/app-release-1.0.3.apk` — signed release APK.
- `build/play-review-2026-10-12/artifacts/app-releaseValidation-1.0.3.apk` — optimized debug-certificate build installed on Waydroid.
- Same archive: separate R8 mapping/configuration/seeds/usage/resource outputs for both variants, test XML, verification evidence and SHA-256 hashes. Build log: `build/play-review-2026-10-12/build.log`.

Signed release certificate SHA-256 remains `9c01dcc10feaa3abe2696bff3d03080a53f2b7fd8e8ca160d3e7c16423ce707e`. Validation certificate remains `2b6a1d140ce234273a72bc0b9031cbc7672ebf628dc3b58e1a491e93072fe269`.

## Waydroid verification today

ARTEMIS diagnosis reports ready. Live ARTEMIS hierarchy/screenshots with ADB interactions used the user-selected Waydroid `192.168.240.112:5555` (Android 13 / API 33).

- Installed validation code 4 using `adb install -r`; no uninstall or data clearing. First install remains October 8, 17:23:39; last update October 12, 22:00:49. Calculator still has its original ungraded GPA, zero counted credits and original empty course draft; planner remains empty.
- Light and restored red/dark/OLED themes show readable system icons. The registration confirmation field and Create Account action remain above the keyboard after scrolling. Back dismisses the keyboard while retaining registration; the following Back returns to the app. No credentials were entered or account created.
- Calculator final course/term/year actions and footer are reachable in portrait and landscape. Portrait rotation 0, original size 480x1000, density 180, font scale 1.0 and red/dark/OLED appearance were restored. Crash buffer is empty.
- Screenshots: `theme-light.png`, `auth-keyboard.png`, `calculator-preserved.png`, `calculator-final-actions.png`, `calculator-landscape-final-actions.png`. Configuration and install evidence: `waydroid-before.json`, `waydroid-final.json`, `installed-version.txt`, `system-bars-final.txt`, `crash-buffer.txt`.

### Earlier connectivity limitation — resolved later October 12

Today the device's pre-existing global proxy `192.168.240.1:8888` refused connections. Temporarily bypassing it and restarting the app produced request timeouts too. Host access to the backend health endpoint returned HTTP 200, while Android system services also logged network timeouts. Live course/instructor/PDF/backend flows could not be reverified in this environment today. There was no new invalid-query error; both optimized-bytecode Appwrite contract checks pass. Do not confuse today's network limitation with the fixed October 10 R8 query regression.

All five original proxy settings were restored exactly; `waydroid-proxy-original.json` and `waydroid-proxy-restored.json` match. Logs are in `waydroid-network-log.txt`. No backend or network code was changed to mask this condition.

### Connectivity repair and fresh checks

The later user-requested repair identified the missing listener for the existing Android proxy. The parent repository already contained `tools/scripts/waydroid_proxy.py` for host-VPN connectivity. Starting it restored API requests immediately in the same APK. The host route uses a VPN interface with MTU 1420 while Waydroid's Ethernet uses 1500; the exact lower-level cause of direct-forwarding stalls was not isolated further because the intended relay restores connectivity.

The helper now binds only to `192.168.240.1`, closes upstream sockets, limits request headers, reports failures and exits on bind errors. A user service starts it at login and restarts after 10 seconds, also retrying while the bridge is absent. A deliberate TERM verified automatic recovery (`NRestarts=1`); a subsequent relayed backend health request returned HTTP 200 in approximately 0.44 seconds. Live protocol checks return the expected 405/431/502 responses for unsupported HTTP, oversized headers and unreachable upstreams.

Fresh ARTEMIS hierarchy/screenshots and ADB logs verify **1083 courses retrieved**, refreshed platform statistics (**586 students**, 1323 reviews, 1083 courses, 791 instructors), BUS1102 filtering (GPA 3.89, seven reviews), course details/reviews and syllabus PDF rendering. The successful retrieval log has no new course timeouts or invalid-query errors. No app binary, timeout setting or user data changed. The working original Android proxy settings are intentionally retained, with the relay left enabled/running.

Evidence: `network-fix/app-network-after.txt`, `network-fix/service-recovery.txt`, `network-fix/service-journal.txt`, `network-fix/relay-protocol-checks.json`, `network-fix/proxy-settings.json`, `network-fix/course-search.png`, `network-fix/course-reviews.png`, `network-fix/syllabus.png`. Parent repository setup documentation is `docs/waydroid-network.md`.

## Remaining checks / release steps

1. Upload the **code-4 signed AAB** through the normal Play release workflow. Confirm code 4 and optimization/mapping details for that bundle; old code-3 notices describe the old upload. Wait for Play processing and inspect recalculated coverage/notices. This has not been done locally.
2. Android 15/16 enforcement with gesture and three-button navigation remains pending. An Android 16 AVD is now available, but explicit device selection was requested under the repository ARTEMIS rules and no reply arrived during this run; no second emulator was launched. Waydroid alone cannot certify Android 15+ enforcement.
3. Connectivity is now repaired; fresh course retrieval/search/detail/reviews and syllabus checks pass as described above. Instructor/chart/planner/OAuth-launch/backup-restore checks from October 10 remain historical passes.
4. Physical Samsung/Pixel checks and successful eligible-account Google OAuth callback remain pending. No fabricated callback tokens were used.

## Official guidance checked October 12

- [Google Play DEX optimization notices](https://developer.android.com/google/play/vitals/code-optimization): current app warning threshold is 10 MB uncompressed DEX. This release is approximately 4.42 MB, but Console evaluation remains authoritative. Older AGP versions can supply mapping metadata rather than the newer r8.json.
- [Enable app optimization](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization).
- [Compose system insets](https://developer.android.com/develop/ui/compose/system/insets).
