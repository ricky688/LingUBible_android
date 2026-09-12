package com.lingubible.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Palette
import com.lingubible.app.core.settings.AppLanguage
import kotlinx.serialization.Serializable


@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data class AcademicTools(val initialTab: Int = 0, val fromHome: Boolean = false) : Screen

    @Serializable
    data object Courses : Screen

    @Serializable
    data class CourseDetail(val courseCode: String) : Screen

    @Serializable
    data object Planner : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object GpaHons : Screen

    @Serializable
    data object Instructors : Screen

    @Serializable
    data class InstructorDetail(val name: String) : Screen

    @Serializable
    data object Reviews : Screen

    @Serializable
    data class WriteReview(val courseCode: String? = null) : Screen

    @Serializable
    data class Auth(val mode: String = "login") : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object Settings : Screen
}

enum class BottomNavDestination(
    val screen: Screen,
    val titleZh: String,
    val titleEn: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    EXPLORE(
        screen = Screen.Home,
        titleZh = "探索",
        titleEn = "Explore",
        unselectedIcon = Icons.Outlined.Explore,
        selectedIcon = Icons.Filled.Explore
    ),
    ACADEMIC_TOOLS(
        screen = Screen.AcademicTools(0),
        titleZh = "學業工具",
        titleEn = "Academic Tools",
        unselectedIcon = Icons.Outlined.School,
        selectedIcon = Icons.Filled.School
    ),
    ACCOUNT(
        screen = Screen.Profile,
        titleZh = "帳戶",
        titleEn = "Account",
        unselectedIcon = Icons.Outlined.AccountCircle,
        selectedIcon = Icons.Filled.AccountCircle
    );

    fun label(language: AppLanguage): String = when (language) {
        AppLanguage.ZH_TW -> titleZh
        AppLanguage.EN -> titleEn
    }
}

enum class DrawerCategory(val titleZh: String, val titleEn: String) {
    CORE("核心探索", "EXPLORE"),
    TOOLS("學術工具", "ACADEMIC TOOLS"),
    ACCOUNT("帳戶設定", "ACCOUNT")
}

enum class DrawerNavDestination(
    val screen: Screen,
    val titleZh: String,
    val titleEn: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val category: DrawerCategory,
    val badgeText: String? = null
) {
    HOME(
        screen = Screen.Home,
        titleZh = "首頁",
        titleEn = "Home",
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        category = DrawerCategory.CORE
    ),
    COURSES(
        screen = Screen.Courses,
        titleZh = "課程列表",
        titleEn = "Courses",
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        category = DrawerCategory.CORE
    ),
    INSTRUCTORS(
        screen = Screen.Instructors,
        titleZh = "講師名錄",
        titleEn = "Instructors",
        unselectedIcon = Icons.Filled.School,
        selectedIcon = Icons.Filled.School,
        category = DrawerCategory.CORE
    ),
    REVIEWS(
        screen = Screen.Reviews,
        titleZh = "課程評價",
        titleEn = "Reviews",
        unselectedIcon = Icons.Outlined.RateReview,
        selectedIcon = Icons.Filled.RateReview,
        category = DrawerCategory.CORE
    ),
    PLANNER(
        screen = Screen.Planner,
        titleZh = "選課排程",
        titleEn = "Timetable Planner",
        unselectedIcon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Filled.CalendarMonth,
        category = DrawerCategory.TOOLS
    ),
    CALENDAR(
        screen = Screen.Calendar,
        titleZh = "校歷日程",
        titleEn = "School Calendar",
        unselectedIcon = Icons.Outlined.DateRange,
        selectedIcon = Icons.Filled.DateRange,
        category = DrawerCategory.TOOLS
    ),
    GPA_HONS(
        screen = Screen.GpaHons,
        titleZh = "GPA 與榮譽",
        titleEn = "GPA & Honours",
        unselectedIcon = Icons.Outlined.Calculate,
        selectedIcon = Icons.Filled.Calculate,
        category = DrawerCategory.TOOLS
    ),
    PROFILE(
        screen = Screen.Profile,
        titleZh = "個人檔案",
        titleEn = "Profile",
        unselectedIcon = Icons.Outlined.AccountCircle,
        selectedIcon = Icons.Filled.AccountCircle,
        category = DrawerCategory.ACCOUNT
    ),
    SETTINGS(
        screen = Screen.Settings,
        titleZh = "外觀自訂",
        titleEn = "Appearance & Settings",
        unselectedIcon = Icons.Outlined.Palette,
        selectedIcon = Icons.Filled.Palette,
        category = DrawerCategory.ACCOUNT
    )
}
