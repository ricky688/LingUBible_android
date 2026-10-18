package com.lingubible.app.domain.model

import kotlinx.serialization.Serializable
import androidx.annotation.Keep

// Navigation resolves enum arguments by their original class name in optimized builds.
@Keep
@Serializable
enum class InformationPage(val zh: String, val en: String) {
    FAQ("常見問題", "FAQ"), CONTACT("聯絡我們", "Contact"), TERMS("服務條款", "Terms of Service"),
    PRIVACY("隱私政策", "Privacy Policy"), CHANGELOG("Android 更新日誌", "Android Changelog");
    fun title(isZh: Boolean) = if (isZh) zh else en
}

@Serializable
internal data class InformationText(val zh: String, val en: String) {
    fun primary(isZh: Boolean) = if (isZh) zh else en
    fun secondary(isZh: Boolean) = if (isZh) en else zh
}
@Serializable
internal data class InformationSection(val id: String, val title: InformationText, val paragraphs: List<InformationText>)
@Serializable
internal data class InformationDocument(val title: InformationText, val subtitle: InformationText, val updated: InformationText, val sections: List<InformationSection>)

internal val ANDROID_FAQ = listOf(
    InformationSection("account", InformationText("哪些功能需要登入？", "Which features need sign-in?"), listOf(InformationText("瀏覽課程、講師及公開評價毋須登入。撰寫評價、帳戶收藏及受保護教材需要嶺南帳戶，可使用電郵或 Google 登入。", "Browse courses, instructors and public reviews as a guest. Writing reviews, account favourites and protected materials need a Lingnan account; use email or Google sign-in."))),
    InformationSection("recovery", InformationText("忘記密碼怎麼辦？", "How do I recover my password?"), listOf(InformationText("在登入頁選擇「忘記密碼」，完成安全驗證後接收電郵。開啟或貼上重設連結，再輸入新密碼兩次。連結一小時後失效，且只能使用一次。Google 登入密碼由 Google 管理。", "Choose Forgot password on the sign-in screen, complete verification and check your email. Open or paste the reset link, then enter a new password twice. Links expire in one hour and work once. Google manages your Google sign-in password."))),
    InformationSection("search", InformationText("如何尋找課程和講師？", "How do I find courses and instructors?"), listOf(InformationText("分別使用課程列表及講師名錄的搜尋與篩選功能。課程頁的任教記錄可直接開啟講師的學生評價。", "Use search and filters in Courses or Instructors. Tap a teacher in a course's teaching records to open their student reviews."))),
    InformationSection("reviews", InformationText("如何撰寫、修改和刪除評價？", "How do I write, edit or delete a review?"), listOf(InformationText("在課程評價分頁點按「撰寫評價」，選擇課程與學期，填寫並預覽後自行提交。草稿按帳戶保存七天。用戶設定的「我的評價」可修改或刪除自己的評價。", "Choose Write review from the course Reviews tab, select a course and term, complete the form and preview before publishing. Account-specific drafts last seven days. Edit or delete your own reviews in User Settings → My Reviews."))),
    InformationSection("limits", InformationText("評價數量有甚麼限制？", "What are the review limits?"), listOf(InformationText("每學期最多七篇，每門課通常一篇；若第一篇的成績為 F，重修後可再提交一篇，每門課最多兩篇。系統會在提交前檢查資格。", "Up to seven reviews per term. Normally one per course; if your first grade is F, you may add one after retaking, up to two per course. Eligibility is checked before submission."))),
    InformationSection("privacy", InformationText("匿名評價和資料私隱如何處理？", "How do anonymity and privacy work?"), listOf(InformationText("發表時可選擇匿名；評價仍與帳戶關聯，以支援資格檢查及管理自己的內容。請閱讀隱私政策，勿在評價中填寫私人聯絡資料。", "You can publish anonymously. Reviews remain associated with your account for eligibility and managing your content. Read the Privacy Policy and avoid putting private contact details in reviews."))),
    InformationSection("materials", InformationText("如何下載歷屆試題和教材？", "How do I download papers and materials?"), listOf(InformationText("開啟課程的歷屆試題或學習教材分頁，選取檔案並下載。單個檔案保持原格式，多個檔案匯出為 ZIP。受保護檔案需要登入及伺服器存取權。", "Open Past Papers or Study Materials on a course, select files and download. A single file keeps its format; multiple files export as a ZIP. Protected files require sign-in and server access permission."))),
    InformationSection("backup", InformationText("如何保存課表和 GPA 資料？", "How do I save my timetable and GPA data?"), listOf(InformationText("課表可分享連結、匯出 PNG/PDF 或加入系統日曆。GPA 計算機可備份、還原及匯出試算表；請自行妥善保存匯出的檔案。", "Share a timetable link, export PNG/PDF or add meetings to the system calendar. The GPA calculator supports backup, restore and spreadsheet export. Keep exported files in a safe place."))),
    InformationSection("support", InformationText("如何回報問題或申請刪除資料？", "How do I report a problem or request data deletion?"), listOf(InformationText("使用此 App 的「聯絡我們」表格，或電郵 support@lingubible.com。回報錯誤時請提供 Android 版本、App 版本及重現步驟，勿提供密碼或重設連結。", "Use the app's Contact form or email support@lingubible.com. For bugs, include Android/app versions and reproduction steps. Never send passwords or reset links.")))
)

internal val ANDROID_CHANGELOG = listOf(
    InformationSection("2026-10-16", InformationText("2026-10-16 · 本地客戶端更新", "2026-10-16 · Local client update"), listOf(
        InformationText("新增完整講師任教篩選與成績分佈、課程與講師分享連結，以及可清除的搜尋記錄。", "Added complete instructor history filters and grade distributions, course/instructor sharing links, and removable recent searches."),
        InformationText("新增每學期課表標題、色彩、內容及顯示範圍自訂，並支援匯出解像度與日曆日期設定。", "Added per-term timetable title, color, field and range customization, export resolutions and calendar date settings."),
        InformationText("指南導覽按鈕固定在底部；捲動邊緣漸變在尚有內容時保留。", "Guide navigation stays at the bottom; scroll fades remain while content is available."),
        InformationText("新增原生首次啟動指南，可從設定重看七個主題。", "Added a native first-launch guide with seven topics and replay from Settings."),
        InformationText("新增原生密碼找回、重設及安全驗證流程。", "Added native password recovery/reset forms with security verification."),
        InformationText("新增原生常見問題、聯絡、服務條款、隱私政策及 Android 專用更新日誌。", "Added native FAQ, contact, terms, privacy and an Android-only changelog."))),
    InformationSection("recent-client", InformationText("近期 Android 客戶端改進", "Recent Android client improvements"), listOf(
        InformationText("評價撰寫、預覽、草稿、瀏覽篩選，以及我的評價修改與刪除。", "Review writing, preview, drafts, browsing filters, and editing/deleting your own reviews."),
        InformationText("帳戶收藏、課程與講師篩選、閱讀清單、教材及歷屆試題下載。", "Account favourites, course/instructor filters, reading lists and material/past-paper downloads."),
        InformationText("課表復原與重做、分享及匯入、單次課堂日曆匯出和 PNG/PDF 匯出。", "Timetable undo/redo, sharing/import, individual meeting calendar export and PNG/PDF export."),
        InformationText("GPA 備份與匯出、Material 3 按壓形狀動畫、按鈕觸感及捲動邊緣漸變。", "GPA backup/export, Material 3 press-shape motion, button haptics and scroll-edge fades.")))
)
