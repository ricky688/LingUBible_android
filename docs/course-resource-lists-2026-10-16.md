# Course teaching records and past-paper lists — 2026-10-16

## Delivered

- Course detail → **任教記錄** uses Material 3 `ListItem` rows grouped by teaching term. Chinese names lead when available, with English names and the lecture/tutorial/session below. A person badge and trailing arrow identify the teacher action. Rows wrap without fixed text heights. Term headers remove the previous repeated `2025-T2 (2025)` formatting.
- Tapping a teacher opens the existing instructor screen directly at **學生評價 Student Reviews**. Navigation passes the canonical name through the existing typed route. A Boolean in that destination's `SavedStateHandle` requests reviews; the route identity stays unchanged. A saved one-time focus flag and native list state prevent refresh/recreation from repeatedly jumping the list. Other instructor entry points still open normally. Unknown canonical names are not clickable.
- **歷屆試題** now groups Material 3 list rows by academic year. Each row shows term, lecturer names, PDF type/size and filename. Tapping the row uses the existing native PDF callback. A leading checkbox supports authenticated selection; the trailing 48 dp icon saves an individual file or requests sign-in for guests. Existing filters, sort, single/ZIP export, loading/error handling and account checks remain intact. Export disables checkboxes/download actions while busy.
- Both lists use theme-selected colors and expressive grouped shapes: 24 dp outer corners / 4 dp between rows; outer corners spring to 12 dp while pressed. Existing haptics, native ripple, semantic action labels, stable keys and scrolling fades are retained.
- Removed the unused legacy `PastPaperCard`; its only actual caller now uses the M3 row. Native preview and protected downloads continue through the same parent callbacks. No backend, database, preference, dependency, signing or version changes.

## Key files

- `ui/components/TeachingRecordsContent.kt`, `CourseResourceListItems.kt`, `PastPapersContent.kt`.
- `domain/model/CourseListLabels.kt`.
- `ui/screens/CourseDetailScreen.kt`, `InstructorDetailScreen.kt`; `core/navigation/NavGraph.kt`.

## Validation

- **510 JVM tests pass**, zero failures/errors/skips. Added checks for bilingual term/session labels and the review-header position with empty/duplicate/multiple course histories. Existing typed navigation/name, review browsing, PDF/download/filter and restoration coverage remains passing.
- Signed optimized APK/AAB build successfully; release guard PASS with matching R8 mapping and the unchanged release certificate. Version remains 1.0.6/code7. APK 4,295,908 bytes; AAB 7,631,013 bytes; 873 renamed app types. Artifacts/mapping/logs: `../build/course-resource-lists-2026-10-16/`. APK SHA-256: `b07c33e96a73e1d82996e2919118915efbe6062b5dfc6febfebd5d68f8e613f5`. Results/certificate/test XML: `verification/2026-10-16/course-resource-lists/`. Existing nonfatal toolchain metadata diagnostics remain unchanged.
- Source diff whitespace check passes. No new UI instrumentation tests were authored for unverified device paths.

## Deferred by user

The user explicitly selected **Keep device testing deferred**. No Waydroid install, launch, taps, screenshots or user-data mutations for this change. Device checks still required: long bilingual names/filenames and font scaling; term/year grouping; checkbox versus row versus download taps; teacher review entry and Back; refresh/recreation scroll behaviour; theme/keyboard/navigation insets. Live review publication remains pending; no reviews or votes were created or changed.
