# Grouped-card Waydroid verification — 2026-10-17

User reserved Waydroid for LingUBible after the other app interrupted the earlier check. Installed the archived optimized release with a verified matching certificate, using replacement install without clearing data. Pulled installed APK and confirmed SHA256 `84b8d45100a579f1d06dfe55878dace8e092eaab9f307636b6e61192ec99d684`.

Environment: Android 13 Waydroid, 480×1000 pixels, density180, font1.0, original red/dark/OLED appearance. No display/theme/font/navigation changes. ARTEMIS observed real screens; guarded ADB actions completed navigation and captures. No new instrumentation tests authored.

## Checks

- [History top](instructor-history-top.png): first row has rounded top and 4dp bottom; subsequent rows have 4dp facing corners/gaps.
- [History bottom](instructor-history-bottom.png): last BUS2205 record remains reachable, with rounded bottom and small top corners; fade clears at the bottom.
- [Single result](instructor-history-single.png): filter2026-T2 yields1/14 and all four corners rounded. Reset restored14/14. A CLC9022 record still navigates to its course.
- [Reviews](reviews-top.png): grouped first/middle shapes and readable content retained.
- [Course directory](courses-top.png): first/middle grouped shapes; ACT1110 tap opens Understanding and Analyzing Company Report. Query stayed blank, so no test search-history entry was added.
- [Timetable picker](timetable-picker.png): grouped card and selected border agree. No course toggle performed. Returned to Grid and confirmed same three courses/nine-credit summary.

An initial navigation driver fired Back too quickly during transitions and then could not find Home Search. ARTEMIS identified the current course screen; subsequent guarded Back-button interactions waited between transitions. This required no app change.

## Build and limits

546 JVM tests passed, zero failures/errors/skips, including four grouped-corner cases. Optimized APK/AAB and release guard pass. [Guard](release-guard.json), [test count](unit-tests.json), [installation](installed-release.json), [device results](device-status.json). Artifacts/mapping/log at `../../../../build/grouped-cards-2026-10-17/`.

Account/GPA/preferences were not reset or edited. History filter restored, timetable selections retained, no outbound actions. Other affected Home/favourites/resource/calendar/honours screens were not individually rechecked on this installed build; full theme/font/resize/physical Samsung-Pixel checks and motion/haptic measurements remain pending.
