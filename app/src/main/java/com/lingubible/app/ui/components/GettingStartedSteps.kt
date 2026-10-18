package com.lingubible.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.lingubible.app.core.navigation.Screen

internal data class GuideText(val zh: String, val en: String) {
    fun primary(isZh: Boolean) = if (isZh) zh else en
    fun secondary(isZh: Boolean) = if (isZh) en else zh
}
internal data class GuideLink(val label: GuideText, val destination: Screen)
internal data class GuideStep(val id: String, val title: GuideText, val icon: ImageVector,
    val description: GuideText, val bullets: List<GuideText>, val links: List<GuideLink>)

internal val GETTING_STARTED_STEPS = listOf(
    GuideStep("welcome", GuideText("認識 LingUBible", "Welcome to LingUBible"), Icons.Default.Explore,
        GuideText("由嶺南學生為嶺南學生打造的選課平台。", "Course information and student experiences for the Lingnan community."),
        listOf(
            GuideText("從探索頁進入課程列表或講師名錄，搜尋課程代碼、名稱或講師。", "Open Courses or Instructors from Discover and search by code or name."),
            GuideText("公開評價、課程大綱及校曆毋須登入即可瀏覽。", "Browse public reviews, syllabi and the academic calendar without signing in."),
            GuideText("登入後可撰寫評價、收藏課程與講師，以及下載學習資料。", "Sign in to write reviews, save favourites and download course resources.")
        ), listOf(GuideLink(GuideText("探索課程", "Browse courses"), Screen.Courses))),
    GuideStep("reviews", GuideText("課程與講師評價", "Course & instructor reviews"), Icons.Default.RateReview,
        GuideText("選科前先了解同學的修讀體驗。", "Read classmates’ experiences before choosing courses."),
        listOf(
            GuideText("比較工作量、難度、實用度及成績分佈。", "Compare workload, difficulty, usefulness and grade distributions."),
            GuideText("按學系、學期或教學語言篩選，並調整排序。", "Filter by department, term or teaching language and choose a sort order."),
            GuideText("在課程的任教記錄點按講師，即可前往其學生評價。", "Tap a teacher in a course’s teaching records to jump to their student reviews.")
        ), listOf(GuideLink(GuideText("講師名錄", "Instructors"), Screen.Instructors), GuideLink(GuideText("最新評價", "Latest reviews"), Screen.Reviews))),
    GuideStep("materials", GuideText("課程大綱與歷屆試題", "Syllabi & past papers"), Icons.Default.Description,
        GuideText("在課程頁集中查看閱讀書目及學習資料。", "Find readings and study resources on each course page."),
        listOf(
            GuideText("開啟課程大綱，了解課程內容及評核方式。", "Open the syllabus to check course content and assessment details."),
            GuideText("歷屆試題可按學年與講師篩選；講師未知時會清楚標示。", "Filter past papers by year and instructor; unknown metadata is labelled."),
            GuideText("點按試題列檢視 PDF，勾選多份資料可匯出 ZIP。", "Tap a paper to view its PDF, or select several files to save a ZIP."),
            GuideText("下載需登入，並由你選擇 Android 儲存位置。", "Downloads require sign-in; Android lets you choose where to save them.")
        ), listOf(GuideLink(GuideText("尋找課程資料", "Find course resources"), Screen.Courses))),
    GuideStep("planner", GuideText("選課排程", "Timetable planner"), Icons.Default.CalendarMonth,
        GuideText("選擇班別，即時預覽每週課表與撞堂情況。", "Choose sections and preview your week with conflict warnings."),
        listOf(
            GuideText("在「加選」搜尋 CRN、課程或講師，點按班別加入課表。", "Use Browse to search by CRN, course or instructor and add a section."),
            GuideText("可復原／重做，並點按課堂查看時間、地點與學分。", "Undo or redo changes; tap a meeting for its time, location and credits."),
            GuideText("分享或匯入課表連結；亦可匯出日曆、PNG 圖片或 PDF。", "Share or import timetable links, or export calendar events, PNG images and PDFs.")
        ), listOf(GuideLink(GuideText("開啟選課排程", "Open planner"), Screen.AcademicTools(0)))),
    GuideStep("gpa", GuideText("GPA 與榮譽", "GPA & honours"), Icons.Default.Calculate,
        GuideText("試算成績與目標，了解自己的修讀進度。", "Explore your grades and targets as you plan your studies."),
        listOf(
            GuideText("輸入學期、課程、學分與等第，查看學期及累積 GPA。", "Enter terms, courses, credits and grades for term and cumulative GPA."),
            GuideText("設定目標榮譽，並在統計頁查看歷年一級榮譽比例。", "Set an honours target and explore historical First Class statistics."),
            GuideText("計算器資料儲存於本機；使用備份與還原保留手動輸入內容。", "Calculator entries are saved on this device; use backup and restore to keep a copy.")
        ), listOf(GuideLink(GuideText("開啟 GPA 工具", "Open GPA tools"), Screen.AcademicTools(2)))),
    GuideStep("calendar", GuideText("大學校曆", "Academic calendar"), Icons.Default.Event,
        GuideText("集中查看開學、加退選、考試與假期日期。", "Keep track of term dates, add/drop, exams and holidays."),
        listOf(
            GuideText("切換月份、點按日期查看當天事項，或跳到學期開始。", "Switch months, tap a date for its events, or jump to the start of term."),
            GuideText("事項以類別顏色區分，亦可調整顯示類別。", "Use category colours and filters to find relevant events."),
            GuideText("重要日期請同時留意大學的正式公布及最新安排。", "Check university announcements for the latest official dates and arrangements.")
        ), listOf(GuideLink(GuideText("開啟校曆", "Open calendar"), Screen.AcademicTools(1)))),
    GuideStep("contribute", GuideText("撰寫評價與個人化", "Contribute & personalise"), Icons.Default.Edit,
        GuideText("分享自己的修讀體驗，幫助下一位同學。", "Share your experience to help other students choose."),
        listOf(
            GuideText("登入後以分步表單撰寫評價，送出前可預覽；草稿會儲存於本機。", "Sign in to write a review, preview it before submitting and keep a local draft."),
            GuideText("發表前可選擇匿名；也可收藏常用課程與講師。", "Choose whether to post anonymously and save your favourite courses and instructors."),
            GuideText("用戶設定可切換語言、主題與觸感回饋，亦可隨時重看本指南。", "Use Settings for language, appearance and haptics, and reopen this guide any time.")
        ), listOf(GuideLink(GuideText("撰寫評價", "Write a review"), Screen.WriteReview()), GuideLink(GuideText("我的收藏", "My favourites"), Screen.Favorites)))
)

internal fun shouldShowGettingStarted(hasSeen: Boolean, isHome: Boolean, keyboardVisible: Boolean) =
    !hasSeen && isHome && !keyboardVisible
