# Remaining web parity — 2026-10-16

User authorized all five source-confirmed gaps: instructor details, calendar week/three-day views, timetable customization, recent searches, and course/instructor sharing/deep links.

Use existing Material 3 controls, pure stiffness springs, bilingual text, selected colors, inset/fade policy and repositories. Preserve existing account/preferences/timetable/GPA data. No backend changes, new dependencies, signing changes or publishing. Existing dirty work remains untouched; no mixed-work commits.

## Slices

1. Instructor: complete paginated teaching history with term/language/session/course filters; filtered grade distribution; retain direct reviews entry and course navigation.
2. Calendar correction: week and three-day rendering already existed in MonthlyCalendarOverview. Retained it, saved view/expanded state across recreation, added bilingual mode labels and 48 dp controls with wrapping, and fixed selected-day event fallback to today. No duplicate calendar was added.
3. Timetable: per-term title, course colors, visible fields/days/time bounds, PNG/PDF resolution and ICS dates. Persist new options separately from selections, validate input and keep export/live view consistent.
4. Recent search: bounded local course/instructor history, revisit/remove/clear, no automatic logging of passwords or private forms.
5. Sharing: native course/instructor share sheet and exact validated public HTTPS links, cold/warm handling; keep reset and timetable handlers intact.

## Validation

Explore real interactions with ARTEMIS before UI test authoring. Focused unit tests for data transformations, pagination, new preference isolation, links and export parameters; build each completed slice, then full optimized APK/AAB validation. Use Waydroid without data clear; preserve and restore its configuration and saved selections. Record unverified hardware/domain-association/live-write checks explicitly.

## Delivered interfaces

- Instructor detail now has History, Reviews and Grades sections. History filters combine term, teaching language, session and course; raw record IDs remain distinct. Server pagination no longer stops at 100. Co-taught names use a contains superset plus exact member matching, with a cached paginated fallback for servers that reject contains. Network errors do not trigger a full scan. Each eligible review contributes one course grade, including F/0 and unknown/N/A handling; legacy rows use teaching metadata for session/language filters. History rows open their native course; course-teacher links still enter Reviews directly. Grade charts expose an accessible count summary.
- Timetable More → Customize provides per-term title, colors (exact hex), visible fields/days, Monday-first order, automatic/manual HH:mm range, low/standard/high export resolution, and ICS date range. Separate appearance JSON preserves selected IDs, undo/redo and calculator/account preferences. Invalid settings are rejected with a visible footer error; corrupt appearance data falls back without deleting selections. Screen and PNG/PDF use one field/range policy. Theme colors reach the renderer; text contrast adapts to picked course colors. The preview renders small while saved files use the requested dimensions. Custom date windows omit meetings whose first occurrence is beyond the end, and empty calendar windows report a message.
- Course and instructor search bars now offer Recent Searches. Explicit search submission and opening a searched result save trimmed queries, deduplicated and limited to 20 per directory. Revisit, remove and clear are native. No search logging from private forms.
- Course/instructor top bars share public HTTPS URLs. Strict origin/path/value parsing handles cold/warm links and recognized web detail tabs, while preserving timetable and secret-reset handlers. Native routes gain defaulted initial-tab arguments; selected course tabs are saveable. Incoming extras are not forwarded.
- Updated the web source's existing public/.well-known/assetlinks.json with the verified native release certificate, retaining both existing TWA fingerprints. Native detail intent filter requests verification. This source file still needs web deployment on both hosts; Play App Signing fingerprint must be confirmed for Play-distributed builds. No website was published.
- Android-only local changelog updated. No dependency, backend API/schema, minimum SDK, preference migration, signing/version change or publishing.

## Artifacts and current validation

See verification/2026-10-16/remaining-parity/ for unit XML, release guard, installed hash, screenshots and export files. Signed APK/AAB/mapping/build log are archived at build/remaining-parity-2026-10-16/. Final results are in [the device report](verification/2026-10-16/remaining-parity/report.md) and results.json. Follow-up completed 2026-10-17: 542 JVM tests pass, optimized APK/AAB and release guard pass, matching installed APK rechecked, recent-search restart/removal and exact-hex selection verified. Timetable appearance and Month view restored. Domain association deployment and hardware/live checks remain explicitly pending in the report.
