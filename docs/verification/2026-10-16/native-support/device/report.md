# Waydroid native support verification — 2026-10-16

## Result

**PASS for the tested non-destructive flows**, on Waydroid Android 13, 480×1000 px, density 180, font scale 100%, existing selected red/dark/OLED appearance. The signed optimized release was installed by replacement; no data clear or sign-out.

| Check | Result |
| --- | --- |
| Optimized startup | Initial enum lookup crash found and fixed with targeted `@Keep`; release guard rejects old APK and passes fixed artifacts. Cold/warm startup works. |
| First-launch guide | Appeared and dismissed normally; no repeated prompt during later recovery launches. |
| Native Settings entries | All five entries visible; final Android changelog row reachable and opens directly. No appearance preferences changed. |
| FAQ | Two answers expanded/collapsed, bilingual content visible; last related actions reached. |
| Contact | Empty send blocked locally, six categories inspected; no message sent. Temporary short draft survives Privacy → Back and was cleared. Keyboard Back dismisses input before navigation. |
| Terms / Privacy | Native bilingual policy content, original June 5, 2025 date, final Android notes and footer actions reached; no browser handoff. |
| Android changelog | Version 1.0.6 (7), local Android changes, recent native additions and last entry verified. |
| Recovery links | Warm and cold existing HTTPS link format opens native recovery. Deliberately nonexistent token rejected safely. No password changed. |
| Recovery validation | Empty email blocked before verification/send. |
| CAPTCHA / recovery email | Exactly one user-authorized request via native UI. Observed VERIFYING → SENDING → accepted result. User confirmed receipt. No resend. |
| Restart / user data | Signed-in recipient confirmed unchanged after process restart. No automatic resend; current app process has no fatal exception. Timetable/GPA data were not edited. |

## Limits

A real password change and valid-reset-form completion were intentionally not performed. No live contact/support message, review or vote was submitted. Password rules and confirmation have JVM coverage; the valid reset-form/device submission still needs a dedicated test account or explicit permission. Physical devices, Android 15/16, other theme/font/orientation settings and TalkBack remain pending.

Recovery screenshots are intentionally absent because that screen uses `FLAG_SECURE`; the sanitized observed labels and request stages are in `recovery-request.json`. No account email, real token or password is included in the report. The temporary host recipient file was removed.

## Evidence

- `results.json` in the parent folder: installed/final artifact hashes, build and checks.
- `startup-regression.md`, `release-guard-regression-red.txt`, `release-guard-after-startup-fix.json`: actual optimized regression and guard.
- `recovery-request.json`: one request, stages, sanitized result and user receipt confirmation.
- `final-device-checks.json`: final configuration, sign-in and crash check.
- PNGs: FAQ, contact validation/draft, policy dates/notes, Android changelog and final Settings entries.

ARTEMIS observations plus grounded ADB actions were used. The initial autonomous ARTEMIS run failed at the startup crash and later hit a model timeout; it is not a passing checklist. After the fix and the user reserved Waydroid, the checks above were performed directly. No UI instrumentation code was authored from unverified interactions.
