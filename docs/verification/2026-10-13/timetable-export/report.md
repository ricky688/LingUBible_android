# Course timetable exports — October 13, 2026

## Scope and changes

The user clarified that this request targets **選課排程 / course timetable**, not the school academic calendar.

- A tapped course block now retains the exact section + meeting. The existing popup highlights its weekday, time, meeting type, venue and selected term. Its new actions are **Add to calendar** and **Export meeting (.ics)**. Existing section details, conflicts, close and removal actions remain.
- Calendar insertion opens the native event editor with title, location, description, Hong Kong start/end times and a weekly rule bounded by the selected term. No calendar permission or direct calendar database writes. Users save the draft in their calendar app. Missing calendar handlers fall back to a single-meeting .ics share.
- The .ics action exports only the clicked weekday/time meeting, excluding other lectures/tutorials in that section. The existing whole-timetable export stays available. Both exports end recurrence at the term's final day in Hong Kong, converted to UTC.
- Added **匯出圖片 Image** to the weekly grid controls. The controls wrap at narrow widths. Image capture records the full non-lazy header and grid body inside the scroll containers, then combines them into a PNG; off-screen day columns and time rows are included. It follows the current Direct/Scroll layout and theme colors.
- PNG encoding/document copying runs off the main thread. Android's CreateDocument picker chooses the destination. The pending cache path survives activity recreation; picker cancellation and completed saves remove the temporary file. Failures show bilingual feedback and allow retry. No new dependency, backend API, preference migration or signing changes.

## Checks completed

- Before writing tests, ARTEMIS hierarchy and ADB exploration confirmed the target app, empty Planner, course browser and first catalog entries. Then Waydroid switched to another application's workflow; the user explicitly requested source work first. Device interactions stopped at that point.
- Debug Kotlin compilation succeeds. **394 unit tests pass**, zero failures/errors/skips. Nine new calendar tests cover clicked-meeting isolation, whole-timetable export preservation, weekday alignment, inclusive-minute/midnight ends, timezone independence, leap day, term bounds, invalid input and escaping. Three image document tests cover full byte copying, closed streams and provider/write failure handling while preserving the source.
- `git diff --check` passes.
- Optimized validation APK, signed release APK and release AAB all built. The initial combined run passed tests and built validation, then failed release compilation because a temporary 384 MB Metaspace cap was too low. Retrying release APK/AAB with the usual 2 GB heap and no temporary Metaspace cap exited 0. No repository/toolchain configuration changed.
- Local release guard PASS: identical APK/AAB DEX, matching embedded R8 mapping and 645 renamed application types present in DEX. This count does not represent Play Console coverage. Optimized Appwrite device smoke checks were not rerun during the device-use deferral.
- Latest artifacts and logs: `build/timetable-export-2026-10-13/`. Version remains 1.0.3/code 4. The next Play upload needs a higher code if 4 was already uploaded externally. None of these new artifacts were installed or published.

## Device checks pending

Do not treat the new UI or image output as visually verified yet. Waydroid remains in use by another workflow; no new APK was installed during that deferral.

1. Coordinate exclusive Waydroid access. Baseline observed timetable was empty. A first Add tap was issued as foreground switched away, so recheck whether test section 5403 (ABCT1D01) was selected and restore the original selection after testing.
2. Add a section with multiple meetings (e.g. ACT3300, CRN 6). Tap each block and verify its selected meeting, popup scrolling, Add to calendar and .ics action. Verify only that meeting is exported, weekly recurrence is bounded by term and editor times/location are correct. Test absent calendar-handler fallback.
3. Export PNG in Direct and Scroll layouts, including after scrolling vertically and horizontally. Open the saved file: verify all seven headers, all timetable hours through 21:30 and all selected meetings; compare colors/text and confirm there are no system bars, popup overlays or cropped final rows/columns.
4. Cancel the file picker, rotate while it is open, retry after a provider failure, and check large fonts/short windows. Check close/remove/back interactions and restore fixture selections.
5. Check native insertion behavior on Google Calendar and Samsung Calendar when hardware/apps are available; some calendar apps may handle recurrence extras differently. The single-meeting .ics export provides the alternative import path.

No runnable UI tests were authored for unverified new device flows. The 12 new tests are local unit tests. No upload or publication.

## References

- [Android calendar insertion intents](https://developer.android.com/identity/providers/calendar-provider).
- [Compose graphics-layer capture](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers#write-contents-of-a-composable-to-a-bitmap).
