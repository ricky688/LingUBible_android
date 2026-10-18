# Remaining parity: local verification

Completed 2026-10-17. Source work and build completed 2026-10-16; this follow-up finished final-release device checks. This report is limited to the five requested parity areas.

## Build and installation

- 542 JVM tests, zero failures/errors/skips; checked against Gradle XML. Optimized release APK and AAB build successfully. Existing toolchain warnings remain nonfatal.
- Release guard passes: APK 4,463,525 bytes, AAB 7,960,736 bytes, uncompressed DEX 5,189,216 bytes; 987 renamed application types, matching mapping, required InformationPage navigation enum retained. These are local checks, not Play Console percentages.
- Signed replacement installation retained data. Installed APK hash was rechecked on October 17 and matches final5: `e3d17a68227d87e46620c48be29517a1df85aeb7280a330389179e0c835d2e4a`.
- APK/AAB/mapping/build log: `../../../../build/remaining-parity-2026-10-16/`. Machine evidence: [results.json](results.json), [unit-tests.json](unit-tests.json), [release-guard.json](release-guard.json), [installed-release.json](installed-release.json).

## Device results

Waydroid Android 13, 480×1000 pixels, density 180, font scale 1.0, existing red/dark/OLED appearance. User reserved Waydroid for the October 17 follow-up after it was found running VibeFinance. No resolution, font or navigation setting was changed.

| Area | Verified behavior | Evidence |
| --- | --- | --- |
| Instructor | Complete history and grade sections render. Earlier intermediate checks: 14 records → 1 for 2026-T2, reset → 14; grade filter empty/reset states. Final5 shows three submissions (A: 1, N/A: 2), mean 4.00. | [History, final5](instructor-history-final5.png), [Grades, final5](instructor-grades-warm-link-final5.png), [Filter, build3](instructor-filtered-build3.png) |
| Teacher navigation | CLC9022 teaching record opens Prof. CHUNG Yu Ho directly on Reviews. | [Course history](course-teaching-link-final5.png), [Teacher reviews](teacher-reviews-navigation-final5.png) |
| Public links | Explicit native VIEW intents: warm course `/teaching`, cold course `/grades`, warm instructor `/grades` open the intended native tabs. Earlier base instructor link also worked. This does not verify browser association. | [Cold course Grades](course-grades-cold-link-final5.png) |
| Calendar | Existing Week and Three-day layouts render with bilingual controls; restored Month. | [Week](calendar-week-final5.png), [Three days](calendar-three-day-final5.png) |
| Recent searches | ACT2200 revisits the query and opens the result; entry survives process restart; its removal shows the empty state. Initial history contained only our earlier test entry. Only ACT2200 was removed. | [History](recent-searches-final5.png), [After restart](recent-searches-restart-final5.png), [Opened course](course-details-final5.png) |
| Exact colors | Entered #FFFF00, applied it, reopened settings and confirmed exact value; the yellow block uses dark readable text. Reset original appearance and retained all three courses/nine-credit summary. | [Picker](hex-color-picker-final5.png), [Grid](timetable-exact-hex-final5.png) |
| Timetable export | Earlier pre-final check changed title to Waydroid test, fields/days/resolution, restarted and confirmed persistence. Saved PNG and PDF through Android document picker; PNG 3600×2100, PDF one A4 landscape page. Restored defaults; only own two Downloads artifacts removed after pulling. | [PNG](timetable-export-high.png), [PDF](timetable-export-high.pdf), [PDF details](pdf-info.txt), [Preview, pre-final](timetable-preview-pre-final.png) |
| Sharing | Native external share target opened; cancelled without recipient or send. | Earlier observation; delivery not tested. |

Screenshots with `intermediate`, `build3` or `pre-final` names are from those earlier builds. `final5` screenshots use the installed APK hash above. Earlier validated history/filter/calendar/renderer code is unchanged in final5; final5 also includes exact-hex and accessibility/export guard fixes.

The selected old catalogue ABCT course codes have no credit metadata, so the optional per-block field explicitly says Credits unknown; the pre-existing timetable summary estimates nine. No credit values were invented.

## Test-driver incidents

Two locator/timing problems were investigated and resolved without app changes: an ACT2200 text match initially tapped the search field instead of the result; a scroll ran before the sheet finished opening. A cold-link check initially waited for a label the chart does not display; ARTEMIS confirmed the correct chart, and the corrected check passed. These were not API failures.

ARTEMIS observations and guarded ADB interactions grounded the device checks. Autonomous Flash trace `82490180-f616-4325-908d-6c70c8cde3e3` hit diagnosed provider 429 quota and was stopped. No new device instrumentation code was authored. JVM tests cover pagination, matching/filtering, link validation, search persistence/isolation, appearance validation/isolation and export boundaries.

## Data preservation and remaining checks

Retained account/session, GPA, timetable CRNs 5403/5404/5405, undo/redo and existing preferences. No uninstall, clear-data, sign-out, recovery email, review publication, calendar insertion or outbound message was performed. Appearance defaults and Month view restored; only our ACT2200 history entry removed. No website/store publishing or mixed-work commit.

Pending: deploy web assetlinks source on both hosts and check Play App Signing certificate; browser automatic association; physical Samsung/Pixel and Android 15/16/font/theme/navigation/cutout/performance matrix; instructor search-history UI round-trip; calendar state restoration through actual Activity recreation; on-device invalid-settings/empty-ICS messaging; actual share/calendar delivery and review publication. Unit tests cover the relevant pure/state logic, but do not replace these device/live checks.
