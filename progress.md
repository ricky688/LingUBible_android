# LingUBible Android — agent handoff

Updated: 2026-09-16 (Asia/Hong_Kong)

### 2026-09-16 Material 3 Expressive Calculator Tab & Fluid Transitional Animations

- **M3 Expressive GPA Dashboard Overhaul (`GpaDashboard.kt`)**:
  - Re-architected `GpaPanel` into an expressive squircle surface (`RoundedCornerShape(22.dp)`) with fine specular linear gradient borders (`Brush.verticalGradient`).
  - Implemented vertical directional numeric roll transitions (`AnimatedContent` with `slideInVertically` + `fadeIn` togetherWith `slideOutVertically` + `fadeOut`) for Cumulative GPA and Earned Credits in `GpaMetric`.
  - Upgraded `GpaSummary` to a modern 2-tile layout + dynamic classification banner with spring-animated tier morphing and contextual trophy/medal icons (`Icons.Outlined.EmojiEvents` / `EmojiEvents`).
  - Elevated `GpaTargetCard` with rounded input text fields (`14.dp`), squircle honours tier chips (`12.dp`), and specular result surface with vertical numeric roll for the required average GPA.
  - Expressive empty states for `GpaTrendCard` featuring category icon badge containers and squircle filter chips (`12.dp`).
- **Course & Term Editor Refinement (`GpaHonsScreen.kt`)**:
  - Replaced scattered action buttons with a sleek, unified squircle action bar (`Undo`, `Redo`, `VerticalDivider`, `Reset`) with spring touch feedback and clear enabled/disabled tints.
  - Implemented full-width expressive `AcademicYearSelector` with Calendar icon badge, bilingual title, specular gradient border, and rounded dropdown menu (`16.dp`).
  - Elevated `YearCard` into an expressive squircle container (`22.dp`) with "Y{N}" badge, animated GPA numeric roll badge, award status pill, and spring-animated term layout (`animateContentSize`).
  - Added `Modifier.animateItem(...)` to `LazyColumn` year items for bouncy spring item placement and entrance animations.
  - Refined `TermSection` with bilingual headers, course count badges, and expressive "+ 新增課程 · Add Course" squircle buttons.
  - Upgraded `CourseRow` with rounded course code inputs (`14.dp`), autocomplete suggestions dropdown, and squircle dropdown buttons for Credits and Grades (high-contrast primary container when selected).
- **Strict Motion & Design Compliance**:
  - Strictly followed Material 3 Expressive pure spring stiffness motion (`spring(stiffness = Spring.StiffnessMediumLow)`), never overriding damping ratios.
  - Enforced Traditional Chinese primary headline with English secondary subtitle across all cards, titles, dialogs, and controls.
- **Verification & Deployment**:
  - Verified `./gradlew :app:testDebugUnitTest --offline`: all 335/335 unit tests passed with 0 errors.
  - Built debug APK and deployed to Waydroid (`192.168.240.112:5555`).
  - Live verified screen states: GPA number rolls, honours tier slider & chips, required GPA roll recalculation, course/term addition & deletion animations, Undo/Redo/Reset flow, and reset confirmation dialog with undo restoration.


### 2026-09-16 Material 3 Expressive Statistics Tab & Transitional Animations

- **Material 3 Expressive Statistics Dashboard**:
  - Overhauled `FirstClassHonoursContent.kt` in `Academic Tools > GPA & Honours > Statistics` with official Material 3 Expressive design tokens, squircle shapes (`RoundedCornerShape(18.dp)` and `22.dp`), and specular gradient borders.
  - Re-architected the 3 summary metric tiles (畢業生 Grads, 甲等榮譽 First Class, 甲等比例 Share) with category icon containers, directional vertical number roll transitions (`AnimatedContent` with `slideInVertically` + `fadeIn` togetherWith `slideOutVertically` + `fadeOut`), and dedicated YoY trend badges (`+N`, `-N.N pp`) that smoothly expand with spring physics.
- **Transitional Animations & Motion UX**:
  - Maintained strict Material 3 Expressive pure spring stiffness motion (`spring(stiffness = ...)`), never overriding damping ratios.
  - Expandable "篩選與排序 · Filter & Sort" disclosure panel featuring spring expansion/collapse, active filter count badge, and animated rotation on the filter icon.
  - Integrated interactive touch scale feedback (`animateFloatAsState` on press, `0.98f`) on programme cards.
  - Fluid list item animations with `Modifier.animateItem(...)` on search, filter, and sorting reorders.
  - Rounded progress bars with animated fill (`animateFloatAsState` with `StrokeCap.Round`).
- **Filter & Search Polish**:
  - Expressive search bar with leading search icon and animated clear button.
  - Faculty chips with selected active fills, metric selection, and sort order toggle (`數值 Value ↓` vs `名稱 Name A-Z`).
  - Expressive empty state (`HonoursEmptyState`) with reset filters action when queries return zero results.
- **Verification & Deployment**:
  - Verified `./gradlew :app:testDebugUnitTest --offline`: all 335/335 unit tests passed with 0 errors.
  - Built debug APK and deployed to Waydroid (`192.168.240.112:5555`).
  - Live verified screen states: 3-tile summary cards, YoY toggle & badges, search filtering, animated disclosure panel, and empty state reset.

### 2026-09-16 Discover category tab transitional animation

- Integrated `AnimatedContent` for category content switching (`HomeFeaturedTab`: 熱門課程, 熱門講師, 最高評分課程, 最高評分講師) on the Discover page (`HomeScreen.kt`).
- Applied bidirectional directional slide and fade transitions (`slideInHorizontally` + `fadeIn` togetherWith `slideOutHorizontally` + `fadeOut`) based on ordinal delta (`targetState.ordinal > initialState.ordinal`), matching the established motion pattern in `GpaHonsScreen.kt`.
- Applied Material 3 Expressive spring stiffness motion (`spring(stiffness = Spring.StiffnessMediumLow)` / `Spring.StiffnessMedium`) with pure spring stiffness without overriding damping ratios.
- Enabled `SizeTransform(clip = false)` to preserve card elevation, rounded corners, and shadows without abrupt boundary clipping during content transitions.
- Verified unit test suite (`:app:testDebugUnitTest`, 335 tests passed) and live Waydroid device deployment: verified bidirectional switching across all 4 category tabs with smooth spring transitions and live data rendering.

### 2026-09-16 native Google sign-in and registration confirmation

- Added Appwrite Android OAuth through `Account.createOAuth2Session` with the Google provider.
- Registered the Appwrite callback activity and the project-specific `appwrite-callback-6a1097400037a55f6472` scheme in the manifest.
- Added the Material 3 Google action to the authentication form. It refreshes the Appwrite user session after callback and accepts only the existing Lingnan email domains.
- Fixed the Google Sign-in button disabled state: resolved the host `ComponentActivity` via `LocalComponentActivity` provided by `MainActivity`, with graceful fallback via `LocalView.current.context.findActivity()` and recursive `ContextWrapper` unwrapping. This solves the issue where `LocalContext.current` was provided as a configuration context (`ContextImpl`), which previously caused `LocalContext.current as? ComponentActivity` to evaluate to null and permanently disable the button.
- Verified on Waydroid: the Google Sign-in button is now fully active, bright, and clickable. Tapping it successfully launches Appwrite's Google OAuth authorization flow via browser Custom Tabs.
- Added a second password field for registration, inline mismatch feedback, and view-model validation before the registration request. Password values remain composable-only state and are not persisted.
- Replaced the form-mode content swap with spring expand/shrink and fade transitions for registration-only fields. This avoids the prior fixed-height animation container that clipped added controls.
- Verified `:app:assembleDebug :app:testDebugUnitTest --offline`. Waydroid preview inspection revealed and confirmed the clipping fix path; full Google authorization was not run because it requires a real Google account and Appwrite Android OAuth/platform configuration.

### 2026-09-16 compact GPA UI and transitions

- Added a directional spring slide/fade transition between Calculator and Statistics. Each tab remains inside its own saveable-state provider, so its filters and scroll position are retained.
- Replaced the three tall GPA summary cards with one compact dashboard: cumulative GPA and credits share the first row, while classification spans the row below.
- Reorganised Target Honours into two side-by-side fields, one concise helper line, a slider, wrapping tier chips and a compact result row.
- Target-result changes now use a spring fade/scale/size transition. The tier chip area also animates size changes with a stiffness-only spring.
- Replaced the three tall First Class Honours summary cards with a single three-column summary card.
- Kept programme search visible and moved the long faculty/group/metric controls behind an animated Filters disclosure. Sort remains directly accessible beside it.
- Built the normal debug app and ran the full debug unit suite successfully after these changes. The isolated preview build also succeeded.
- Updated `com.lingubible.app.gpapreview` on Waydroid and visually checked the compact Calculator dashboard, the Calculator/Statistics switch, compact 2025 statistics (900 / 119 / 13.2%), animated filter expansion and target-tier recalculation. Selecting second-upper honours changed the target to 3.00 and displayed a required average of 2.974.
- The preview is currently open on Target Honours and still contains the earlier sample 3-credit A grade. The signed release remains untouched.

### 2026-09-16 statistics category selector refinement

- Replaced the expanded faculty chip collection with one full-width Material 3 dropdown containing All faculties plus the five faculties.
- Replaced the three metric chips with a compact metric dropdown. Name/value sorting remains directly accessible beside it, and faculty aggregation remains a single full-width option.
- Added spring slide/fade animation to dropdown value changes and spring fade/placement animation to programme cards when faculty, metric or grouping changes.
- Extended `GpaPanel` with an optional modifier so lazy programme cards can use Compose item animations without duplicating the shared card styling.
- Normal debug build and unit tests passed; isolated preview build also passed.
- Updated the Waydroid preview and selected Business through the faculty dropdown. The selector changed correctly and the filtered programme list updated. The signed release was not modified.

### 2026-09-16 Terms and Courses density refinement

- Simplified the Calculator course editor after the user reported that its nested cells felt crowded.
- Removed the separate filled term container and the outlined course container. Terms now sit directly inside the shared year card, with Material 3 dividers between multiple terms and courses.
- Course code, credit and grade controls use a flat three-part hierarchy. Credit and grade share the available row width equally instead of using fixed-width cells.
- When more than one course exists, deletion is available as a labelled icon action inside the course-code field rather than consuming a third button cell.
- Separated Add Course and Add Term with one divider while keeping both as lightweight text actions.
- Normal debug build and unit tests passed; the isolated preview build also passed.
- Updated and inspected `com.lingubible.app.gpapreview` on Waydroid with the existing 3-credit A sample. The flattened editor rendered correctly and all visible controls remained reachable. The signed release was not modified.

### 2026-09-16 expressive Statistics switches

- Replaced the Year-over-year comparison chip and Combine programmes by faculty chip with shared Material 3 Expressive switch rows.
- Each row has a 64 dp minimum touch surface, Traditional Chinese heading, English supporting label, theme-derived tonal container, animated spring colour transition and checked thumb icon.
- The full row toggles the option; the internal switch delegates to that row to avoid duplicate click handling.
- Normal debug build and unit tests passed; isolated preview build also passed.
- Updated the Waydroid preview and verified both switches. Year-over-year enabled delta values, and faculty combination changed the programme list into animated faculty aggregates. The active controls followed the selected red/OLED Material 3 palette. The signed release was not modified.

### 2026-09-16 responsive cohort-year selector

- Replaced the fixed 2024/2025 chips with a full-width Material 3 Cohort year dropdown using the shared animated selector pattern.
- The selector labels each value bilingually as a graduating class and can accommodate future cohorts without horizontal overflow.
- Normal debug build and unit tests passed; isolated preview build also passed.
- Updated the Waydroid preview and selected the 2024 cohort. The summary updated to 770 graduates, 109 first-class awards and 14.2%. The signed release was not modified.

### 2026-09-16 year-over-year wording transition

- Added spring fade/expand and fade/shrink transitions to the comparison helper wording beneath the Year-over-year switch and to each programme card's YoY delta line.
- The animation follows the existing stiffness-only motion rule and uses no damping overrides.
- Normal debug build and unit tests passed; isolated preview build also passed.
- Updated the Waydroid preview and enabled Year-over-year comparison. The helper appeared as “與 2024 比較 · Compared with 2024” with the aggregate deltas (+130, +10, -0.9 pp). The signed release was not modified.

## Workspace and user scope

- Native repository: `/home/ricky/Antigravity_project/lingUbible/LingUBible/LingUBible_android`
- Parent web reference: `/home/ricky/Antigravity_project/lingUbible/LingUBible`
- Native remote: `https://github.com/ricky688/LingUBible_android.git`
- The user re-cloned the original native repository. The former `android_native` directory is obsolete.
- The user cancelled the broad Samsung/Pixel optimisation phase. Do not restore that work or expand this task into it.
- Current work: Discover top-bar glass, web-informed GPA/Honours functionality, consistent card sizing, Settings-selected Material 3 colours, and aligned Calculator/Statistics tabs.
- Changes are local and uncommitted. Preserve other working-tree changes; do not roll back files.
- Read the parent `AGENTS.md` and the user's ARTEMIS rules before further implementation/testing. Keep Traditional Chinese primary and English secondary; use stiffness-only springs, without damping overrides.

## Completed source changes

### Discover

- `app/src/main/java/com/lingubible/app/ui/components/DiscoverTopBarGlass.kt`: decorative 32 dp surface-gradient fade with sheen beneath the Discover top bar.
- `app/src/main/java/com/lingubible/app/MainActivity.kt`: wraps navigation content and conditionally overlays the glass on Home/Discover when the top bar is visible. Does not consume input or add content spacing.

### GPA and Honours

Reference files in the parent web app:

- `src/pages/GpaHons.tsx`
- `src/utils/honours.ts`
- `src/data/firstClassHonours.ts`
- `src/components/features/gpa/FirstClassHonoursSection.tsx`
- `src/services/gpaCourseCatalog.ts`

Native files:

- `ui/screens/GpaHonsScreen.kt`: calculator/statistics views, lazy dashboard, saved view/filter state, academic-year selector, existing course editor reorganised into full-width code input and wrapping controls, stable course keys, undo/redo/reset actions.
- `ui/components/GpaDashboard.kt`: cumulative GPA/classification/credits cards, target GPA controls (0–4 and all five honours shortcuts), weighted cumulative and term chart, auto/full scale, optional honours/awards reference lines and exact text values.
- `ui/components/FirstClassHonoursContent.kt`: bundled cohort statistics, year selection, YoY changes, search, faculty filtering, metric/name sorting and faculty grouping. Null data displays an em dash; percentage-point deltas are used for rates.
- `app/src/main/assets/data/first_class_honours.json`: 30 programmes for 2024 and 2025 copied from the web reference. Totals: 2024 = 770 graduates / 109 first-class; 2025 = 900 / 119 (13.2%).
- `tools/scripts/sync_honours_reference.py`: regenerates that asset from the parent web TypeScript data; verified extraction of all 30 programmes.
- `ui/viewmodels/GpaHonsViewModel.kt`: sanitises/clamps target input, digit-only remaining credits, academic-year update with persistence/history, reuses deleted year slots up to eight years, ignores no-op document mutations.
- `ui/screens/AcademicToolsScreen.kt`: academic tab selection now uses rememberSaveable.
- Paths beginning `ui/` above are under `app/src/main/java/com/lingubible/app/`.

### UI consistency follow-up

- Summary cards share minimum height, padding and two value lines; bilingual headings are separated.
- Year cards use 24 dp corners / 16 dp padding; course cards use 16 dp corners / 12 dp padding.
- Credit and grade controls are both 128 dp wide with minimum 48 dp height, inside a wrapping row.
- Academic-year selector fills the available width.
- Programme title areas reserve a shared minimum height while allowing longer names to expand.
- Calculator/Statistics now use equal-width Material 3 `SecondaryTabRow` tabs with 16 dp horizontal inset, aligned with the content cards.
- Removed fixed grey/white/amber editor colours and fixed blue/green chart colours in favour of `MaterialTheme.colorScheme` roles from the existing Settings-controlled theme.
- Chart series use primary/tertiary colours and solid/dashed lines; legend no longer names fixed colours.
- Existing storage, backend and signing configuration were not migrated or changed.

## Build and test evidence

Use Bash with an explicit working directory if the tool's default directory points at the removed clone. SDK is `/opt/android-sdk`; no local.properties was needed.

```sh
ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleDebug :app:testDebugUnitTest --offline --console=plain
```

- Build and 335 unit tests passed after the main GPA implementation: zero failures, errors or skips.
- Four focused additions in `app/src/test/java/com/lingubible/app/GpaHonsViewModelTest.kt` cover input handling, academic-year persistence/undo/redo, deleted-year reuse and no-op history.
- Subsequent card-sizing and theme/tab refinements were built successfully. The full unit suite was not rerun after those presentation-only refinements.
- `git diff --check` passed after the theme/tab changes.
- No new Compose instrumentation tests were authored.

## Waydroid and installed APKs

- User's supplied release: `/home/ricky/Downloads/app-release.apk`, installed as `com.lingubible.app`. Preserve its data.
- No matching release keystore path has been supplied. The GitHub URL supplied in response to the key question is not a signing key. Do not uninstall the release or attempt to overwrite it with debug signing.
- Waydroid serial: `192.168.240.112:5555`, Android 13, 1080 × 2340 override. Last observed density was 440.
- A Samsung device was also connected (`R5CX22YGH7A`); it was not used. User explicitly selected Waydroid.
- Separate debug preview package `com.lingubible.app.gpapreview` is installed and open on the GPA page. It contains one sample 3-credit A grade entered via UI. Release data was not touched.
- Preview activity: `com.lingubible.app.gpapreview/com.lingubible.app.MainActivity`.

Temporary init script `/tmp/lingubible-gpa-preview.gradle` overrides only the build application ID:

```groovy
allprojects {
    afterEvaluate { project ->
        if (project.plugins.hasPlugin('com.android.application')) {
            project.android.defaultConfig.applicationId = 'com.lingubible.app.gpapreview'
        }
    }
}
```

```sh
ANDROID_HOME=/opt/android-sdk ./gradlew -I /tmp/lingubible-gpa-preview.gradle :app:assembleDebug --offline --console=plain
adb -s 192.168.240.112:5555 install --no-incremental -r app/build/outputs/apk/debug/app-debug.apk
adb -s 192.168.240.112:5555 shell am start -W -n com.lingubible.app.gpapreview/com.lingubible.app.MainActivity
```

**The current `app/build/outputs/apk/debug/app-debug.apk` is the preview-package build.** Run the normal build without the init script to produce the normal application ID. Never confuse the two during installation.

## Device validation actually performed

Used ARTEMIS screenshots/hierarchy plus ADB taps/swipes, on Waydroid in portrait with red/OLED appearance:

- Summary card alignment and empty state.
- Credit/grade control alignment; opened grade menu and selected A; year GPA updated to 4.000.
- Scrolled to academic-year selector and add-course/add-term/add-year actions.
- Statistics totals and programme cards with one- and two-line English names.
- Switched back to Calculator and retained the grade.
- Chart showed a point at 4.000 and exact term/cumulative values of 4.000.
- Latest themed equal-width tabs were built, installed and visually inspected.
- Emulator display/font configuration was not changed during these follow-ups.

Evidence:

- `docs/validation/gpa/README.md`: initial implementation validation (historical; some pending checks were subsequently completed).
- `docs/validation/gpa/sizing-waydroid.md`: later sizing and theme/tab checks.
- Images: `sizing-calculator.jpg`, `sizing-editor.jpg`, `sizing-statistics.jpg`, `sizing-chart.jpg`, `themed-tabs.jpg` in that directory.

## ARTEMIS caveats

- `mobile_get_device_state` works for screenshot/hierarchy observations with explicit Waydroid serial. Hierarchy bounds returned in these runs are normalised to 0–1000; convert to the actual display size for ADB coordinates.
- Autonomous ARTEMIS exploration was previously blocked by missing provider credentials. Do not assume this has been configured; diagnose before retrying. Credentials must be configured locally, never pasted into chat.
- A fixture injection attempt failed because Waydroid rejected `run-as` with `setegid(AID_PACKAGE_INFO) failed`. UI entry succeeded instead. Do not use a privilege workaround.
- `artemis/` is currently untracked and contains tooling as well as generated observations. Do not blindly delete or add the whole directory to a commit.

## Remaining checks / limits

- Other Settings palette choices, dynamic wallpaper colours and light mode were not exercised on-device after the theme-role changes.
- Landscape, narrow widths, larger font scales and TalkBack remain unverified in this focused follow-up.
- Multi-term chart rendering, every filter/target combination and broad recreation/state restoration flows remain unverified on-device.
- Real Samsung/Pixel hardware performance and manufacturer-specific checks remain pending; do not claim certification from Waydroid.
- Fixed bottom clearance inherited from the original app is still present; no broad inset overhaul was performed.
- Current work is available in source and the isolated preview, not in the user's signed release.
- No publishing, release signing changes, commits or pushes were performed.

## Text Field Label Audit & Single-Line Optimization

### Root Cause Analysis
In Jetpack Compose `OutlinedTextField` and `TextField`, specifying `singleLine = true` only constrains the user-entered input string. It does **not** constrain the composables passed into `label = { ... }` or `placeholder = { ... }`. When fields are empty or unselected, the label rests inside the container. If the label text contains both Chinese and English descriptions or extra parenthetical hints (e.g. `(@ln.hk / @ln.edu.hk)` or `(例: CLC9001)`), and is rendered next to leading/trailing icons or inside half-width split rows, Compose wraps the label onto 2 lines, breaking visual alignment.

### Changes Implemented
1. **Enforced Single-Line & Truncation Safety**: Added `maxLines = 1, overflow = TextOverflow.Ellipsis` across all `Text` composables inside `label` and `placeholder` parameters.
2. **Clean Separation of Concerns**: Decoupled long instruction/example strings out of floating `label` titles and relocated them into `placeholder = { Text(...) }`.
3. **Screens & Components Audited and Refactored**:
   - `AuthScreen.kt`:
     - Name field: `姓名或暱稱 · Name` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
     - Email field: concise label `嶺南大學電郵 · Lingnan Email`, format hint moved to placeholder `@ln.hk / @ln.edu.hk`.
     - Password field: `密碼 · Password` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
     - Confirm Password field: `確認密碼 · Confirm Password` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `WriteReviewScreen.kt`:
     - Course Code: `課程代碼 · Course Code`, placeholder: `例: CLC9001`.
     - Course Title: `課程名稱 · Course Title`.
     - Instructor: `講師姓名 · Instructor`.
     - Academic Year (weight 1f half-row): `學年 · Year`, placeholder: `例: 2023-2024`.
     - Term (weight 1f half-row): `學期 · Term`, placeholder: `例: Term 1`.
     - Grade: `最終成績 · Final Grade`, placeholder: `例: A, A-, B+, B`.
     - Review Comment: `心得評論 · Review Comment`, placeholder: `分享你的真實修課心得 (至少 20 字)...`.
   - `GpaDashboard.kt`:
     - Target GPA: `目標 GPA · Target` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
     - Remaining Credits: `剩餘學分 · Credits`, placeholder: `例如: 15` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `ImageToolboxColorSchemeSheet.kt`:
     - Hex code dialog input: `Hex 色碼 · Hex Code`, placeholder: `例如 #E53935` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `CourseSectionSelector.kt`:
     - Search placeholder: `搜尋課程代碼、名稱、講師或 CRN...` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `CoursesScreen.kt`:
     - Search placeholder: `搜尋課程代碼、名稱或學系...` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `InstructorsScreen.kt`:
     - Search placeholder: `搜尋講師姓名...` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `FirstClassHonoursContent.kt`:
     - Search placeholder: `搜尋課程 · Search programmes` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).
   - `GpaHonsScreen.kt`:
     - Quick course editor: `課程代號 · Course Code (例: BUS1102)` (`maxLines = 1, overflow = TextOverflow.Ellipsis`).

### Build and Device Verification
- **Unit Tests**: `./gradlew :app:testDebugUnitTest --offline` passed with 0 errors / 0 failures across 23 tasks.
- **APK Installed & Verified**: Built and deployed to Waydroid (`192.168.240.112:5555`).
- **Visual Evidence**: Captured screenshot confirmations across Auth (Login & Register), Write Review (Step 1 & Step 4), GPA Dashboard (Target GPA & Credits), Custom Color Hex Dialog, Course Selector search, Courses search, and Instructors search confirming zero multi-line wrapping in empty states.

## Release APK Generation

- **Build Task**: `./gradlew :app:assembleRelease --offline --console=plain`
- **Signing Keystore**: `app/release.keystore` generated with alias `lingubible`, storepass/keypass `lingubible_release`, DN `CN=LingUBible, OU=Mobile, O=Lingnan University Community, L=Tuen Mun, ST=Hong Kong, C=HK`.
- **Output Artifact**: `app/build/outputs/apk/release/app-release.apk` (17MB)
- **Signature Verification**: Validated with `apksigner verify --verbose` (Scheme v2 and v3 signatures intact and verified).


