# Button press shapes and My reviews — October 12

## Changes

- Connected/scrollable groups now use native Material 3 `ToggleButton` pressed/checked shape animation and `ButtonGroupDefaults` leading/middle/trailing shapes. Keep the existing selected palette, optional checkmarks, sizing/alignment, scroll fades and callbacks. Release restores the selected shape; buttons do not shrink as a whole.
- Regular filled, tonal, elevated, outlined and text buttons opt into Compose's `ButtonDefaults.shapes` overload (57 actual button call sites). Existing custom resting shapes are retained. Shared split buttons use native toggle shapes. Custom review FAB/inline action, syllabus, home CTA and category grid no longer apply press scaling; purposeful FAB expansion/translation and favorite selection motion remain.
- Shared spring helpers no longer override damping, matching AGENTS.md. Material 3 remains 1.5.0-alpha01; no dependency/toolchain upgrade.
- Profile's My reviews action previously navigated to `Screen.Reviews`, which always fetched the latest 30 public reviews. It now navigates to dedicated `Screen.MyReviews`, with its own title and account-specific ViewModel state.
- `getReviewsForUser` filters on `user_id` in every server query, orders newest first and follows cursor pages of 100, so older reviews are not lost outside the recent public feed. Public latest reviews remain unchanged.
- Guests see a sign-in action. Signed-in account changes cancel/discard earlier requests and clear account-specific results; only matching author IDs are displayed. Errors show Retry and never fall back to the public feed. Collection is lifecycle-aware. No backend schema, preference or data migration.

## Verification

Before authoring regression tests, ARTEMIS live hierarchy/ADB exploration reproduced Profile → My reviews opening Latest reviews as a guest. The optimized updated APK was then explored on the same Waydroid API 33 device.

- Held-press screenshots verify both native connected selection and ordinary buttons morph their corners. Their rendered bounds remain identical between resting and pressed screenshots (`press-bounds.json`). Original red/dark/OLED selection was retained.
- Profile → My reviews now displays the dedicated title and guest sign-in prompt. Sign-in opens authentication; Back returns to My reviews with no public reviews. Rechecked after the final install (`my-reviews-final.png`).
- Home → Latest reviews → View more still opens the public review feed (`public-reviews.png`); logs show 30 public reviews loaded. Home course/stat data also refreshes successfully through the repaired Waydroid relay.
- **382 unit tests pass, zero failures/errors/skips.** Six new ViewModel tests cover guest access, author filtering, logout with a pending response, account switching with a late response, failure/retry isolation and the public feed. Two repository tests verify account filtering on every page, loading 101 records and rejecting blank IDs.
- Optimized release and validation Appwrite wire-format checks both PASS. Release guard PASS: matching APK/AAB DEX, matching embedded mapping, 633 renamed application types in DEX; local count is not Play coverage.
- Offline test/optimized validation/signed release APK/AAB build exited 0; `git diff --check` passes. Existing Kotlin metadata diagnostics remain non-fatal. Final validation installed with `adb install -r` at 23:13:29; first installation remains October 8 17:23:39. No uninstall or app-data clearing.

Screenshots: `connected-rest.png`, `connected-pressed.png`, `button-rest.png`, `button-pressed.png`, `my-reviews-signed-out.png`, `my-reviews-final.png`, `public-reviews.png`. Results: `build-results.json`, `release-verification.json`, `installed-final.txt`, `device-log.txt`, both optimized Appwrite smoke logs.

## Artifacts / limits

Current APKs, signed bundle, separate release/validation R8 outputs, build logs, test XML and verification evidence are archived under `build/buttons-my-reviews-2026-10-12/`; normal `app/build/outputs/` paths match. These include the new UI/reviews changes and supersede the earlier Play-review archive for further distribution. Version remains the locally unreleased 1.0.3/code 4. No upload or publication was performed; if code 4 has since been uploaded externally, the next Play upload needs a higher code.

Waydroid is signed out. Real-account review viewing has not been completed on device; account filtering, pagination, refresh/error and account-change behavior are verified by the regression tests. No credentials were requested or account created. Android 15/16 navigation, physical Samsung/Pixel and full eligible-account OAuth checks remain pending from the earlier plan.

## References

Native API availability was confirmed against the installed 1.5.0-alpha01 artifact before implementation:

- [Compose Button shapes](https://developer.android.com/reference/kotlin/androidx/compose/material3/ButtonDefaults).
- [Compose connected button defaults](https://developer.android.com/reference/kotlin/androidx/compose/material3/ButtonGroupDefaults).
