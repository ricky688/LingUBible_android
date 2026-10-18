# Best Practices and Security Alignment Update

## Area / priority

**Password-reset input, native/WebView messaging and external intent validation — High.** Reset links are bearer credentials. Forged/ambiguous links must not silently select another account, and a web frame must not receive account data or arbitrary native capabilities.

## Scope

New support form policy/repository, recovery/contact ViewModels, verification component, recovery/information screens; existing Auth/Profile/Settings callers, navigation, manifest and Koin. No dependency changes or backend security changes.

## Implementation

- Exact HTTPS origin/path/port checks; bounded Appwrite user ID and 64-character base64url reset token; reject duplicate/extra parameters, user info and fragments. Cold/warm dispatch shares validation.
- No reset secrets in typed navigation args, preference storage or authored logs; accepted Activity intent data/extras are removed. Passwords remain memory-only; recovery surface disables screenshots.
- App-owned verification HTML receives a randomized one-time handshake through a port sent only to the exact HTTPS target origin. No `addJavascriptInterface`, user fields or account data. Block main-frame navigation, file/content access, mixed content and non-allowlisted resource origins. Timeout/failure stops the request; CAPTCHA is never bypassed.
- Existing server function validates CAPTCHA/rate limits/token expiry/use. Native request and contact submits require explicit user action. Transport status alone is insufficient: require 2xx and the literal JSON Boolean `success: true`. Raw backend messages, reset tokens and passwords are not shown in error UI.
- Validate form inputs at repository boundaries as well as in UI. Coroutine cancellation is rethrown; repeated callbacks/taps cannot duplicate requests; stale validation results cannot replace new state. Server permissions and checks remain authoritative.

## Implementation diff

See [security-changes.diff](security-changes.diff). New security files are shown in full. Existing-file diffs are against the checked-out Git HEAD and therefore include previously authorized, uncommitted timetable/review/guide navigation context; those existing changes were preserved.

## Validation and limitations

Focused JVM tests verify malformed inputs, action/payload contracts, failure/duplicate/cancellation paths and no secret restoration. Full suite: 527 tests passing. Compile/release verification is in results.json. Device, live CAPTCHA score, email delivery, reset consumption and contact sending remain deferred. Android App Links association still depends on the website; native paste works without that association. No production credential, email, review or account was mutated in this work.
