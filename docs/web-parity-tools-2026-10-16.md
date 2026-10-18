# Remaining web tools — Android implementation, 2026-10-16

## Delivered source changes

| Area | Behaviour and entry point |
| --- | --- |
| Instructor directory | Tune icon beside search opens a Material 3 filter sheet. Combine actual departments, teaching terms, languages and former-staff inclusion. Sort name, department, teaching, grading, review count or GPA in either direction. The directory and term record queries paginate beyond their first server page. Missing numeric values remain last; zero is valid. Search also matches Chinese names and nicknames. Refresh retries term options and filters. The card now uses actual grading statistics. |
| Past papers | Course detail → Past Papers: year and instructor multi-selects, newest/oldest sorting, per-file selection, select all/clear and selected downloads. Filename instructors take precedence; otherwise match lecture instructors from the same course and academic term, including web summer aliases. Unknown metadata stays available; unknown dates sort last. Changing filters/sort clears selection. |
| Downloads | Single selection saves the original file; multiple selections save a ZIP. Both past papers and study materials share the existing repository download and safe sequential ZIP implementation. Android's file picker chooses the destination. Pending file selection/account survives picker recreation; account changes reject private downloads; failed or cancelled writes attempt to remove incomplete documents. Downloading does not alter server files. |
| Timetable history | Undo and redo buttons; ten entries per direction per term. Add/remove/clear/import use one shared selection update. New edits discard redo. History is held by the ViewModel; saved selections continue to use the existing preferences. Loading another term cancels/ignores stale reads. Read failures no longer cache partial catalogues. |
| Timetable sharing/import | More → Share link / Import link / Clear term. Shares use the web's HTTPS `/planner?term=...&sections=...` format. Import previews the target term, new/already-saved/missing sections and resulting conflicts; confirmation merges into that term and is one undo step. HTTPS Android entry points open import confirmation. No automatic import. Clear requires confirmation and supports undo. |
| Timetable PDF | Existing PNG preview now includes a PNG/PDF format choice. PDF reuses the full-calendar bitmap, orientation, gridline option, credits and theme preview, fits on one A4 page with margins, and saves via the native document picker. PNG remains available. |

No backend changes, preference migrations, new dependencies, version/signing changes, commits or publishing actions.

## Reference source

- Web instructor filters: `../src/components/features/reviews/AdvancedInstructorFilters.tsx`.
- Past-paper enrichment/filter/sort/selection: `../src/pages/CourseDetail.tsx`.
- Share format and limits: `../src/services/timetableShare.ts`; timetable history/import: `../src/pages/Timetable.tsx`.
- Native reuse: filter controls, PastPaperCard, material repository, MaterialZip, TimetableImageExport and existing preview/save flow.

## Validation

All **505 JVM tests pass** (zero failures/errors/skips). `testDebugUnitTest`, `assembleRelease` and `bundleRelease` complete successfully. Release guard PASS: 4,279,526-byte APK, 7,623,878-byte AAB, 4,941,268 uncompressed DEX bytes, 861 renamed app types present, matching R8 mapping embedded. Version remains 1.0.6/code7, min SDK 26 / target 36; the signing certificate matches the previously verified installed release. Artifacts and mapping: `../build/parity-tools-2026-10-16/`. APK SHA-256: `8cb5153cd118909ad339785e169137f21158113bd6662ae9ce3dd9346efac038`. Machine-readable results and copied test XML: `verification/2026-10-16/parity-tools/results.json`. Existing dependency Kotlin-metadata diagnostics remain in lint output; the unchanged toolchain completed the build. New focused JVM coverage includes instructor pagination/filter retry/cache, paper fallback/filter/sort, account-switch protection, timetable parser/history/import/stale reads and PDF placement.

[Intent security review](verification/2026-10-16/parity-tools/intent-security.md).

## Pending device checks

The user selected **Finish the source changes first** when Waydroid showed another application. This task does not install, launch, change settings or mutate saved selections on Waydroid. Existing signed-in device data is preserved. No screenshots or device test claims are made for these additions.

Next device pass: instructor filter sheet/query results and retry; course with several past papers → filters → PDF view → single save → ZIP save/cancel/recreation; timetable add/remove/undo/redo/term switch → share chooser → paste/import preview/cancel/confirm/undo → PNG and PDF save/view in both orientations; Android cold/warm links. Preserve the user's timetable; only undo/clean up test additions.

Live genuine review submission/publication remains pending by user choice. Samsung/Pixel hardware, large fonts/TalkBack, theme matrix and landscape device checks remain pending. Undo/redo history is not persisted across process death; saved timetable selections are persisted. Automatic verified App Links need the existing web domain's Digital Asset Links association; paste/import does not depend on that association.
