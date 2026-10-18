# Native password recovery and information pages — 2026-10-16

## Delivered

- Sign in → Forgot password opens a native Material 3 form. School-email validation precedes a small embedded reCAPTCHA v3 verification surface; only a verification token crosses the origin-scoped message port. Email, reset links and passwords never enter the WebView.
- Reuses the existing `send-verification-email` backend function and its `sendPasswordReset`, `validatePasswordResetToken`, `completePasswordReset` actions. Existing CAPTCHA, server rate limits, one-hour expiry and one-time token consumption remain authoritative. No backend changes or alternate CAPTCHA-free recovery endpoint.
- The request result says **if an account exists**. Handles failed verification, network/service failure, rate limits, invalid/used/expired links, password requirements and mismatched confirmation. Duplicate taps cannot initiate parallel requests. New requests cancel pending validation and clear the old credential. A fresh process returns to the request screen; reset credentials/passwords are not persisted.
- Supports the exact existing HTTPS email link on `lingubible.com` and `www.lingubible.com`, with strict path, port, user ID, token, and duplicate-parameter validation. Cold and warm accepted intents use the same handler. The reset secret is removed from the Activity intent and never becomes a navigation argument. Merely opening a link validates it; password changes require the user's explicit submit. Paste-the-link is available when Android opens the domain in a browser. Automatic verified App Links still require the domain's Digital Asset Links association/user link settings; no domain configuration was changed.
- New passwords require the web form's 12–40 characters, uppercase/lowercase/digit/symbol and common-password rejection, plus confirmation. The server validates again. Sensitive recovery content uses `FLAG_SECURE`; prior window flag state is restored on exit. Password values use memory-only Compose state.
- Native FAQ, contact, terms, privacy and **Android-only changelog** open from Profile footer links and Appearance & Settings. FAQ has expandable native answers describing actual Android behavior. Legal text is bundled in both languages for offline reading with original platform policy dates and separate Android storage/backup notes. These copies are not a newly revised platform policy.
- Contact uses native fields/category selection and the existing CAPTCHA-protected `sendContactForm` action. Name/email/word-count validation matches the backend; failure retains form values, success clears them. Email compose and Android GitHub issues are optional explicit external actions. No support message was sent during implementation.
- Changelog lists this local client's changes and recent Android additions. Build version comes from `BuildConfig`; no web release history is imported and local entries do not claim Google Play publication. Settings' displayed version now uses the same actual build version.
- Selected Material 3 colours, native button shaping/haptics, pure spring transitions, shared scrolling fades/clearance, wrapping controls and bilingual content are preserved. No dependencies, auth preference migration, version/signing/toolchain changes, account changes or publishing.

## Source references

Web: `src/pages/auth/ForgotPassword.tsx`, `ResetPassword.tsx`, `src/services/api/auth.ts`, `src/components/auth/PasswordStrengthChecker.tsx`, `src/pages/legal/{FAQ,Contact,Terms,Privacy}.tsx`, locales, and `functions/send-verification-email/src/{handlers,routes,config,recaptcha,password-reset-template}.js` in the parent repository. Public site key was read from the deployed website's public JavaScript; no secret key is bundled.

Android: `SupportForms.kt`, `SupportRepository.kt`, `PasswordRecoveryViewModel.kt`, `ContactViewModel.kt`, `SecurityVerification.kt`, `PasswordRecoveryScreen.kt`, `InformationPages.kt`, `InformationScreen.kt`, `assets/information/*.json`, navigation, manifest, Koin and Settings/Profile/Auth callers.

Official references: [Android message bridges](https://developer.android.com/develop/ui/views/layout/webapps/native-api-access-jsbridge), [WebView bridge security](https://developer.android.com/privacy-and-security/risks/insecure-webview-native-bridges), [local HTML origin handling](https://developer.android.com/develop/ui/views/layout/webapps/load-local-content), [reCAPTCHA v3](https://developers.google.com/recaptcha/docs/v3), [Appwrite Functions](https://appwrite.io/docs/references/cloud/client-android-java/functions).

## Local validation

- **527 JVM tests passed**, zero failures/errors/skips. Focused coverage: strict/malicious links, password/email/contact validation, malformed/false server responses, actual SDK execution action/payload shape, duplicate sends, cancellation, failure/retry, invalid reset tokens, process-memory reset, legal asset decoding and Android-specific content.
- The verification message-handshake self-check passes (nonce/port/action and duplicate rejection); it uses a simulated Google callback, not live CAPTCHA. A missing/updating WebView fails safely. Policy links use normal back navigation so Contact form state can be restored when returning.
- Incremental core and UI builds passed. Signed optimized APK (4,430,619 bytes) and AAB (7,882,904 bytes) built successfully; release guard **PASS**, unchanged certificate `9c01dcc10feaa3abe2696bff3d03080a53f2b7fd8e8ca160d3e7c16423ce707e`, matching R8 mapping embedded. Both packaged policy assets match source bytes. Artifacts/mapping/build log: `build/native-support-2026-10-16/`. SHA-256 hashes and results: `verification/2026-10-16/native-support/results.json`.
- Existing nonfatal Kotlin metadata diagnostics are unchanged. No toolchain expansion was required.

## Device and live checks — 2026-10-16 follow-up

The user requested Waydroid testing and explicitly authorized **one recovery email to the account already signed in**; password changes and support-message submission remain unapproved.

- The first optimized installation crashed on launch because Navigation resolves `InformationPage` by its original name but R8 renamed it. Added a narrow `@Keep` annotation and a release guard checking the original enum name in mapping and DEX. The guard rejects the pre-fix build, then passes the corrected build. Evidence: `device/startup-regression.md` and the captured AndroidRuntime stack.
- Rebuilt with all 527 tests passing; installed by replacement with no data clear. Installed bytes and certificate match the corrected signed archive. Fixed APK SHA-256: `0f157262afdb45cb2c7e6660910c0e3bcf7a916e836d8deea02ef2f5215bb2ae`. No fatal exceptions in the corrected app process.
- ARTEMIS observations and grounded ADB actions verified startup, the first-launch guide and normal dismissal, preserved signed-in account, Settings information entries and opening the native FAQ.
- The user reserved Waydroid after foreground interference. Verified FAQ expansion/collapse and final actions; contact empty validation, categories, keyboard Back and draft restoration/cleanup; full native Terms/Privacy with original dates and final notes; Android-only changelog/version/last entry; final Settings-row reachability; warm/cold reset invalid-token handling; empty-email rejection and preserved sign-in after restart.
- Exactly **one** authorized recovery email was requested through the native UI. CAPTCHA and sending stages completed; the user confirmed receipt. No resend, password change or contact message. Current app process has no fatal exceptions. Detailed results and screenshots: `verification/2026-10-16/native-support/device/report.md`.
- Remaining: real password change/valid reset-form submission, live contact submission, physical devices and the full display/accessibility matrix.

### Earlier implementation status (before device testing was authorized)

At initial delivery, device checks were deferred by user choice. The follow-up above supersedes that deferral. No new UI instrumentation tests have been authored; live password changes and contact submission remain untested.

Next authorized device pass: guest Settings/Profile links; FAQ expansion; offline legal text and Android changelog; selected themes/large fonts/landscape/insets; native recovery invalid email; CAPTCHA success/failure/timeout/cancel/retry; email with a dedicated test account; cold/warm link and paste; used/expired link; password rules/mismatch; explicit successful reset and subsequent login; Back/keyboard/modal ordering; rotation/process recreation; contact validation and a user-approved genuine submission. Do not send test messages to live support or change the user's account password without explicit authorization.

A working Android System WebView and Google reCAPTCHA network access are needed for the embedded verification step; live verification and email delivery passed on this Waydroid installation. A public website key rotation requires updating the bundled public key. Legal policy updates require refreshing the bundled snapshots. Physical Samsung/Pixel and Android 15/16 checks remain pending.
