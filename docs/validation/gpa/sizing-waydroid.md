# Card sizing: Waydroid check

Checked the updated debug preview on Waydroid Android 13 at 1080 × 2340, using ARTEMIS screenshots/hierarchy and ADB taps/swipes. No emulator configuration was changed.

- Summary cards: matching heights and aligned bilingual labels in the empty state.
- Course editor: credit and grade buttons have equal dimensions before and after selecting A (4.00). The year GPA updates to 4.000.
- Academic-year selector spans the card width; add-course, add-term and add-year actions can be scrolled into view.
- Statistics: programme cards with one- and two-line English names have matching visible heights; names are readable.
- Switching back to Calculator retains the entered grade. The chart displays a 4.000 point and both exact term/cumulative values.

Screenshots: [summary](sizing-calculator.jpg), [editor](sizing-editor.jpg), [statistics](sizing-statistics.jpg), [chart](sizing-chart.jpg).

This is a portrait spot check, not a complete landscape/font-scale/accessibility test. Multi-term chart behavior and all filter combinations remain unverified on-device.

The isolated package `com.lingubible.app.gpapreview` remains installed and open for inspection. The user-supplied release package and its data were not modified.

## Theme and tab follow-up

Replaced remaining fixed editor/chart colours with MaterialTheme colour roles supplied by the existing Settings-driven theme. Chart legend now uses solid/dashed labels rather than fixed colour names. Calculator/Statistics use equal-width Material 3 secondary tabs, inset 16 dp to align with the content cards.

Debug preview rebuilt, installed and inspected on Waydroid with the existing red/OLED appearance. [Updated tabs](themed-tabs.jpg). Other Settings palette choices were not exercised in this check.
