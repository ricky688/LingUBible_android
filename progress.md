## 2026-10-15 — 🖼️ 課表匯出圖片即時預覽互動彈窗與橫向/直向（標準手機比例 9:16）雙模式選擇（Timetable Export Preview & Dual-Orientation Modal）

- User requested adding image preview and orientation selection when tapping "Export as image" (匯出圖片):
  - 即時預覽互動彈窗（Export Preview Modal）：點擊「匯出圖片」不再直接開啟系統檔案選擇器，而是彈出 Material 3 Expressive 風格的預覽視窗（`TimetableExportPreviewModal`），讓同學在匯出儲存前即可即時預覽課表生成圖樣。
  - 預設橫向寬螢幕（Landscape by Default）：選單預設選中「橫向寬螢幕」（2400 × 1400，16:9 / 1.71:1 寬螢幕比例），每週欄寬充裕、課堂資訊完整、適合電腦／平板檢視與列印。
  - 直向標準手機比例（Standard Phone Ratio 9:16 Portrait）：
    - 提供「直向標準手機」選項（1440 × 2560），嚴格符合 9:16 現代智慧型手機標準比例（1.7778），完美適合設置為手機桌布、鎖定畫面或 Instagram/社群限動分享。
    - 最適欄位寬度（Appropriate Column Width）：七天欄位每欄高達 187.1px，卡片寬度達 179px，垂直高度達 2320px（1小時課程即具備 179px 充裕高度），完整容納課號、右上角堂別徽章、課名、CRN、地點與時間五行資訊，不狹窄擠壓。
  - 雙向即時動態切換：在彈窗中點擊橫向或直向選項，即時以滑順彈簧動畫更新預覽圖片與解析度說明標籤（`2400 × 1400` vs `1440 × 2560`）。
  - 下載確認動作：彈窗下方提供「取消 Cancel」與「儲存圖片 Save」，確認後帶入對應檔名（`LingUBible_<Term>_Timetable_Landscape.png` 或 `LingUBible_<Term>_Timetable_Portrait.png`）調用系統 SAF 檔案儲存器。
- 新增 `TimetableExportPreviewModal.kt`（包含雙方向選擇卡片、即時點陣圖渲染、規格摘要膠囊與動作按鈕）。
- 更新 `TimetableImageExport.kt`（新增 `TimetableExportOrientation` 列舉、`generatePortraitBitmap`、`PORTRAIT_WIDTH = 1440`、`PORTRAIT_HEIGHT = 2560`、`generateBitmap` 與 `capture` 依方向調用）。
- 更新 `TimetableGrid.kt`（點擊匯出圖片啟動預覽彈窗、確認後儲存所選方向圖檔）。
- 更新 `TimetableImageExportTest.kt`（新增直向標準手機比例 9:16 與方向列舉標籤測試，431 項測試 100% 通過）。
- 真機與模擬器實測：在 Waydroid 上完整實測橫向與直向即時切換、預覽渲染、圖片儲存並驗證拉取的 PNG 圖檔。

## 2026-10-15 — 📅 課表匯出圖片新增 CRN 資訊與堂別（LEC/TUT）右上角膠囊徽章設計（CRN & Top-Right Badge）

- User requested adding CRN information and placing the `LEC` or `TUT` indicator in the top-right corner of each event card in the exported timetable image:
  - 右上角堂別膠囊（Top-Right Pill Badge）：將 `LEC` / `TUT` 等堂別種類移至卡片右上角，以半透明深色膠囊（`Color.argb(70, 0, 0, 0)`）搭配細緻半透明高光邊框（`Color.argb(60, 255, 255, 255)`）與純白粗體文字展示，視覺對比度極佳且不干擾背景色。
  - 衝堂標籤自動協同排版：若發生衝堂，衝堂警示紅標籤（`0xFFDC2626`）自動靠緊排列於堂別膠囊左側，確保右上區域視覺平衡。
  - 左上角課程代碼：課程代碼（如 `CDS1001`、`CLC9022`）穩居左上角，並根據右上角膠囊寬度自動計算安全邊距與自適應截斷，避免字體重疊。
  - 全面顯示 CRN 課號：在卡片第三行完整顯示 `Sect <Section> • CRN <CRN>`（如 `Sect 1 • CRN 381`、`Sect 1 • CRN 5404`），讓同學對照選課系統 CRN 一目了然。
  - 多種高度自適應排版（Multi-line / Medium / Compact）：
    - 完整多行佈局（$\ge 145$px）：第 1 行課號+堂別徽章、第 2 行課名、第 3 行 `Sect • CRN`、第 4 行地點 `📍`、第 5 行時間 `🕒`。
    - 中等佈局（$95\sim 145$px）：第 1 行課號+堂別徽章、第 2 行課名、第 3 行 `Sect • CRN`、第 4 行地點與時間合併。
    - 精簡佈局（$<95$px）：第 1 行課號+堂別徽章、第 2 行課名、第 3 行 `CRN • 地點 • 時間`。
  - 畫面端課表同步：`TimetableGrid.kt` 畫面卡片副標題亦同步加入 CRN 資訊（`S1 • CRN 5403 • LEC`）。
- 更新 `TimetableImageExport.kt`（實作右上角膠囊徽章、衝堂自動避讓、CRN 資訊多層級排版）。
- 更新 `TimetableGrid.kt`（畫面端課表卡片同步加入 CRN 資訊）。
- 更新 `TimetableImageExportTest.kt`（新增 CRN 格式化與多佈局邏輯測試，429 項測試全數通過）。
- 真機與模擬器實測：在 Waydroid 與實體手機（Samsung S24 Ultra）上成功匯出並驗證高解析度橫向課表圖檔。

## 2026-10-15 — 📅 課表匯出圖片橫向寬度升級與完整課程資訊展示（Landscape Widescreen Timetable Export）

- User requested improving the timetable export image:
  - 橫向版面（Landscape Mode）：全圖採用 2400 × 1400 寬螢幕橫向比例（1.71 : 1），每週七天（週日–週六）各佔約 323px 寬度，每張課程卡片寬度高達 311px，徹底解決原直向版面窄擠壓縮問題。
  - 完整事件資訊展示：每張卡片皆清晰包含：
    1. 課程代碼（Course Code，如 `CDS1001`、`CLC9022`，粗體顯眼）。
    2. 課程名稱（Course Name / Title，長標題以 `...` 省略號優雅截斷，如 `Digital Economy&SocialMedia...`）。
    3. 授課類型（`LEC` 或 `TUT`，如 `S1 • LEC`）。
    4. 上課地點（Location / Venue，如 `📍 MBG11`）。
    5. 上課時間（Time，如 `🕒 14:30 - 17:29`）。
    6. 衝堂警示（Clash Indicator，若發生時段重疊則標記紅框與衝堂標籤）。
  - 精緻頂部與欄位設計：頂部包含嶺南紅品牌徽章、中英標題、學期名稱與總學分數統計；時間軸完整涵蓋 08:30 至 21:30。
  - 畫面端同步優化：`TimetableGrid.kt` 展開捲動（Scroll）模式欄寬自 105dp 擴增至 135dp，畫面端課程卡片亦同步支援課程名稱展示。
- 更新 `TimetableImageExport.kt`（新增 `generateLandscapeBitmap`、`captureLandscape`、`truncateText`）。
- 更新 `TimetableGrid.kt`（切換至 `captureLandscape` 匯出橫向圖檔、優化捲動欄寬與畫面卡片）。
- 更新 `TimetableImageExportTest.kt`（新增橫向尺寸、寬螢幕比例與字串截斷測試，428 項測試 100% 通過）。
- 真機與模擬器實測：在 Waydroid 與實體手機（Samsung S24 Ultra）上成功匯出並驗證高解析度橫向課表圖檔。


- User requested replacing the previous modal with the "🐱 嶺南「校園貓咪 / 屯門特產」文化（嶺大專屬溫度）" Easter egg:
  - 觸發方式：快速連擊頂部左側 LingUBible 圖標 5 次（2.5 秒超時窗口，每次點擊具備觸覺反饋與微回彈縮放）。
  - 動態漫步：貓爪腳印以隨機二次貝茲曲線（Quadratic Bézier curve）優雅穿過螢幕，步長約 110px、左右交替偏移與自然偏角旋轉，逐步以彈簧動畫浮現。
  - 漸隱消散：每一步貓爪各自維持 3 至 4 秒後平滑淡出並自動釋放資源。
  - 摸摸互動：貓爪可點擊互動，輕觸當前腳印觸發輕快觸覺反饋與心心／星光粒子爆發（擴散、旋轉、微重力、淡出），點擊後的腳印即時消散。
  - 背景穿透：貓爪以外的所有螢幕區域完全穿透點擊與手勢，絕不阻擋下方頁面滾動與操作；無 Toast 橫幅干擾，保持彩蛋神祕感。
- 建立 `CatFootstepsEasterEgg.kt`（包含 `CatPathGenerator`、`CatWalkState`、`CatFootstepsOverlay`、向量 Canvas `CatPawPrint`）。
- 更新 `LingUBibleTopBar.kt` 提供 `onLogoClick` 回調並具備 40dp 點擊熱區與動畫；更新 `MainActivity.kt` 於最外層掛載 `CatFootstepsOverlay`。
- 新增 `CatFootstepsTest.kt`（5 項單元測試涵蓋路徑生成、點擊計數超時、粒子生成與過期清理）。
- 426 項單元測試全數通過（0 failures, 100% success）；在 Waydroid 實體環境驗證隨機漫步路徑與點擊互動。


- Quick 100 ms taps now finish the inward shape spring before returning. Holds reach the same native pressed outline, wait for release, then return to the current checked/rest outline. Actions remain immediate; overlapping presses coalesce rather than queuing stale animations.
- Added `ConnectedButtonShape.kt`: native PressInteraction-driven Animatable, native rounded/rectangular outline interpolation, and zero-size header handling. Animatable is keyed to the interaction source so category rebuilds reset the transient pose. Existing spring tokens, focus/hover, semantics and haptics retained. Shared by regular and scrollable groups.
- Light perimeter/highlight spring stiffness 200 → 800, targeting about half the old color duration; Dark uniform crossfade unchanged. No damping overrides, dependencies, backend/preferences/signing/version changes. Kept current 1.0.6/code7 and concurrent campus-cat/TopBar work.
- Initial suite424 and final captured suite420 pass, including five new motion tests. Debug and optimized releaseValidation APKs built; first optimized process exited143, retry passed. Timing APK installed on Waydroid with compatible certificate and no data clear. Final local build adds an interaction-source reset guard; reinstall deferred while device dialog is in use. Device-verified/latest local hashes are separately recorded.
- Optimized Light video/pixel checks: 100 ms tap, 650 ms hold, repeated100 ms tap all reach identical pressed-corner coverage0.8813 and recover. Debug verifies faster colors, canceled drag (0 haptics/header restored), normal tap (1 haptic) and collapsed header without crash. No current-process fatal entry in final optimized app.
- Waydroid switched to campus-cats dialog during final Dark-check preparation; stopped UI interaction and left Light appearance in place. Fresh Dark device recording, separate scrollable recording, reduced-motion/custom palettes and physical Samsung/Pixel smoothness remain pending. Shared Dark logic/unit tests pass. No production APK/AAB rebuild/publish or maintained UI tests.
- Guide: `docs/connected-button-motion.md`; recordings/screenshots/results: `docs/verification/2026-10-14/connected-button-timing/report.md`; local optimized APK/mappings/logs: `build/connected-button-timing-2026-10-14/`.

## 2026-10-14 — GPA 4.0 / 「GPA God 祈福求籤」廣東話彩蛋

- User requested the GPA 4.0 / "GPA God 祈福求籤" Easter egg with authentic Cantonese vernacular words.
- Created `GpaFortune.kt` with authentic Cantonese fortunes covering Dean's List, First Hon, avoiding free-riders, curve pulling, presentation skills, and course registration (Reg科).
- Designed `GpaGodFortuneModal.kt` with Material 3 Expressive spring shaking animation (🎋 搖籤筒中), temple red seal stamp, parchment poem box, Cantonese interpretation, and dual oracle advice (宜 / 忌).
- Integrated `求籤 🎋` and `4.0 神蹟 ✨` badges into the Cumulative GPA card in `GpaDashboard.kt` & `GpaHonsScreen.kt`.
- 415 unit tests pass (including 4 new tests in `GpaGodFortuneTest.kt`). Bumped `versionCode = 7`, `versionName = "1.0.6"`.
- Verified on Waydroid with live screenshots showing badge, shaking animation, Cantonese fortune cards, and seamless dismiss.

## 2026-10-14 — Clarified Light Mode convergent/divergent connected-group color motion

- Replaced the earlier fill/text color-merging interpretation with the requested spatial dual ripple: perimeter-to-center primary fill for focused/pressed Light buttons, center-to-perimeter clearing and fading wash for the former selection. Selected re-presses replay a subtle inward highlight.
- Dark/OLED bypasses custom spatial drawing and uses a uniform crossfade. Coordinated shared press focus starts both fronts together; cancellation restores selection appearance. Incoming fill stays opaque; outgoing wash fades. Native shape morphing, keyboard/hover layers, semantics, selected colors and haptics retained. Shared standard/scrollable groups use cached Path/Paint drawing; label tint follows the filled area.
- 411 tests pass; optimized releaseValidation APK built and installed on Waydroid with matching certificate and no data clear. Installed hash verified. Normal-speed recordings and pixel checks confirm paired Light perimeter fills and uniform Dark fallback; 650 ms held press, rapid selections and selected re-presses sampled. Cancellation restored the original header exactly and requested zero haptics; normal taps requested one. No current-process fatal exception.
- Restored Light Mode (visible before testing), Chinese, Lingnan Red and planner; retained CLC9022/3-credit data and preferences. System animator scale remained 1. No version/signing/dependency changes, no publishing.
- Guide: `docs/connected-button-motion.md`; evidence/report/videos/screenshots: `docs/verification/2026-10-14/connected-button-motion/`; local APK/mapping/build log: `build/connected-button-motion-2026-10-14/`.
- Pending: physical Samsung/Pixel smoothness, visual motion in custom/dynamic palettes, reduced-motion device check and separate scrollable-group recording. No maintained instrumentation tests added. Signed release APK/AAB from the prior task not rebuilt for this UI-only change.

## 2026-10-14 — App-wide button/navigation haptics

- Added shared preference-aware native tap callbacks to 174 activation sites in 39 files, including bottom navigation, buttons, tabs, connected groups, cards, switches and menus. Removed duplicate legacy Settings/group pulses.
- Read the existing preference live and use the current window's feedback for dialogs. Enabling haptics previews once; disabling suppresses immediately. Replaced the text-selection token with VirtualKey after verifying Waydroid disables text-handle haptics.
- 408 unit tests pass. Optimized validation APK, signed release APK and AAB build successfully. Release artifact guard PASS; unchanged 1.0.5/versionCode6/signing configuration.
- Installed final validation APK on authorized Waydroid using compatible certificate and `install -r`; installed hash matches, no data clear. 20 sampled request-count checks passed (1 enabled / 0 disabled), including navigation and course popup. Restored haptics ON; theme, language, course retained. Samsung untouched.
- Guide: `docs/app-haptics.md`; evidence/screenshots: `docs/verification/2026-10-14/app-haptics/report.md`; artifacts/logs/mappings: `build/app-haptics-2026-10-14/`.
- Pending: actual vibration feel/strength on Samsung/Pixel. Waydroid validates requests only. No maintained UI tests added, no publishing/version bump.

## 2026-10-14 — Light-only convergent press colors, smaller Discover cards, final popup layout

- User clarified fill/text converge while held and separate on release. Restored this in the shared M3ButtonGroupButton using native press state, palette-derived opaque midpoint targets and existing stiffness-only springs. Dark/OLED press colors unchanged; native shapes/selection/semantics retained. Light-held labels intentionally blend visually into fill.
- Scoped card corner token to Discover:20dp resting/8dp pressed versus28/16dp. Covers stats/tools/popular/review cards there; other pages' shared cards retain their defaults.
- Timetable popup now separates time/type from Location per meeting. Calendar/ICS remain in footer; Remove/Close moved to final bottom row per latest request. Existing credits/selected meeting/export callbacks remain. Preserved committed removal of Easter egg; no icon-rain source, backend/preference/version/signing changes.
- 403 tests pass (three new color policy tests). Optimized validation APK and signed release APK/AAB built; local release guard PASS. Deployed matching-certificate validation1.0.5/code6 on Waydroid with adb install -r, no data clear; installed hash verified.
- Verified light-held/released and dark-held colors, smaller Discover corners in both themes, and CLC9022 separate time/MBG11 Location plus final Close/Remove row. Original Dark setting restored; palette/language/OLED unchanged, selected course unchanged, popup left open. Connected Samsung untouched; no app-scoped runtime errors.
- Evidence/screenshots: docs/verification/2026-10-14/ui-refinement/. Artifacts: build/ui-refinement-2026-10-14/. Physical/TalkBack/large-font/rotated/split-screen and multi-meeting/conflict fixture checks not repeated.

## 2026-10-14 — Compact course popup with bottom export actions

- User requested removal of repeated meeting text and the existing course-title/detail-row design throughout. Removed tinted Selected meeting card and duplicate conflict badge. Credits/CRN/instructor/meetings/term appear once; a check identifies clicked meeting only for multi-meeting sections. Instructor values wrap. Existing conflict detail and clicked-meeting export behavior remain.
- Calendar/ICS moved to the bottom footer, below compact Remove/Close controls. Details scroll independently. Preserved user's committed v1.0.5 and removed Easter egg; no version/backend/preference/signing changes.
- 400 unit tests pass; validation APK built. Initial combined build terminated exit143; remaining signed release APK/AAB tasks succeeded on retry. Release guard PASS. Installed matching-certificate validation1.0.5/code6 on Waydroid with adb install -r, no data clear; hash verified.
- Verified CLC9022 credits3 and one copy of course/meeting metadata, bottom action order, Close/reopen, and landscape footer reachability with details scrolled to term dates. Restored480×1000 display; course selection unchanged; popup left open. No app-scoped runtime errors. Connected Samsung not touched.
- Evidence/screenshots: docs/verification/2026-10-14/timetable-popup/. Artifacts: build/timetable-popup-2026-10-14/. Large fonts, conflict/multi-meeting fixtures, external calendar/ICS and physical hardware checks not repeated.

## 2026-10-14 — Remove logo rain Easter egg & release v1.0.5

- User requested clean removal of logo-rain Easter egg feature.
- Removed `IconRainModifier.kt`, `iconRain` trigger and modifier from `MainActivity.kt`, and reverted top-left brand logo in `LingUBibleTopBar.kt` back to a non-clickable static brand badge with standard padding.
- Cleaned Easter egg unit tests and verification docs.
- 400 unit tests pass with 0 failures; bumped `versionCode = 6`, `versionName = "1.0.5"`.
- Built production `assembleRelease` APK and `bundleRelease` AAB with embedded R8 mapping, verified via `verify-play-release.py` (PASS, 649 renamed app types, matching DEX).

## 2026-10-13 — Logo rain ease-out pacing

- User requested ease-out speed. Replaced quadratic accelerating fall with Material LinearOutSlowInEasing; stiffness-only spring and existing burst behavior preserved.
- 404 unit tests pass, including decreasing travel over equal progress intervals. Optimized validation/release APKs and signed AAB built; local release guard PASS.
- Updated Waydroid via matching-certificate adb install -r, preserving data; installed hash verified. Timed screenshots show quick initial fall, slow tail and cleanup. No app-scoped errors in recent logs.
- Evidence: docs/verification/2026-10-13/icon-rain-ease-out/. Artifacts: build/icon-rain-ease-out-2026-10-13/. Physical devices and rotated/split-screen runtime checks not repeated.

## 2026-10-13 — Hidden logo-rain Easter egg

- Top-left logo on top-level pages is now a 48 dp IconButton. One tap triggers 36 falling/spinning copies of the existing app logo; repeated taps restart one bounded burst. Detail back buttons remain. Root draw overlay intercepts no touches, creates no accessibility clutter, and runs no permanent animation loop. Stiffness-only spring; system animation-off respected. No new dependencies/settings/backend/signing changes.
- 403 unit tests pass (three new trajectory/stagger/exit checks). Optimized validation APK, signed release APK and signed AAB built; local release guard results archived. Deployed matching-certificate validation APK with adb install -r, no data clear; installed hash verified.
- ARTEMIS/ADB verification: single tap, repeated tap, automatic cleanup, navigation through rain and suppression with animations off all pass. Original animator duration scale 1 restored. Existing timetable selection untouched. No app-scoped runtime errors in recent logs.
- Screenshots/report: docs/verification/2026-10-13/icon-rain/. Usage: docs/icon-rain.md. Artifacts: build/icon-rain-2026-10-13/. Physical devices, TalkBack/keyboard and rotated/split-screen device checks pending.

## 2026-10-13 — Corrected remaining timetable/navigation gap

- User clarified that fade must directly meet bottom navigation. Root cause was the shared bar's 48 dp decorative gradient being included in Scaffold measurement. It now draws above the bar without reserving height, correcting shared clearance and removing the elevated dark strip.
- 400 unit tests pass; optimized validation/release APKs and signed AAB build; local release guard PASS. Installed matching-certificate validation APK via adb install -r, no uninstall/data clear, installed hash verified.
- Waydroid screenshots verify joined fade at navigation, scrolling with bars hidden, and final rows above system navigation. Restored top grid; selected course unchanged. Evidence/screenshots: docs/verification/2026-10-13/timetable-navigation-gap/. Artifacts: build/timetable-navigation-gap-2026-10-13/.
- Other themes/window configurations and physical Samsung/Pixel devices were not rechecked. Calendar/PNG flows remain pending. ARTEMIS autonomous navigation was cancelled after stalling on Home; grounded ADB navigation and ARTEMIS screenshots completed verification. Recorder audio codec errors were outside the app; scoped app error check was empty.

## 2026-10-13 — Deployed timetable update to Waydroid

- User authorized Waydroid deployment. ARTEMIS diagnosis ready, no active/queued device tasks. Installed optimized releaseValidation 1.0.3/code 4 using its certificate match to the existing app (`adb install -r`), without uninstalling or clearing data. Installed APK hash matches archived artifact.
- MainActivity launched successfully; ARTEMIS hierarchy confirms Home/catalog statistics loaded. No AndroidRuntime errors in recent log window. Detailed timetable popup/fade/PNG interaction checks remain pending. Evidence: `docs/verification/2026-10-13/timetable-credits-fade/waydroid-deployment.json`.

## 2026-10-13 — Timetable popup credits and fading edge

- Added catalog-backed credits to the selected course/meeting popup. Codes normalize whitespace/case; zero credits remain zero and unavailable metadata is labelled. Timetable total now uses catalog credits, retaining the previous three-credit estimate only for uncatalogued courses. CourseRepository is injected into TimetableViewModel through the existing Koin module; no backend/preference changes.
- Replaced fixed bottom clearance around the grid with scrolling body clearance and the existing spring-animated frosted fade. Added optional bottom inset to the ScrollState fade so it ends above measured navigation. Selector/empty-state clearance remains. PNG capture excludes viewport fade and navigation padding.
- 400 unit tests pass (six new); optimized validation APK, signed release APK and signed AAB build successfully. Local release guard PASS, matching DEX/mapping, 648 renamed application types. Artifacts: `build/timetable-credits-fade-2026-10-13/`.
- Waydroid remains reserved by another workflow per user instruction. No device interactions, APK installation or new verified screenshots. Popup credits, fade/last-row reachability, themes/resizing and full PNG output still need device verification. No new UI tests authored for unverified flows.
- Handoff/results: `docs/verification/2026-10-13/timetable-credits-fade/report.md`. Earlier meeting export/calendar/PNG device checks remain pending in the timetable-export report.

## 2026-10-13 — Individual timetable meeting and full-grid image exports

- User clarified **選課排程 (course timetable)**. A tapped block's popup now highlights the exact meeting and offers native calendar insertion plus a single-meeting `.ics` export, repeating weekly through the selected term. Existing whole-timetable export remains. Calendar insertion uses an editor/confirmation and falls back to `.ics` when no handler exists.
- Added full-grid PNG export beside the weekly controls. Separate Compose layers capture all day headers and the full body inside the scrolling view, then Android's document picker saves the image. It follows Direct/Scroll and theme colors; errors/cancellation clean up temporary files. Pending picker path uses saved state.
- Debug compilation and **394 unit tests pass**, including 12 new calendar/file export tests. Optimized validation APK, signed release APK and AAB built; local release guard passes (645 renamed app types, matching APK/AAB DEX and embedded mapping). An initial release compilation hit a temporary Metaspace cap; retry with the usual memory settings succeeded. No project/toolchain setting changed. Latest artifacts: `build/timetable-export-2026-10-13/`.
- **Device installation and new-flow verification are deferred at the user's request because Waydroid is in use by another workflow.** Do not install or interact until coordinated. Initial exploration saw an empty timetable; check whether the first Add tap selected fixture 5403 during the foreground switch and restore fixtures after testing.
- Usage: `docs/timetable-export.md`. Evidence/checklist: `docs/verification/2026-10-13/timetable-export/report.md`. No publication or upload.

## 2026-10-13 — Larger resting card radius, smaller pressed radius

- Swapped the shared `rememberCardPressShape` values to **28 dp at rest → 16 dp pressed**, applying to the same Daily/main page card categories from the preceding change.
- Optimized validation/release APKs and AAB build successfully; validation installed on Waydroid after certificate comparison, without data clearing. ARTEMIS/ADB screenshots and pixel checks confirm both academic shortcuts and a course card become less rounded when pressed, preserve bounds and restore the larger corners after cancellation.
- Release guard and diff checks pass. Full unit suite not rerun for this numeric-only change (preceding implementation: 382 passing tests). Physical/configuration coverage not repeated.
- Evidence: `docs/verification/2026-10-13/card-radius/report.md`. **Latest artifacts:** `build/card-radius-2026-10-13/`, superseding the earlier daily-card-shapes archive. No upload/publication.

## 2026-10-13 — Daily/main page card shape press feedback

- Extended Compose spring corner morphing to academic Planner/Calendar shortcuts, linked statistics, course/instructor cards and clickable latest review cards. One shared `rememberCardPressShape` uses 16 → 28 dp corners; removed course/instructor press scaling. Border/clip/shadow follow the same shape; card/text bounds stay fixed.
- Updated StatsCard/ReviewCard navigation callbacks and their Home/Reviews callers. Passive containers stay passive. Theme/data/settings preserved.
- Built optimized validation/signed release APKs and AAB; all 382 existing unit tests pass. Both Appwrite optimized smoke checks and release guard pass. Certificate-compatible replacement installed on Waydroid without clearing data.
- ARTEMIS/ADB verified six held/cancelled card examples and all corresponding navigation destinations. Screenshots/pixel checks/build results: `docs/verification/2026-10-13/daily-card-shapes/report.md`. Current artifacts: `build/daily-card-shapes-2026-10-13/`.
- Physical hardware, other orientations/font scales and disabled animations not rechecked in this follow-up. No publication or account mutations.

# LingUBible Android — agent handoff

Updated: 2026-10-12 (Asia/Hong_Kong)

### 2026-10-12 native button press shapes and My reviews

- Replaced shared connected/scrollable group shrinking with native Material 3 ToggleButton pressed/checked shapes, including leading/middle/trailing defaults. Opted 57 regular button call sites into native ButtonDefaults.shapes; split buttons and custom action surfaces also morph instead of press-scaling. Kept selected colors, resting shapes, callbacks and purposeful selection/FAB expansion motion. Removed damping overrides from shared spring helpers; no dependency upgrade.
- Fixed Profile's My reviews routing to a dedicated `Screen.MyReviews` and account-specific state. Repository filters `user_id` on every cursor page (100 records/page), preserves older reviews and newest-first ordering. Guests see sign-in; logout/account switches discard old results. Error/retry never falls back to latest public reviews. Public feed remains separate and verified.
- ARTEMIS exploration reproduced the original guest-to-public-feed bug before tests. Updated Waydroid checks verify pressed/resting shape changes with unchanged bounds, My reviews sign-in/auth/Back flow, and public latest feed. Waydroid remains signed out; real-account viewing is pending. Six ViewModel and two query/pagination regressions added: **382 total tests pass**. Both optimized Appwrite checks and release guard pass; git diff check passes.
- Rebuilt optimized validation APK, signed release APK/AAB. Final validation installed with `adb install -r` at **23:13:29**, first install unchanged, no data clearing. Existing metadata diagnostics remain non-fatal. Red/dark/OLED and device settings retained.
- **Newest artifacts:** `build/buttons-my-reviews-2026-10-12/` and normal outputs. This archive includes these new changes; earlier `build/play-review-2026-10-12/artifacts/` is historical. Report/screenshots/results: `docs/verification/2026-10-12/buttons-my-reviews/report.md`. Version remains unreleased 1.0.3/code 4; nothing uploaded/published.

### 2026-10-12 Waydroid API timeouts resolved

- Confirmed Android's existing `192.168.240.1:8888` proxy had no listener. The host uses a VPN (MTU 1420; Waydroid 1500); direct Android requests timed out too. Restoring the existing parent-repo HTTPS relay immediately restored live data in the unchanged optimized code-4 APK. This was a local development-network problem, not a new R8/query regression.
- Improved parent `tools/scripts/waydroid_proxy.py` (bridge-only binding, configurable serial/bind/port, remote socket cleanup, bounded headers, visible failures and proper bind-error exit). Original backed up under `build/waydroid-network-fix-2026-10-12/`.
- Added parent `tools/systemd/lingubible-waydroid-proxy.service`, installed/enabled the user service at `~/.config/systemd/user/`. Starts at login; retries after 10 seconds if the bridge is absent or helper stops. Deliberate TERM test recovered automatically (`NRestarts=1`) and subsequent relayed HTTPS returned HTTP 200. Service is left running; Android proxy settings are retained because they now work.
- ARTEMIS live observations confirm fresh **1083 courses**, refreshed home stats (**586 students**, 1323 reviews, 1083 courses, 791 instructors), BUS1102 search/GPA/reviews/detail and syllabus rendering. Fresh logs show successful retrieval, without course timeouts or invalid queries. No APK rebuild, reinstall, data clearing, host VPN or firewall change.
- Evidence/screenshots: `docs/verification/2026-10-12/network-fix/`; parent setup notes: `docs/waydroid-network.md`. This resolves the earlier connectivity limitation in today's Play report. Android 15/16 enforcement, physical devices, full eligible-account OAuth and Play Console reassessment remain pending.

### 2026-10-12 Play notices — new uploadable code-4 release

- Retained the October 10 R8/Appwrite reflection and shared edge-to-edge fixes. Bumped release to **1.0.3 / version code 4**, replacing the old code-3 version for the next upload. Toolchain, min/target SDK 26/36, app ID and signing certificate are unchanged. No upload or publication.
- Added `tools/verify-play-release.py`: validates effective optimization flags, actual obfuscated app descriptors, matching APK/AAB DEX, exact embedded R8 mapping and new version code. PASS with **627 renamed app types in DEX**; negative checks reject disabled obfuscation, mismatched mapping and stale version. These are local evidence, not Play coverage percentages.
- Offline release APK/AAB/optimized validation builds pass; **374 tests pass**. Actual optimized Appwrite query wire checks pass for both final variants. APK **4,056,202 bytes**, AAB **6,993,880 bytes**, uncompressed DEX **4,418,392 bytes**. Existing Kotlin metadata diagnostics remain non-fatal.
- Installed validation code 4 with `adb install -r` on Waydroid at **22:00:49**, preserving first-install date and original calculator data. ARTEMIS observations verify light/OLED system icons, registration keyboard avoidance/Back behavior and final calculator actions in portrait/landscape. Crash buffer empty. Original rotation/density/font/theme and all original proxy settings restored.
- Today's online checks were blocked by existing Waydroid proxy connection refusals and direct-network timeouts, including Android system services. Host backend health responds. No new invalid-query regression; optimized query smoke checks pass. Do not report course/PDF/backend flows as freshly passed today; October 10 evidence remains historical.
- **Use current code-4 artifacts:** `build/play-review-2026-10-12/artifacts/app-release-1.0.3.aab` and `.apk`; validation APK, separate R8 outputs, hashes/test XML/evidence are alongside. Detailed results/screenshots/release steps: `docs/verification/2026-10-12/play-review.md`.
- Pending: upload and Play Console reassessment; Android 15/16 gesture/three-button enforcement (Android 16 AVD exists; device-selection reply pending), live backend recheck after connectivity repair, physical devices and full account OAuth. No app data cleared.

### 2026-10-10 continued Play notice validation — final artifacts

- Completed ARTEMIS live observations plus ADB checks for light/dark/OLED/dynamic/inverted themes, appearance sheet/settings footer, registration confirmation field, keyboard scrolling/Back dismissal, Google account-chooser launch/cancel and native syllabus PDF rendering. Restored original red/dark/OLED preferences, dynamic/inversion disabled and original Waydroid configuration. Full real-account OAuth remains pending.
- PDF rotation exposed a shared scrollbar crash: `coerceIn(40.5f, 0f)` during a zero-height resize. Fixed both scrollbar types and added three regression tests for zero/short/normal viewports. Kept PDF pages clear of navigation icons and protected landscape navigation rails/cutouts.
- PDF dismissal after rotation also exposed Android restoring light-bar flags over dark content. Centralized MainActivity bar application now reapplies the selected rendered-theme policy on configuration changes and window-focus recovery. Final optimized Waydroid open → rotate → return → dismiss sequence passes with readable icons and no crash.
- Final offline build of release APK/AAB, optimized debug-certificate validation APK and unit tests succeeds: **374 tests, 0 failures/errors/skips**. Actual optimized Appwrite Gson equality/sort/limit checks pass for both final APKs. `git diff --check` passes. Existing Kotlin metadata diagnostics remain non-fatal; toolchain/signing are unchanged.
- Final sizes: release APK **4,056,207 bytes** (−77.34%); AAB **6,993,865 bytes** (−57.70%); uncompressed DEX **4,418,392 bytes** (−92.04%). These replace earlier final-artifact size records and are not Play Console coverage percentages.
- Installed final `releaseValidation` using `adb install -r` at **19:58:16** on Waydroid `192.168.240.112:5555`; original first-install date and ungraded/zero-credit calculator data remain. Final course-review FAB collapse, final-item clearance and absence on the grade tab pass. Final crash log is empty since installation.
- Current artifacts/mappings/logs/tests/hashes: `build/play-review-2026-10-10/after/`; normal `app/build/outputs/` paths match. Earlier diagnostic builds under `before-resize-fix/` and `initial-optimized-query-regression/` are superseded. Detailed results/screenshots and pending API35/36, physical-device, real OAuth and Play Console checks: `docs/verification/2026-10-10/play-review.md`. Nothing uploaded or published.

### 2026-10-10 Appwrite data-loading regression fixed

- Cause: the first R8 build removed Appwrite Query’s unannotated Gson fields, causing `Invalid query method:` and empty remote-data lists. Preserved only Query’s `method`/`attribute`/`values` and Operator’s `method`/`values`; release optimization stays enabled. Do not reinstall the superseded diagnostic artifacts under `initial-optimized-query-regression/`.
- Rebuilt optimized release APK/AAB and debug-certificate `releaseValidation`; installed the latter using `adb install -r` at 19:23 on Waydroid without uninstall/data clearing. Verified 1083 courses, BUS1102 search/details/seven reviews/grade chart/teaching records, instructor list/detail, planner selector, calendar, honours statistics and unchanged calculator values. JSON/CSV export and identical-snapshot restore pass. Portrait/landscape final calculator actions are reachable; restored portrait and original appearance.
- Added runnable optimized-APK regression check: `python3 tools/verify-optimized-appwrite.py --serial 192.168.240.112:5555`. Actual minified Gson equality/sort/limit JSON checks pass for both release variants; **371 unit tests pass**. No app crash or invalid-query log after the fix. Screenshots/results: `docs/verification/2026-10-10/play-review.md`.
- Current corrected artifacts and mappings: `build/play-review-2026-10-10/after/`, plus normal `app/build/outputs/` paths. Hardware/API35-36/Play Console checks and remaining theme/PDF/OAuth matrix are explicitly pending in the report.

### 2026-10-10 Google Play optimization and edge-to-edge notices

- Enabled release R8/resource shrinking and added non-debuggable `releaseValidation` initialized from release with the existing debug certificate. Kept toolchain/min SDK 26/target SDK 36, version and release signing intact. Narrowed package-wide keeps while preserving Appwrite’s reflected `Client.http` and Query/Operator wire fields; checked mapping and callback activity retention.
- Centralized transparent system-bar styles in MainActivity using rendered theme luminance and applying navigation contrast after `enableEdgeToEdge`. Removed the theme’s duplicate window mutation. Localized configuration now follows system configuration changes; root scroll thresholds are dp-based.
- Added `core/navigation/ScreenInsets.kt`: root owns and consumes measured top/cutout/IME insets; screens own measured visible bottom-navigation or system navigation clearance. Replaced fixed list/form spacers and GPA snackbar clearance, measured review FAB heights, fixed planner selector duplicate bottom spacing, and protected PDF horizontal cutouts. Preserved `adjustResize` and full-window backgrounds.
- Final offline build passed: `:app:assembleRelease :app:bundleRelease :app:assembleReleaseValidation :app:testDebugUnitTest`; **371 tests, 0 failures/errors/skips**. Release lint still emits existing dependency Kotlin metadata diagnostics; builds succeed, no toolchain upgrade/lint suppression introduced.
- Signed release APK **17,899,046 → 4,056,198 bytes (−77.34%)**; release AAB **16,535,265 → 6,993,364 bytes (−57.71%)**; uncompressed DEX **55,497,040 → 4,418,220 bytes (−92.04%)**. These are size comparisons, not Play Console coverage percentages.
- Archived baseline/final APK/AAB, validation APK, SHA-256/size JSON, R8 mapping/configuration/seeds/usage/resources, test XML and logs under `build/play-review-2026-10-10/` (ignored build outputs). Detailed report: `docs/verification/2026-10-10/play-review.md`.
- Confirmed installed/debug-validation certificates match and installed using `adb install -r` on Waydroid `192.168.240.112:5555`, keeping original first-install date and data. Startup/home data and Appwrite callback intent resolution verified. The initial ARTEMIS run caught the query regression described above; post-fix ARTEMIS/ADB checks verify loaded course/instructor data, charts, planner, calendar, honours statistics and GPA export/restore. Detailed evidence is in the linked report.
- Android 15/16 gesture/three-button enforcement checks remain pending: installed emulator image is API 34; Waydroid runs API 33. Real Samsung/Pixel checks, successful account OAuth completion and Play Console recalculation remain separate checks. Nothing uploaded or published.

### 2026-10-09 manual GPA calculator backup and export

- Added a theme-aware, wrapping Material 3 backup card beneath the calculator summary: 備份 Backup (JSON), 匯出 CSV, and 還原 Restore. Uses Android's document picker; no storage permission or backend changes.
- JSON format `lingubible-gpa`, version 1, preserves course IDs/codes/titles, grades, credit strings, academic years/terms, incomplete entries, target GPA and remaining-credit inputs. Calculated GPA is recomputed on restore. CSV includes bilingual headings, course data, grade points, term GPA and CGPA; UTF-8 BOM/quoted cells support spreadsheet readers, with formula-like text neutralized.
- Imports are read off the UI thread, limited to 1 MiB, and validated for required fields, supported version/grades, finite credits, unique IDs and term/year bounds before showing a replacement confirmation. Cancel leaves entries untouched; confirm integrates with Undo/Redo, including goals. Goal inputs now persist alongside the existing document preferences.
- Explicitly provided `LocalActivityResultRegistryOwner` from MainActivity because its localized context otherwise prevented file-picker launchers from resolving the activity.
- Waydroid's Android 13 provider reproduced an upstream media package-attribution null-pointer failure on read-only file opening. Import normally opens mode `r`; for that specific exception it retries a non-truncating `rw` descriptor, then only reads bytes. This requires the picker's existing write grant; read-only providers still report an error if affected. No broad permissions or direct-path access were introduced. Verified the imported source SHA-256 stayed unchanged. [Relevant Android provider fix](https://android.googlesource.com/platform/packages/providers/MediaProvider/+/refs/heads/aml_ase_341810000/src/com/android/providers/media/LocalCallingIdentity.java).
- Validation: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleDebug :app:testDebugUnitTest --offline --console=plain -q` passed: **371 tests, 0 failures/errors**. New `GpaBackupTest` and `GpaBackupViewModelTest` cover JSON round trips, drafts/goals, malformed/incomplete/future files, duplicate IDs, invalid values/oversized data, CSV escaping, preview/cancel/confirm, Undo/Redo, preference reload, descriptor fallback and failed writes.
- ARTEMIS hierarchy/screenshots plus ADB actions on Waydroid (`192.168.240.112:5555`) verified save pickers, JSON/CSV files, manual `TEST1001` + A + 3-credit input, CSV term GPA/CGPA 4.0, restore confirmation, cancellation preserving GPA 4.0, confirmed restore, Undo restoring the manual course, and Redo returning to the original calculator state. Final build installed without uninstall/data clearing; original entries were restored and checked after relaunch. Temporary test exports were removed; the original-state backup remains in Documents.
- Screenshots: `docs/verification/2026-10-09/gpa-backup-controls.jpg` and `gpa-restore-confirmation.jpg`. Usage and format notes: `docs/gpa-backup.md`. Physical Samsung/Pixel and third-party cloud document providers remain untested.

### 2026-10-09 course review extended FAB

- Replaced the course detail review action with Material 3 `ExtendedFloatingActionButton`, using the selected theme's primary/on-primary colors.
- Show it only on 課程評價, after the course loads, including courses without reviews. Removed the duplicate empty-state action and course-detail toolbar review action.
- Fixed `rememberIsScrollingUp` to observe scroll changes without mutating derived state: scrolling down collapses to a labelled pencil icon, scrolling up or reaching the top expands, and stopping retains the current state.
- Added stable review item keys and bottom list clearance so the final review remains reachable beneath the FAB.
- Validation: `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleDebug :app:testDebugUnitTest --offline --console=plain -q` passed. Verified the installed and built APK certificates match, then installed the debug update on Waydroid (`192.168.240.112:5555`) without uninstalling or clearing app data.
- ARTEMIS hierarchy/screenshot inspection with ADB interactions verified ACT1110's empty review action and BUS1102's populated list: down-scroll collapse persists while idle, up-scroll expansion, no review action on grade distribution/past papers/teaching records, and returning to reviews restores its action and scroll position. Tapping the FAB opens the review form prefilled with BUS1102; no review was submitted.
- Screenshots: `docs/verification/2026-10-09/course-fab-expanded.png`, `course-fab-collapsed.png`, and `course-fab-other-tab.png`. Physical Samsung/Pixel checks were not performed in this task.

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
