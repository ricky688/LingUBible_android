<div align="center">

<img src="../../app/src/main/res/drawable/ic_lingubible_logo.png" alt="LingUBible Logo" width="120" style="border-radius: 28px;"/>

# LingUBible Android 📱
### *嶺南大學課程與講師評價 · 官方原生 Android 應用*
### *Official Native Android Client for LingUBible*

[![English](https://img.shields.io/badge/Language-English-blue?style=for-the-badge)](../../README.md)
[![繁體中文](https://img.shields.io/badge/Language-繁體中文-red?style=for-the-badge)](README.md)

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--36)-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive%201.5.0--alpha01-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Appwrite](https://img.shields.io/badge/Backend-Appwrite%20SDK%2027.2.0-FD366E?style=flat-square&logo=appwrite&logoColor=white)](https://appwrite.io)
[![Koin](https://img.shields.io/badge/DI-Koin%204.0.0-E53935?style=flat-square)](https://insert-koin.io)
[![Tests](https://img.shields.io/badge/Tests-556%20Passing-brightgreen?style=flat-square)](https://github.com/ricky688/LingUBible_android)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](../../LICENSE)

<br/>

**LingUBible Android** 是專為嶺南大學（Lingnan University）學生量身打造的現代化原生 Android 應用程式。採用 **Jetpack Compose** 與 Google 最新 **Material 3 Expressive (M3E)** 設計語言構建，結合流暢的彈簧物理動效（Spring Motions）、毛玻璃質感（Glassmorphism）、雙語介面規範，並與 [LingUBible Web App](https://www.lingubible.com) 資料庫全面互通，為嶺大學子提供流暢的修課心得查閱、選課排程、行事曆與 GPA 榮譽規劃體驗。

[✨ 核心功能](#-核心功能-features) • [📱 介面預覽](#-介面展示-screenshots) • [🏗️ 技術架構](#%EF%B8%8F-技術架構-architecture) • [🚀 快速開始](#-快速開始-quick-start) • [🧪 測試驗證](#-測試與驗證-testing) • [📄 授權條款](#-授權條款-license)

</div>

---

## ✨ 核心功能 (Features)

### 🎓 1. 學術工具：GPA & 榮譽規劃 (GPA & Honours Planner)
- **計算器 (Calculator Tab)**：
  - **動態數據滾輪 (Numeric Roll Transitions)**：累計 GPA (cGPA)、學期 GPA、學分總計均配備直向平滑數字滾動動效 (`AnimatedContent` + Spring Physics)。
  - **榮譽等級動態橫幅**：自動計算並判定榮譽等級（甲等榮譽 First Class Honours、乙等一級、乙等二級、丙等等），支援動態勳章圖標。
  - **目標 GPA 模擬器 (Target Simulator)**：自訂目標 GPA 與剩餘學分，提供一鍵捷徑（如甲等 3.50、乙一 3.00），即時推算剩餘課程所需平均成績。
  - **多學年課程編輯器**：支援新增/刪除多學期與多學年、自動補全課程代碼、全功能 **復原 (Undo) / 重做 (Redo) / 重設 (Reset)** 歷史紀錄棧。
- **榮譽統計 (Statistics Tab)**：
  - **官方畢業數據庫**：內建嶺南大學 30 個學系/課程於 2024 與 2025 年之官方畢業數據（畢業總人數、一級榮譽人數與比例）。
  - **多維度篩選與比較**：支援依學院（文學院、商學院、社會科學院、數據科學院等）分組過濾、搜尋、年度 YoY 增減百分比對比。

### 📅 2. 學術行事曆 (Academic Calendar & Web Parity)
- **月視圖、週視圖與三日視圖**：與 Google Calendar 及 LingUBible Web 版一致的月視圖設計，單日格子直接顯示分類色彩事件標籤（Holiday、Teaching、Exam 等）。
- **完整學術日程**：即時同步嶺南大學官方重要時程（開學、Add/Drop 期、考試週、放假等），支援點擊查看詳細說明與單日事件清單。

### 🗓️ 3. 課表排程規劃 (Timetable Planner)
- **可展開式課程類別面板**：從緊湊滾動條平滑展開為多欄學院類別網格，點擊即時篩選課程。
- **衝突偵測與視覺化**：自選課程節次 (Sections)，自動檢測時段衝突，自訂學期標題與色彩標記並去除多時段重複學分計算。
- **匯出與分享**：支援匯出 PNG、PDF 及標準 `.ics` 行事曆檔案，並支援與網頁版互通的分享／匯入連結。

### 🔍 4. 探索與課程／講師庫 (Discover & Directory)
- **全方位智慧搜尋**：支援課程代碼（如 `CLC9001`、`BUS1102`）、中英文名稱、講師全名與常見學生暱稱搜尋，並記錄最近搜尋。
- **Material 3 Expressive 動效**：進入課程與講師詳情頁具備容器轉場（Container Transform）動畫、18dp 獨立懸浮卡片及 Vico 成績分佈圖表。
- **課程資源與教材**：支援必讀／推薦書單、歷屆試題與學習教材瀏覽，內建 PDF 檢視器與單檔／ZIP 批次下載。

### ✍️ 5. 真實課程評價與社群審核 (Course Reviews)
- **多維度客觀評分**：涵蓋 Workload（工作量）、Difficulty（難度）、Grade（給分寬容度）與 Teaching（教學品質），支援 0.0 / N/A。
- **社群互動與草稿**：支援實用／非實用投票、帳戶專屬 7 天評價草稿、資格檢查及個人評價編輯／刪除。

### 🔐 6. 學生驗證與隱私安全 (Authentication & Profile)
- **嶺南大學校園郵箱驗證**：原生登入與 Google 登入僅限 `@ln.hk` 學生帳號，並提供原生密碼找回與重設流程。
- **個人化與外觀設定**：自訂動物頭像、OLED 純黑與 Material You 動態配色，以及用戶設定 M3E 載入與擴散轉場動效。

---

## 📱 介面展示 (Screenshots)

<div align="center">

| GPA 計算器 (Calculator) | 目標 GPA 模擬器 (Target Simulator) |
|:---:|:---:|
| <img src="../../docs/validation/gpa/calculator.jpg" width="360" alt="Calculator View"/> | <img src="../../docs/validation/gpa/sizing-calculator.jpg" width="360" alt="Target Simulator View"/> |

| 課程與學期編輯器 (Course Editor) | 官方榮譽畢業統計 (Honours Statistics) |
|:---:|:---:|
| <img src="../../docs/validation/gpa/sizing-editor.jpg" width="360" alt="Course Editor View"/> | <img src="../../docs/validation/gpa/statistics.jpg" width="360" alt="Statistics View"/> |

| 等寬分頁與 Material 3 主題 (Themed Tabs) | 成績分佈圖表 (Grade Visualizer) |
|:---:|:---:|
| <img src="../../docs/validation/gpa/themed-tabs.jpg" width="360" alt="Themed Tabs View"/> | <img src="../../docs/validation/gpa/sizing-chart.jpg" width="360" alt="Grade Distribution Chart"/> |

</div>

---

## 🎨 設計規範：Material 3 Expressive (M3E)

LingUBible Android 遵循官方最新 **Material 3 Expressive** 標準進行設計與實現：

1. **純彈簧物理動效 (Pure Spring Motion Tokens)**：統一使用 `MotionScheme.expressive()` 與純剛度彈簧（`AppMotion`），呈現自然且富有彈性的回彈手感。
2. **表現力形狀與按鈕變形**：全應用按鈕統一支援 `8.dp` 按壓圓角形變（`AppButtonDefaults`），搭配 `18.dp` 獨立懸浮卡片與容器轉場（`ContainerTransform`）。
3. **微光毛玻璃質感 (Frosted Glassmorphism)**：半透明表面層搭配漸變高光邊框與捲動邊緣漸變（`fadingVerticalScroll` / `FadingLazyColumn`）。
4. **雙語視覺階級 (Bilingual Hierarchy)**：清晰呈現繁體中文與英文雙語介面層次。

---

## 🏗️ 技術架構 (Architecture)

本專案遵循現代 Android 開發的最佳實踐（Clean Architecture + MVVM + Repository Pattern）：

```text
LingUBible_android/
├── app/
│   ├── src/main/java/com/lingubible/app/
│   │   ├── core/                    # 核心主題、導航路由、設定、動效 Token、i18n 工具
│   │   │   ├── navigation/          # NavGraph, BottomBar, TopBar, Drawer
│   │   │   ├── settings/            # AppSettingsManager (DataStore)
│   │   │   ├── theme/               # Theme, Color, Type, AppMotion
│   │   │   └── util/                # I18n, IcsExporter
│   │   ├── data/                    # 資料層實作 (Data Sources & Repositories)
│   │   │   ├── remote/              # AppwriteClientProvider (Appwrite SDK)
│   │   │   └── repository/          # Auth, Course, Instructor, Review, Calendar, Timetable
│   │   ├── di/                      # 依賴注入 (Koin Module Definitions)
│   │   ├── domain/                  # 領域層 (Models, Repository Interfaces, Use Cases)
│   │   │   ├── calculator/          # HonoursCalculator
│   │   │   ├── model/               # Domain Entity Models
│   │   │   └── repository/          # Repository Interfaces
│   │   └── ui/                      # 表現層 (Jetpack Compose UI)
│   │       ├── common/              # 共用 Modifier 與觸感回饋工具
│   │       ├── components/          # M3E 組件 (ContainerTransform, Charts, Modals)
│   │       └── screens/             # 功能畫面 (Home, Courses, Planner, Calendar, GPA, Profile)
│   └── src/test/                    # 單元測試套件
```

### 🧰 技術棧一覽

| 領域 / 組件 | 技術選擇 | 版本 | 說明 |
|:---|:---|:---|:---|
| **語言** | Kotlin | `2.0.0` | 支援協程、高階函式與型別安全 |
| **UI 框架** | Jetpack Compose | BOM `2026.09.00` | 宣告式原生 UI 構建 |
| **設計系統** | Material 3 Expressive | `1.5.0-alpha01` | M3E 實驗性表現力組件與動效 |
| **依賴注入** | Koin | `4.0.0` | 輕量且適用於 Compose 的 DI |
| **後端服務 (BaaS)** | Appwrite Android SDK | `27.2.0` | 負責 Auth、TablesDB 資料庫、Storage 與 Functions |
| **資料視覺化** | Vico Charting | `2.0.2` | 成績分佈直方圖與趨勢分析圖表 |
| **非同步處理** | Kotlin Coroutines & Flow | `1.8.1` | 響應式狀態流與並發處理 |
| **圖片載入** | Coil Compose | `2.7.0` | 非同步圖片載入與快取 |
| **單元測試** | JUnit 4 + MockK + Turbine | 最新 | 覆蓋 GPA 演算、狀態處理與 Undo/Redo 歷史棧 |

---

## 🚀 快速開始 (Quick Start)

### 環境需求
- **Android Studio**：Ladybug (2024.2.1) 或更新版本
- **JDK**：Java 17
- **Android SDK**：
  - `compileSdk`: 36
  - `targetSdk`: 36
  - `minSdk`: 26 (Android 8.0 Oreo 以上)

### 1. 複製專案
```bash
git clone https://github.com/ricky688/LingUBible_android.git
cd LingUBible_android
```

### 2. 環境配置 (Appwrite)
專案預設已配置官方生產環境 Appwrite 端點：
- **Endpoint**: `https://appwrite.lingubible.com/v1`
- **Project ID**: `6a1097400037a55f6472`
- **Database ID**: `lingubible`

*(如需切換至自建 Appwrite 伺服器，可於 `app/build.gradle.kts` 中調整 `buildConfigField` 參數)*

### 3. 編譯與安裝 Debug APK
```bash
# 設定 Android SDK 路徑 (若環境變數已設定則可略過)
export ANDROID_HOME=/path/to/android-sdk

# 編譯 Debug 版本 APK
./gradlew :app:assembleDebug

# 透過 ADB 安裝至連接的手機或模擬器
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 測試與驗證 (Testing)

執行完整離線單元測試：
```bash
./gradlew :app:testDebugUnitTest --console=plain
```

執行 R8 Release 打包與 Google Play 規範檢查：
```bash
./gradlew :app:assembleRelease :app:bundleRelease
python3 tools/verify-play-release.py
```

---

## 🤝 參與貢獻 (Contributing)

歡迎為 LingUBible Android 貢獻程式碼或提出建議！
1. Fork 本專案至您的 GitHub 帳號。
2. 建立您的特性分支：`git checkout -b feature/awesome-feature`。
3. 遵循專案之 Material 3 Expressive 設計規範與單元測試標準。
4. 提交您的變更：`git commit -m '✨ Add awesome feature'`。
5. 推送至分支：`git push origin feature/awesome-feature`。
6. 開啟一個 Pull Request。

---

## 📄 授權條款 (License)

本專案採用 [MIT License](../../LICENSE) 條款開源授權。

---

<div align="center">

**Made with ❤️ for all Lingnan University Students**  
*Let every review be a guiding light on your learning journey.*

</div>
