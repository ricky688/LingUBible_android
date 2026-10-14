# App-wide haptics — 2026-10-14

## Change

- Shared `hapticClick` and `hapticChange` callbacks cover 174 native activation sites across 39 files: navigation bars/drawer/top-bar actions, tabs, buttons/FABs, clickable cards, toggles, selectors and dropdown items.
- The existing haptics setting is read at activation time. Platform feedback comes from each control's current Compose window, including dialogs. Custom components delegate feedback to their inner native control once.
- Removed older manual Settings/connected-group feedback. A haptics-setting change is applied before its pulse: enabling previews once and disabling requests none.
- Tap token changed from TextHandleMove to VirtualKey. Waydroid reports `android:bool/config_enableHapticTextHandle=false`; the old token produced no app-attributed service requests. VirtualKey produced the expected requests. [Android platform guidance](https://developer.android.com/develop/ui/views/haptics/haptic-feedback).
- No new dependency, permission, backend change, stored-preference migration, signing change or version change. Preserved existing UI refinements and the user's removal of the Easter egg.

## Validation

- 408 JVM tests across 26 suites; zero failures, errors or skips. Five focused haptics tests cover the tap token, one enabled pulse, disabled suppression, live preference changes and feedback-type forwarding.
- `assembleReleaseValidation`, `assembleRelease`, `bundleRelease`: passed with existing optimized configuration. Existing toolchain metadata warnings remain nonfatal.
- ARTEMIS device observations grounded manual ADB taps on Waydroid Android 13, 480×1000, density180. No maintained instrumentation tests were authored.
- 20 app-attributed request-count checks passed. Bottom navigation, connected group, filled/outlined buttons, back action and course-popup open/Close were sampled. Enabled actions sent one request; disabled actions sent zero. Controls continued to navigate while disabled. Enabling the setting sent one preview request.
- Final optimized validation APK installed using `adb install -r` after certificate compatibility verification. Installed APK SHA-256 matches the final validation artifact; data was not cleared. No fatal AndroidRuntime entry for the app's current process was found.
- Restored original haptics **ON**. Preserved Chinese, Dark/OLED, Lingnan Red and selected CLC9022 (3 credits, CRN381). Samsung device was not modified.
- Release guard PASS: APK/AAB DEX content agrees, R8 mapping embedded matches, 651 renamed application types present. These are local checks, not Play Console coverage percentages.

## Evidence and artifacts

- [Request counts](haptic-requests.json), [first navigation request](navigation-first-request.json), [connected-group request](connected-request.json)
- [Restored setting screenshot](restored-haptics-on.png), [disabled setting](haptics-off.png), [course popup](course-popup-on.png)
- [Unit test results](unit-tests.json), [installed build](installed-build.json), [release guard](release-verification.json), [summary](results.json)
- Final APKs, AAB, R8 mapping/configuration and build logs: `build/app-haptics-2026-10-14/` (local ignored build output).

## Limits

Waydroid's virtual vibrator reports no physical capabilities and zero amplitude/scale. Android service requests were verified; tactile strength, feel and OEM behavior still need Samsung/Pixel hardware. Not every action, authenticated flow or external calendar/export operation was exercised; callback business logic is retained. System-setting suppression uses Android's native API and was not separately toggled during this run.
