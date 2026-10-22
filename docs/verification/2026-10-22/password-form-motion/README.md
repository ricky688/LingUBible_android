# Change-password form transition — 2026-10-22

## Backend wiring

ProfileScreen Save calls AuthViewModel.updatePassword, which delegates to AppwriteAuthRepository.updatePassword. The repository awaits Appwrite Account.updatePassword(password = newPassword, oldPassword = oldPassword), returning success only after the SDK call completes. This updates Appwrite Authentication credentials; no local-only password write is used. A real credential change was not submitted in this check, so backend acceptance has not been verified end to end.

## UI change and validation

Replaced abrupt password-button/form visibility with AnimatedVisibility using the existing AppMotion Expressive spring tokens. The button fades out; the form expands/fades from the top, and Cancel reverses the motion. Existing pressed shapes, haptics, field validation and submission behavior remain intact. No custom duration/damping or dependency changes.

Final optimized release built in 2m19s and was installed on Android 13 Waydroid. Installed APK bytes verified. Inspected a bounded open/cancel recording and semantic layouts, confirming the current/new/confirm fields appear and disappear correctly. No password was entered, revealed, saved or changed; account remained signed in. The saved video is cropped to the password section and below to exclude account identity fields.

This cosmetic change did not rerun unit tests; the preceding 555-test run passed. Live password update, physical devices and disabled-motion settings were not tested. No AAB/publishing.

APK SHA-256: 0c9093671e0960cea9ff77f47ae3be6d39ba3bca713ccdffde0572cb82bdc406
