# UI and release readiness — 2026-10-18

## Outcome

Completed the available Waydroid checks, fixed four layout problems, rebuilt and installed the optimized release. **546 JVM tests pass**; APK/AAB build and release guard pass; installed APK hash matches the archived artifact. Original device configuration and appearance restored; timetable still has three courses/nine credits. Android15/16, physical-device checks and automatic website association remain pending.

## Fixes

- **Hidden search field:** at200% text in landscape, the expanded header left no room above the keyboard. The shared header now expands only when requested, keyboard hidden and available height at least480dp. The visual header and shared state agree, while preserving the requested scroll state for later expansion. Final input bounds are y119–203 above the keyboard beginning y222. ACT survives rotate/Back; Back dismisses the keyboard before navigation.
- **Honours headings:** at320dp/200%, the title and faculty competed for width, producing nearly vertical text. Headings now stack at narrow widths/large fonts; cohort and percentage wrap safely. Final title widths are372px, rather than a narrow strip.
- **Tab/navigation labels:** GPA English labels stay on one line with a smaller secondary style. Bottom navigation uses centered ellipsis while retaining full icon accessibility labels; no labels spill into neighbouring items.
- **Summary numbers:** three narrow metric tiles broke13.2% across lines. They now stack for narrow windows/large fonts. The final percentage occupies one line (bounds57,684–201,746).

Only four existing UI files changed: MainActivity, LingUBibleBottomBar, GpaHonsScreen, FirstClassHonoursContent. [Focused patch](source.patch), [source hashes](source-hashes.json). Existing springs, colors, haptics, grouping and data behavior retained; no backend/dependency/toolchain/signing/version/preference migration.

## Device evidence

Waydroid Android13, initial480×1000, density180, font1.0; app Dark/OLED/red/Tonal Spot, wallpaper colors off. User reserved the device after another task was foreground. ARTEMIS diagnosis reported ready; ARTEMIS screen observations and guarded ADB grounded the checks. No new instrumentation tests authored. Driver checks use observed labels/bounds and explicit waits; final measured assertions are archived.

| Area | Result | Evidence |
| --- | --- | --- |
| Home course/teacher groups | Real lists render with small facing corners. | [Courses](home-featured-dark.png), [Teachers](home-instructors-dark.png) |
| Favourites | Signed-in empty state readable. Populated groups not exercised; no remote items created. | [Empty state](favourites-dark.png) |
| Readings | ACT1110 empty state; ACT2201 required/recommended groups, real titles and scrolling. | [Populated](readings-populated-light-100.png), [Scrolled](readings-populated-scrolled-light-100.png) |
| Study files | ACT2201 signed-in empty state. Populated files not exercised; no upload/download. | [Empty state](study-materials-light-100.png) |
| Calendar | October19 holiday + hostel grouped cards retain wording/colors. Restored Today. | [Events](calendar-events-light-100.png) |
| Honours | Grouped programme lists in light mode; corrected320dp/200% headings and numbers. | [Programme, final](honours-programme-320dp-200-final.png), [Summary, final](honours-summary-320dp-200-final.png) |
| Themes/fonts | Light100/150/200, dark100 and OLED100 inspected; original preferences restored. | [Light200](settings-light-200-layoutfix-intermediate.png), [Dark](settings-dark-100-layoutfix-intermediate.png), [Restored, final](settings-restored-final.png) |
| Keyboard/rotation | Search fully visible at landscape200%; query retained, Back dismisses IME. Test query cleared without submission/history entry. | [Before](search-keyboard-landscape-200.png), [After, final](search-landscape-keyboard-200-final.png), [After Back, final](search-back-retained-200-final.png), [Assertions](final-keyboard-assertions.json) |
| User timetable | Same three courses/nine credits; no course toggle or calculator edits. | [Final](timetable-preserved-final.png) |

`final` screenshots use APK509ad1b6… . `layoutfix-intermediate` screenshots use signed APKb536c34b… before the last summary-only change. `headerfix-intermediate` uses APKb98d6e65… . Other screen captures are baseline APK84b8d451… . These distinctions avoid claiming every theme/screen/font combination was repeated on the final APK. Actual final summary/keyboard assertions passed; original device settings match exactly. No AndroidRuntime fatal exception was present for the final app process.

## Artifacts

Version1.0.6/code7, min26/target36; signing unchanged. Final SHA256:

- APK:509ad1b6eb65ea7e20f164ca4a2454518f11d3d75b95563546c2a9538c7c6946
- AAB:ceb83410f21a8f5dbc363f6aacb5b9640d52676d9dd0bbd503b5584509c8a8c4

[Guard](release-guard.json): APK4,463,528bytes; AAB7,968,939bytes; uncompressed DEX5,193,312bytes;992renamed app types, matching mapping and required InformationPage enum retained. These are local checks, not Play Console percentages. Existing nonfatal toolchain metadata diagnostics remain. [Unit count](unit-tests.json), [Installed hash/certificate](installed-final.json), [Machine results](results.json).

Signed [APK](../../../../build/release-readiness-2026-10-18/app-release.apk), [AAB](../../../../build/release-readiness-2026-10-18/app-release.aab), mapping and build log are archived together. No publishing, commit, data clear, uninstall, account/GPA/favourite edits, recovery email, review publication, resource opening, calendar insertion or outbound messages.

## Website links: external blockers

Live www association JSON returns200 but declares only the old com.lingubible.www.twa app. Native entry is valid in the web source and [prepared copy](prepared-assetlinks.json); its certificate matches this local release, and TWA entries are retained. Confirm Play App Signing certificate separately before deployment for Play builds.

Apex lingubible.com association URL returns301 to www. Both declared hosts must serve the JSON directly without redirects ([Android association requirements](https://developer.android.com/training/app-links/configure-assetlinks)). Waydroid reports1024 for both hosts, a verifier-specific failure code, not a decoded reason ([verification guidance](https://developer.android.com/training/app-links/verify-applinks)). [HTTP evidence](assetlinks-status-no-redirect.json), [live targets](website-association.json), [device result](app-links-final.txt). No force approval/default-handler changes made. Website deployment/routing and subsequent verification are separate; nothing published.

## Remaining checks

No Android15/16 system images or AVDs installed; only Android13 Waydroid connected ([inventory](emulator-availability.json)). Android15/16 edge-to-edge, gesture/three-button/cutout tests, physical Samsung/Pixel, haptic feel and performance measurements remain pending. This was a sampled phone check, not every screen × theme × font × navigation combination. Populated favourites/study-file groups were unavailable without creating data. Play Console recalculation, Play signing fingerprint and external delivery remain unverified.
