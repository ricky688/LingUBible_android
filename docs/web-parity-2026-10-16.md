# Android web-feature additions — 2026-10-16

## Delivered

| Area | Android behaviour |
| --- | --- |
| Favourites | Uses the existing `favorites/user_favorites` table and the web's `user_id`, `type`, `item_id`, `created_at` fields. One shared account-scoped store drives hearts on Home, course/instructor lists and course details. Profile and drawer open My Favourites. Refresh, error states and removal of unavailable catalogue entries are supported. Account changes clear cached private state; failed saves never pretend to succeed. Writes preserve the user's intended state if another client already changed the record. |
| My Reviews | Own-review edit dialog for course comments, grade, workload/difficulty/usefulness, anonymity, language, and each existing instructor's grading/teaching/comments. Preserves instructor identities, sessions and unknown fields. Fetch/update/delete check the SDK account against document ownership; permissions remain enforced by Appwrite. Delete requires confirmation, cleans review votes in pages, and triggers the existing statistics function. Errors retain the form or confirmation. |
| Recovery | Native sign-in has Forgot Password. Opens `https://lingubible.com/forgot-password` in the browser, retaining the existing web CAPTCHA, email and token checks. This is a browser handoff, not a new native email/token implementation. Android Back returns to native auth. |
| Course filters | Multi-department, actual teaching-language codes (E/C/P/1–5), service-learning types, offered terms loaded from `terms` and paged teaching records, signed-in materials-only selection, sorting and direction. Missing numeric data stays last. Uses denormalized course metadata; no invented default English badge. Pending loads are cancelled when new search/filter input arrives. The sheet opens fully so Apply is reachable. |
| Resources | Added horizontally scrollable Reading List and Study Materials tabs. Parses required/recommended bibliography and safe HTTP(S)/DOI links, including library-proxy DOIs. Authenticated storage listing matches exact course prefixes and paginates. PDFs reuse the existing viewer; single downloads retain original bytes/type/name, multiple selections use a ZIP through Android's document picker. ZIP names cannot traverse directories or collide. Account changes close private panels; incomplete exports are cleaned up. |
| Information links | Guide, changelog, FAQ, contact, terms and privacy open their respective web paths. |

## Validation

- 456 JVM tests pass, zero failures/errors/skips. Added tests for favourites creation/failure/guest and account-switch protection, remote-state idempotence, review ownership and patch preservation, combined filters, unknown/zero GPA ordering, readings/DOI parsing, ZIP names/bytes/failure, and study-storage pagination/prefix matching.
- Signed optimized release APK and AAB pass the existing release guard; matching R8 mapping is embedded. Version remains 1.0.6/code7. Final artifact hash matches the installed Waydroid APK. No uninstall or data clear.
- ARTEMIS observation and grounded ADB interactions verified startup, real catalogue filtering (English 667; 2026–27 Term 1 368), required/recommended ACT2200 readings, guest resource/favourite/review screens, FAQ URL, recovery page launch and Back navigation. Final process has no fatal exceptions.
- Device testing caught and fixed an initial browser-launch crash caused by the localized non-Activity context; browser intents now include `FLAG_ACTIVITY_NEW_TASK`, matching the existing Profile pattern. The final recovery launch was repeated successfully.
- Evidence: `verification/2026-10-15/web-parity/`, including final filter/recovery screenshots, results.json, test XML and release-guard.json. Archived artifacts/mapping/build log: `../build/web-parity-2026-10-15/`.

## Remaining checks

A dedicated signed-in test account is needed to verify server permissions and full cross-client favourites sync, editing/deleting a disposable review, authenticated PDF/file-picker/ZIP exports, and sending/completing an actual reset email. No live reviews or emails were created/changed during guest verification. Physical Samsung/Pixel, other themes, large fonts and landscape remain unverified for these additions. No UI instrumentation tests were authored for account-only paths that could not be explored. Existing unrelated timetable and design work is preserved; nothing was committed or published.
