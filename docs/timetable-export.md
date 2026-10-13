# Course timetable exports

## Export one class meeting

1. Open **學業工具 → 選課排程 → 課表** and tap a class block.
2. The popup shows the course credits from the course catalog and highlights the selected weekday, time, location, meeting type and term. If credit metadata is unavailable, it says so instead of inventing a value.
3. Choose **加至日曆 Add to calendar** to open the installed calendar app's event editor. Review the event and save it there.
4. Alternatively, **匯出此時段 Export meeting (.ics)** shares a calendar file for that meeting only. If no calendar app handles insertion, LingUBible falls back to this file export.

A selected meeting repeats weekly through the selected term. Other lecture/tutorial meetings of the same section are excluded. Times use Asia/Hong_Kong and preserve the CSV's inclusive last minute (18:29 becomes an 18:30 calendar end).

The existing **匯出日曆** action still exports every meeting of all selected sections.

## Export the full timetable as a PNG

With at least one selected class, choose **匯出圖片 Image** beside the weekly grid controls, then select a destination in Android's file picker. The image contains the seven day headers and full timetable body, including rows and columns outside the current scroll viewport. It follows the current Direct/Scroll layout and selected colors. Scroll mode gives wider day columns for sharing.

No storage or calendar permissions are requested. Saving uses Android's document picker; calendar insertion opens an editor for confirmation. Cancelling the picker removes the temporary image. Failures show an error and allow retry.

## Timetable edge treatment

The weekly grid now scrolls behind bottom navigation with the shared spring-animated frosted fade at the visible edge. Bottom clearance is inside the scrollable body, so final rows remain reachable. The exported PNG records the raw grid before the viewport fade and excludes navigation spacing.

## Validation status

Device verification for the new actions and image capture is pending: Waydroid is currently used by another workflow, and the user requested source work first. No new build has been installed during that deferral. See `docs/verification/2026-10-13/timetable-export/report.md` for export validation and `docs/verification/2026-10-13/timetable-credits-fade/report.md` for the credits/fade follow-up.
