# Material 3 dividers — 2026-10-21

## Implementation

Existing separators now use Material 3 HorizontalDivider defaults: 1 dp with the active color scheme's outlineVariant. Removed additional opacity reductions and sub-dp overrides from profile, appearance settings, review footers, course details, GPA terms/forms, calendar event details, term menus and navigation drawer groups. Existing divider padding remains; the two profile navigation-row separators are inset to align with ListItem text (56 dp start, 16 dp end).

The timetable course popup's divider moved from beneath the title to above the pinned calendar/export/remove/close actions. No additional separators were added between Discover cards or connected buttons. No custom component, dependencies, animation, backend, preference, version or signing changes.

## Validation

- Optimized release APK built successfully in 2m22s; existing Kotlin metadata/R8 warnings remained nonfatal.
- Installed as an update on Android 13 Waydroid with the existing release certificate. Installed APK bytes match the generated artifact (see results.json).
- Inspected grouped profile rows in light and dark themes; restored the original Dark selection. Profile screenshots are cropped to the preferences group, excluding account information.
- Inspected appearance settings separators, the timetable popup action divider and credits row, and review-card footers. No export, remove, calendar, vote, account or reset action was invoked.
- Timetable retained its three courses / nine-credit summary. Its selected course's existing unavailable-credit fallback was unchanged.
- No new logic or executable tests were introduced for this cosmetic change. The unit suite was not rerun. Prior scrollbar/motion changes were preserved.
- Physical-device, large-font, landscape, dynamic-color and every individual separator screen were not retested. No AAB or publishing.

APK: app/build/outputs/apk/release/app-release.apk
SHA-256: ea38138ea971f19b9669064d70d67fe988c60acfb40bc45375fdaf4197c5a61f

Reference: https://developer.android.com/develop/ui/compose/components/divider
