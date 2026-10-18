# Grouped stacked cards — 2026-10-17

## Request and design

Adjacent vertical cards should read as a list. Reuse existing getGroupedItemShape: exposed outer corners retain each component's radius, facing corners use 4 dp, and card gaps use 4 dp. First = rounded top/small bottom, middle = small all around, last = small top/rounded bottom; a single result remains rounded on every corner. Indices/counts are per displayed group, including filtered/truncated results and section boundaries.

## Source reach

- Existing rememberCardPressShape delegates to grouped shape with default single-card arguments; optional index/count on PopularCourseCard, PopularInstructorCard and ReviewCard. Existing pure spring press motion, elevations, colors/haptics and standalone callers remain.
- Course/instructor directories; Home featured/top lists and latest reviews; favourites (separate course/instructor/unavailable groups); course/instructor/all/my review lists; instructor teaching history.
- Timetable course-section picker (border and card share the same shape); selected calendar-day event cards; required/recommended reading groups; study files; honours programme results.
- Existing course-teaching and past-paper grouped rows now share getGroupedItemShape instead of duplicating its corner policy.
- Side-by-side dashboard grids and standalone section panels retain their existing shapes. No content, filtering/navigation/state, persistence, backend, signing or version changes.

## Validation

Four focused JVM shape tests cover single/two/multiple-card groups and pressed outer radius with fixed inner corners at density 2. The initial asymmetric drawing-outline test hit Android RectF not mocked on plain JVM; replaced it with direct CornerSize measurements without adding a dependency. Full suite: 546 tests passed, zero failures/errors/skips. Optimized release APK and AAB builds succeeded, with the existing nonfatal toolchain diagnostics; release guard passed (991 renamed app types, matching mapping, preserved navigation enum). See verification/2026-10-17/grouped-cards/ for guard JSON, test count and focused XML. Signed artifacts/mapping/build log are archived at build/grouped-cards-2026-10-17/.

User subsequently reserved Waydroid. Matching-certificate replacement installation succeeded, and installed APK hash matches the archived build. Verified first/middle/last and single-result shapes, history→course and course-card navigation, reviews, and matching timetable picker borders. Restored teaching-history filter, retained three courses/nine credits and returned to Grid. Original red/dark/OLED/device settings retained. See [device report/screenshots](verification/2026-10-17/grouped-cards/report.md). Other affected lists and full font/theme/resize/physical-device checks were not individually repeated. No data reset, commit, publishing, outbound action or instrumentation-test authoring.
