package com.lingubible.app.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import com.lingubible.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.theme.*
import org.koin.compose.koinInject

/**
 * Metadata for screen titles and subtitles adhering to Material 3 Expressive
 * and bilingual hierarchy guidelines.
 */
data class TopBarTitleInfo(
    val primaryTitle: String,
    val secondarySubtitle: String,
    val key: String,
    val isHome: Boolean = false
)

enum class TransitionDirection {
    FORWARD_TAB,   // Moving to a tab with higher index
    BACKWARD_TAB,  // Moving to a tab with lower index
    ENTER_DETAIL,  // Moving into a detail screen
    POP_DETAIL     // Popping back from a detail screen
}

data class TopBarState(
    val isVisible: Boolean = true,
    val isExpanded: Boolean = true,
    val isSubBarVisible: Boolean = true,
    val academicSubTab: Int = 0,
    val setVisible: (Boolean) -> Unit = {},
    val setExpanded: (Boolean) -> Unit = {},
    val setSubBarVisible: (Boolean) -> Unit = {},
    val setAcademicSubTab: (Int) -> Unit = {}
)

val LocalTopBarState = compositionLocalOf { TopBarState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LingUBibleTopBar(
    navController: NavHostController,
    isVisible: Boolean = true,
    isExpanded: Boolean = true,
    academicSubTab: Int = 0,
    modifier: Modifier = Modifier,
    settingsManager: AppSettingsManager = koinInject()
) {
    val isDark = isAppDarkTheme()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW

    // Track tab index and transition direction for directional word animations
    var previousTabIndex by remember { mutableIntStateOf(0) }
    var wasDetail by remember { mutableStateOf(false) }
    var transitionDirection by remember { mutableStateOf(TransitionDirection.FORWARD_TAB) }

    val currentTabIndex = remember(destination) {
        when {
            destination?.hasRoute<Screen.Home>() == true -> 0
            destination?.hasRoute<Screen.AcademicTools>() == true ||
                destination?.hasRoute<Screen.Planner>() == true ||
                destination?.hasRoute<Screen.Calendar>() == true ||
                destination?.hasRoute<Screen.GpaHons>() == true -> 1
            destination?.hasRoute<Screen.Profile>() == true -> 2
            else -> -1
        }
    }
    val isCurrentDetail = currentTabIndex == -1

    LaunchedEffect(destination, currentTabIndex) {
        transitionDirection = when {
            isCurrentDetail && !wasDetail -> TransitionDirection.ENTER_DETAIL
            !isCurrentDetail && wasDetail -> TransitionDirection.POP_DETAIL
            currentTabIndex != -1 && previousTabIndex != -1 -> {
                if (currentTabIndex >= previousTabIndex) TransitionDirection.FORWARD_TAB
                else TransitionDirection.BACKWARD_TAB
            }
            else -> TransitionDirection.FORWARD_TAB
        }

        if (currentTabIndex != -1) {
            previousTabIndex = currentTabIndex
        }
        wasDetail = isCurrentDetail
    }

    // Determine current title info with full bilingual hierarchy
    val titleInfo: TopBarTitleInfo = remember(destination, navBackStackEntry, isZh, academicSubTab) {
        when {
            destination?.hasRoute<Screen.Home>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "探索" else "Explore",
                    secondarySubtitle = if (isZh) "嶺南大學課程指南・Course Discovery" else "Lingnan Course & Lecturer Guide",
                    key = "home",
                    isHome = true
                )
            destination?.hasRoute<Screen.AcademicTools>() == true ||
                destination?.hasRoute<Screen.Planner>() == true ||
                destination?.hasRoute<Screen.Calendar>() == true ||
                destination?.hasRoute<Screen.GpaHons>() == true -> {
                val effectiveSubTab = when {
                    destination?.hasRoute<Screen.Calendar>() == true -> 1
                    destination?.hasRoute<Screen.GpaHons>() == true -> 2
                    destination?.hasRoute<Screen.Planner>() == true -> 0
                    else -> academicSubTab
                }
                when (effectiveSubTab) {
                    1 -> TopBarTitleInfo(
                        primaryTitle = if (isZh) "學業工具" else "Academic Tools",
                        secondarySubtitle = if (isZh) "校歷日程・重要日期與活動" else "School Calendar & Events",
                        key = "academic_tools_calendar"
                    )
                    2 -> TopBarTitleInfo(
                        primaryTitle = if (isZh) "學業工具" else "Academic Tools",
                        secondarySubtitle = if (isZh) "GPA 與榮譽・成績試算與榮譽學位" else "GPA & Honours Simulator",
                        key = "academic_tools_gpa"
                    )
                    else -> TopBarTitleInfo(
                        primaryTitle = if (isZh) "學業工具" else "Academic Tools",
                        secondarySubtitle = if (isZh) "選課排程・個人課表與衝突檢測" else "Timetable Planner & Course Schedule",
                        key = "academic_tools_planner"
                    )
                }
            }
            destination?.hasRoute<Screen.Profile>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "帳戶與設定" else "Account & Settings",
                    secondarySubtitle = if (isZh) "個人檔案・主題外觀・語言偏好" else "Profile・Appearance・Language",
                    key = "profile"
                )
            destination?.hasRoute<Screen.Courses>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "課程列表" else "Course Directory",
                    secondarySubtitle = if (isZh) "全校博雅教育與學系專業科目" else "Browse All Lingnan University Courses",
                    key = "courses"
                )
            destination?.hasRoute<Screen.Instructors>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "講師名錄" else "Faculty & Instructors",
                    secondarySubtitle = if (isZh) "各學系教授與課程授課導師" else "Professors & Teaching Faculty",
                    key = "instructors"
                )
            destination?.hasRoute<Screen.Reviews>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "最新課程評價" else "Latest Reviews",
                    secondarySubtitle = if (isZh) "學生真實修讀心得與評分" else "Authentic Student Feedback & Ratings",
                    key = "reviews"
                )
            destination?.hasRoute<Screen.CourseDetail>() == true -> {
                val code = try {
                    navBackStackEntry?.toRoute<Screen.CourseDetail>()?.courseCode
                } catch (_: Exception) { null }
                TopBarTitleInfo(
                    primaryTitle = code ?: (if (isZh) "課程詳情" else "Course Details"),
                    secondarySubtitle = if (isZh) "課程大綱、課業負擔與評價" else "Syllabus, Workload & Student Reviews",
                    key = "course_detail_${code ?: ""}"
                )
            }
            destination?.hasRoute<Screen.InstructorDetail>() == true -> {
                val name = try {
                    navBackStackEntry?.toRoute<Screen.InstructorDetail>()?.name
                } catch (_: Exception) { null }
                TopBarTitleInfo(
                    primaryTitle = name ?: (if (isZh) "講師詳情" else "Instructor Profile"),
                    secondarySubtitle = if (isZh) "任教科目與學生綜合評價" else "Courses Taught & Student Feedback",
                    key = "instructor_detail_${name ?: ""}"
                )
            }
            destination?.hasRoute<Screen.WriteReview>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "撰寫課程評價" else "Write Review",
                    secondarySubtitle = if (isZh) "分享修讀心得，幫助嶺南同儕" else "Share Insights to Empower Peers",
                    key = "write_review"
                )
            destination?.hasRoute<Screen.Auth>() == true -> {
                val mode = try {
                    navBackStackEntry?.toRoute<Screen.Auth>()?.mode
                } catch (_: Exception) { null }
                TopBarTitleInfo(
                    primaryTitle = if (mode == "register") (if (isZh) "註冊新帳號" else "Register") else (if (isZh) "登入帳號" else "Sign In"),
                    secondarySubtitle = if (isZh) "使用嶺南大學學生身分" else "Lingnan Student Authentication",
                    key = "auth_$mode"
                )
            }
            destination?.hasRoute<Screen.Settings>() == true ->
                TopBarTitleInfo(
                    primaryTitle = if (isZh) "外觀與設定" else "Appearance & Settings",
                    secondarySubtitle = if (isZh) "色彩配置・OLED純黑・深淺主題自訂" else "Themes・OLED Black・Palettes",
                    key = "settings"
                )
            else -> TopBarTitleInfo(
                primaryTitle = "LingUBible",
                secondarySubtitle = if (isZh) "嶺南大學課程評價平台" else "Course & Lecturer Guide",
                key = "home_default",
                isHome = true
            )
        }
    }

    val isTopLevelTab = currentTabIndex != -1
    val isFromHome = try {
        destination?.hasRoute<Screen.AcademicTools>() == true &&
            navBackStackEntry?.toRoute<Screen.AcademicTools>()?.fromHome == true
    } catch (_: Exception) {
        false
    }
    val canNavigateBack = destination != null && (
        !isTopLevelTab ||
        isFromHome ||
        destination.hasRoute<Screen.Planner>() ||
        destination.hasRoute<Screen.Calendar>() ||
        destination.hasRoute<Screen.GpaHons>()
    )

    // Frosted glass styling colors & gradient palette aligned with M3
    val targetFrostedSurfaceColor = if (isDark) {
        if (isOledBlackActive()) Color(0xFF000000).copy(alpha = 0.90f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }
    val frostedSurfaceColor by animateColorAsState(
        targetValue = targetFrostedSurfaceColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "topBarFrostedSurface"
    )

    val targetGlassHighlightColor = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f) else Color(0xFFCBD5E1).copy(alpha = 0.85f)
    val glassHighlightColor by animateColorAsState(
        targetValue = targetGlassHighlightColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "topBarGlassHighlight"
    )
    val glassBorderColors = remember(glassHighlightColor) {
        listOf(glassHighlightColor.copy(alpha = 0.25f), glassHighlightColor, glassHighlightColor.copy(alpha = 0.25f))
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) + fadeOut(
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    // 1. Frosted glass translucent base background
                    drawRect(color = frostedSurfaceColor)

                    // 2. Fine specular linear gradient bottom border
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = glassBorderColors
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // 1. Compact Top Bar Row
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Expressive squircle brand logo badge on top-level tabs
                        Image(
                            painter = painterResource(id = R.drawable.ic_lingubible_logo),
                            contentDescription = "LingUBible Logo",
                            modifier = Modifier
                                .padding(start = 16.dp, end = 4.dp)
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                },
                title = {
                    AnimatedContent(
                        targetState = Pair(titleInfo, isExpanded),
                        transitionSpec = {
                            if (initialState.first == targetState.first) {
                                // When route hasn't changed (only expanded/collapsed state toggled),
                                // provide a smooth directional slide & crossfade so compact title glides in naturally
                                (fadeIn(animationSpec = tween(200)) + slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )) togetherWith
                                (fadeOut(animationSpec = tween(140)) + slideOutVertically(
                                    targetOffsetY = { -it / 3 },
                                    animationSpec = tween(140)
                                ))
                            } else {
                                when (transitionDirection) {
                                    TransitionDirection.FORWARD_TAB -> {
                                        (slideInVertically(
                                            initialOffsetY = { height -> height },
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                                        (slideOutVertically(
                                            targetOffsetY = { height -> -height },
                                            animationSpec = tween(180, easing = FastOutLinearInEasing)
                                        ) + fadeOut(animationSpec = tween(160)))
                                    }
                                    TransitionDirection.BACKWARD_TAB -> {
                                        (slideInVertically(
                                            initialOffsetY = { height -> -height },
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                                        (slideOutVertically(
                                            targetOffsetY = { height -> height },
                                            animationSpec = tween(180, easing = FastOutLinearInEasing)
                                        ) + fadeOut(animationSpec = tween(160)))
                                    }
                                    TransitionDirection.ENTER_DETAIL -> {
                                        (slideInHorizontally(
                                            initialOffsetX = { width -> (width * 0.4f).toInt() },
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                                        (slideOutHorizontally(
                                            targetOffsetX = { width -> -(width * 0.3f).toInt() },
                                            animationSpec = tween(180, easing = FastOutLinearInEasing)
                                        ) + fadeOut(animationSpec = tween(160)))
                                    }
                                    TransitionDirection.POP_DETAIL -> {
                                        (slideInHorizontally(
                                            initialOffsetX = { width -> -(width * 0.4f).toInt() },
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                                        (slideOutHorizontally(
                                            targetOffsetX = { width -> (width * 0.3f).toInt() },
                                            animationSpec = tween(180, easing = FastOutLinearInEasing)
                                        ) + fadeOut(animationSpec = tween(160)))
                                    }
                                }
                            }
                        },
                        label = "topBarTitleTransition"
                    ) { (info, expanded) ->
                        if (!expanded) {
                            // In collapsed mode, display compact two-line title in top bar across ALL screens (combining App Icon + Page Name)
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = info.primaryTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = info.secondarySubtitle.substringBefore("・"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else if (info.isHome) {
                            Text(
                                text = "LingUBible",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    val isHome = destination?.hasRoute<Screen.Home>() == true
                    val isReviewable = destination?.hasRoute<Screen.Reviews>() == true || destination?.hasRoute<Screen.CourseDetail>() == true
                    AnimatedContent(
                        targetState = Pair(isHome, isReviewable),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "topBarActionsTransition"
                    ) { (home, reviewable) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (home) {
                                IconButton(
                                    onClick = { navController.navigate(Screen.Courses) },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else if (reviewable) {
                                IconButton(
                                    onClick = {
                                        val courseCode = try {
                                            navBackStackEntry?.toRoute<Screen.CourseDetail>()?.courseCode
                                        } catch (_: Exception) { null }
                                        navController.navigate(Screen.WriteReview(courseCode))
                                    },
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Create,
                                        contentDescription = "Write Review",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            )

            // 2. Material 3 Expressive Collapsing Large Title Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + slideInVertically(
                    initialOffsetY = { -it / 3 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = shrinkVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + slideOutVertically(
                    targetOffsetY = { -it / 3 },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (canNavigateBack) 56.dp else 20.dp,
                            end = 20.dp,
                            bottom = 12.dp,
                            top = 2.dp
                        )
                ) {
                    AnimatedContent(
                        targetState = titleInfo,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )) togetherWith
                            (fadeOut(animationSpec = tween(160)) + slideOutVertically(
                                targetOffsetY = { -it / 2 },
                                animationSpec = tween(160, easing = FastOutLinearInEasing)
                            ))
                        },
                        label = "expandedTitleTransition"
                    ) { target ->
                        Column {
                            Text(
                                text = target.primaryTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (target.isHome) MaterialTheme.colorScheme.primary else (if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF0F172A)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = target.secondarySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
}
