package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import com.lingubible.app.ui.common.hapticClick

import com.lingubible.app.core.navigation.screenContentPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.common.rememberCardPressShape
import com.lingubible.app.ui.common.LocalCardCornerRadius
import com.lingubible.app.ui.components.*
import com.lingubible.app.ui.viewmodels.AuthViewModel
import com.lingubible.app.ui.viewmodels.CoursesViewModel
import com.lingubible.app.ui.viewmodels.HomeViewModel
import com.lingubible.app.ui.viewmodels.InstructorsViewModel
import com.lingubible.app.ui.viewmodels.ReviewsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

enum class HomeFeaturedTab(val title: String, val titleEn: String) {
    POPULAR_COURSES("熱門課程", "Popular Courses"),
    POPULAR_INSTRUCTORS("熱門講師", "Popular Instructors"),
    TOP_COURSES("最高評分課程", "Top Courses"),
    TOP_INSTRUCTORS("最高評分講師", "Top Instructors")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCourse: (String) -> Unit,
    onNavigateToInstructor: (String) -> Unit,
    onNavigateToWriteReview: (String) -> Unit,
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    homeViewModel: HomeViewModel = koinViewModel(),
    coursesViewModel: CoursesViewModel = koinViewModel(),
    reviewsViewModel: ReviewsViewModel = koinViewModel(parameters = { org.koin.core.parameter.parametersOf(false, 5) }),
    instructorsViewModel: InstructorsViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    val isDark = isAppDarkTheme()
    val homeState by homeViewModel.uiState.collectAsState()
    val coursesState by coursesViewModel.uiState.collectAsState()
    val reviewsState by reviewsViewModel.uiState.collectAsState()
    val instructorsState by instructorsViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(HomeFeaturedTab.POPULAR_COURSES) }

    // Favorites local state

    // Rolling text actions
    val tickerTexts = if (isZh) {
        listOf("探索課程", "集思廣益", "明辨是非", "分享心得")
    } else {
        listOf("Explore Courses", "Share Insights", "Compare Reviews", "Plan Better")
    }

    val listState = rememberLazyListState()

    var manualRefreshing by remember { mutableStateOf(false) }
    val isAnyLoading = homeState.isLoading || coursesState.isLoading || instructorsState.isLoading || reviewsState.isLoading
    val isRefreshing = manualRefreshing && isAnyLoading

    LaunchedEffect(isAnyLoading) {
        if (!isAnyLoading) {
            manualRefreshing = false
        }
    }

    CompositionLocalProvider(LocalCardCornerRadius provides 20.dp) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Organic blurred floating circles in background
        FloatingCircles(
            modifier = Modifier.fillMaxSize()
        )

        M3PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                manualRefreshing = true
                homeViewModel.loadStats()
                coursesViewModel.loadCourses()
                instructorsViewModel.loadInstructors()
                reviewsViewModel.loadReviews()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            FadingLazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .mouseScrollbar(listState),
                contentPadding = screenContentPadding(top = 8.dp)
            ) {

                // ==========================================
                // 1. Hero Section
                // ==========================================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Neon / Gradient title
                        Text(
                            text = if (isZh) "嶺南大學課程評價平台" else "Lingnan University Course Review Platform",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "LingUBible",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Reg Bible Tagline
                        Text(
                            text = if (isZh) "Reg科聖經" else "Course Registration Bible",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle
                        Text(
                            text = if (isZh) "博訪博覽，慎選課席。" else "Explore widely, choose wisely.",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rolling Text Ticker: 來這裡你可以 [探索課程]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isZh) "來這裡你可以 " else "Here you can ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            RollingText(
                                texts = tickerTexts,
                                intervalMillis = 2400L
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // CTA Button with shared shape feedback and haptics
                        val ctaInteractionSource = remember { MutableInteractionSource() }
                        val ctaCorner by AppButtonDefaults.pressCorner(ctaInteractionSource, 30.dp)

                        Surface(
                            modifier = Modifier
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(ctaCorner),
                                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                )
                                .clip(RoundedCornerShape(ctaCorner))
                                .clickable(
                                    interactionSource = ctaInteractionSource,
                                    indication = null, onClick = hapticClick {
                                    onNavigateToCourse("")
                                }),
                            shape = RoundedCornerShape(ctaCorner),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 28.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isZh) {
                                            if (authState.currentUser != null) "探索課程" else "立即開始"
                                        } else {
                                            if (authState.currentUser != null) "Explore Courses" else "Get Started"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Go",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }


                // ==========================================
                // 2. Stats Section (4 Cards Grid)
                // ==========================================
                item {
                    val stats = homeState.stats
                    val isStatsLoading = homeState.isLoading && !homeState.hasLoadedStats

                    val studentsDisplay = if (stats.verifiedStudentsCount > 0) "${stats.verifiedStudentsCount}" else if (authState.currentUser != null) "1" else "--"
                    val studentsChange = if (stats.verifiedStudentsLast30Days > 0) "+${stats.verifiedStudentsLast30Days}" else "--"

                    val reviewsDisplay = if (stats.reviewsCount > 0) "${stats.reviewsCount}" else if (reviewsState.reviews.isNotEmpty()) "${reviewsState.reviews.size}" else "--"
                    val reviewsChange = if (stats.reviewsLast30Days > 0) "+${stats.reviewsLast30Days}" else "--"

                    val coursesDisplay = if (stats.coursesCount > 0) "${stats.coursesCount}" else if (coursesState.courses.isNotEmpty()) "${coursesState.courses.size}" else "--"
                    val coursesChange = if (stats.coursesLast30Days > 0) "+${stats.coursesLast30Days}" else "--"

                    val instructorsDisplay = if (stats.instructorsCount > 0) "${stats.instructorsCount}" else if (instructorsState.instructors.isNotEmpty()) "${instructorsState.instructors.size}" else "--"
                    val instructorsChange = if (stats.instructorsLast30Days > 0) "+${stats.instructorsLast30Days}" else "--"

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatsCard(
                                icon = Icons.Filled.VerifiedUser,
                                title = if (isZh) "認證學生" else "Verified Students",
                                value = studentsDisplay,
                                change = studentsChange,
                                isLoading = isStatsLoading,
                                modifier = Modifier.weight(1f),
                                onClick = {}
                            )
                            StatsCard(
                                icon = Icons.Filled.Star,
                                title = if (isZh) "課程評價" else "Course Reviews",
                                value = reviewsDisplay,
                                change = reviewsChange,
                                isLoading = isStatsLoading,
                                modifier = Modifier.weight(1f),
                                onClick = {}
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatsCard(
                                icon = Icons.Filled.Book,
                                title = if (isZh) "涵蓋課程" else "Courses Covered",
                                value = coursesDisplay,
                                change = coursesChange,
                                isLoading = isStatsLoading,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToCourse("") }
                            )
                            StatsCard(
                                icon = Icons.Filled.Person,
                                title = if (isZh) "評價講師" else "Instructors",
                                value = instructorsDisplay,
                                change = instructorsChange,
                                isLoading = isStatsLoading,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToInstructor("") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isZh) "過去 30 天變化趨勢" else "30-day trend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ==========================================
                // 2.5 Quick Academic Tools Section
                // ==========================================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = if (isZh) "學業工具 Academic Tools" else "Academic Tools",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Timetable Planner Card
                            val plannerInteraction = remember { MutableInteractionSource() }
                            Surface(
                                onClick = hapticClick(onNavigateToPlanner),
                                interactionSource = plannerInteraction,
                                modifier = Modifier
                                    .weight(1f)
                                    .pointerHoverIcon(PointerIcon.Hand),
                                shape = rememberCardPressShape(plannerInteraction),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
                                ),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.6f else 0.5f),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (isZh) "選課排程" else "Timetable",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isZh) {
                                            Text(
                                                text = "Timetable",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // School Calendar Card
                            val calendarInteraction = remember { MutableInteractionSource() }
                            Surface(
                                onClick = hapticClick(onNavigateToCalendar),
                                interactionSource = calendarInteraction,
                                modifier = Modifier
                                    .weight(1f)
                                    .pointerHoverIcon(PointerIcon.Hand),
                                shape = rememberCardPressShape(calendarInteraction),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
                                ),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                Color(0xFF3B82F6).copy(alpha = if (isDark) 0.2f else 0.12f),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DateRange,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (isZh) "校歷日程" else "Calendar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isZh) {
                                            Text(
                                                text = "Calendar",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. Featured Tabs (Frosted Glass Sticky Header)
                // ==========================================
                stickyHeader(key = "featured_tabs") {
                    val tabItems = remember(isZh) {
                        HomeFeaturedTab.entries.map { tab ->
                            M3ButtonGroupItem(
                                label = if (isZh) tab.title else tab.titleEn,
                                onClick = { selectedTab = tab }
                            )
                        }
                    }

                    FrostedGlassHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        borderBottomOnly = false
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            ScrollableM3ButtonGroup(
                                selectedIndex = selectedTab.ordinal,
                                items = tabItems,
                                modifier = Modifier.fillMaxWidth(),
                                height = 38.dp,
                                spacing = 6.dp,
                                fillWidthWhenFits = true
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Tab Content: Popular Courses, Instructors, Top Courses, Top Instructors
                item {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            ((slideInHorizontally(
                                animationSpec = AppMotion.spatial(),
                                initialOffsetX = { fullWidth -> direction * fullWidth / 4 }
                            ) + fadeIn(animationSpec = AppMotion.effects())) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = AppMotion.spatial(),
                                    targetOffsetX = { fullWidth -> -direction * fullWidth / 4 }
                                ) + fadeOut(animationSpec = AppMotion.effects())))
                                .using(SizeTransform(clip = false) { _, _ ->
                                    AppMotion.spatial()
                                })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "homeFeaturedCategoryTransition"
                    ) { currentTab ->
                        when (currentTab) {
                            HomeFeaturedTab.POPULAR_COURSES -> {
                                val courses = coursesState.courses
                                if (coursesState.isLoading && courses.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        M3LoadingIndicator()
                                    }
                                } else if (courses.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isZh) "暫無課程資料" else "No courses available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        courses.take(6).forEachIndexed { index, course ->
                                            PopularCourseCard(
                                                course = course,
                                                groupIndex = index,
                                                groupSize = minOf(6, courses.size),

                                                onClick = { onNavigateToCourse(course.code) }
                                            )
                                        }
                                    }
                                }
                            }
                            HomeFeaturedTab.POPULAR_INSTRUCTORS -> {
                                val instructors = instructorsState.instructors
                                if (instructorsState.isLoading && instructors.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        M3LoadingIndicator()
                                    }
                                } else if (instructors.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isZh) "暫無講師資料" else "No instructors available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        instructors.take(6).forEachIndexed { index, instructor ->
                                            PopularInstructorCard(
                                                instructor = instructor,
                                                groupIndex = index,
                                                groupSize = minOf(6, instructors.size),

                                                onClick = { onNavigateToInstructor(instructor.name) }
                                            )
                                        }
                                    }
                                }
                            }
                            HomeFeaturedTab.TOP_COURSES -> {
                                val topCourses = coursesState.courses.sortedByDescending { it.avgRating }
                                if (coursesState.isLoading && topCourses.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        M3LoadingIndicator()
                                    }
                                } else if (topCourses.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isZh) "暫無課程資料" else "No courses available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        topCourses.take(6).forEachIndexed { index, course ->
                                            PopularCourseCard(
                                                course = course,
                                                groupIndex = index,
                                                groupSize = minOf(6, topCourses.size),

                                                onClick = { onNavigateToCourse(course.code) }
                                            )
                                        }
                                    }
                                }
                            }
                            HomeFeaturedTab.TOP_INSTRUCTORS -> {
                                val topInstructors = instructorsState.instructors.sortedByDescending { it.avgRating }
                                if (instructorsState.isLoading && topInstructors.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        M3LoadingIndicator()
                                    }
                                } else if (topInstructors.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isZh) "暫無講師資料" else "No instructors available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        topInstructors.take(6).forEachIndexed { index, instructor ->
                                            PopularInstructorCard(
                                                instructor = instructor,
                                                groupIndex = index,
                                                groupSize = minOf(6, topInstructors.size),

                                                onClick = { onNavigateToInstructor(instructor.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ==========================================
                // 4. Latest Reviews Section
                // ==========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isZh) "最新評價" else "Latest Reviews",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isZh) {
                                Text(
                                    text = "Latest Reviews",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(shapes = AppButtonDefaults.shapes(),
                            onClick = hapticClick { onNavigateToReviews() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Text(
                                text = if (isZh) "查看更多" else "See more",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (reviewsState.isLoading && reviewsState.reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            M3LoadingIndicator()
                        }
                    }
                } else if (reviewsState.reviews.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isZh) "目前尚無最新評價" else "No reviews available yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isZh) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No reviews available yet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isZh) "成為第一位分享選課體驗與評分的同學吧！" else "Be the first to share your course experience and rating!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = hapticClick { onNavigateToWriteReview("") },
                                    shapes = AppButtonDefaults.shapes(shape = RoundedCornerShape(14.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isZh) "發表評價" else "Write a Review", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(reviewsState.reviews.take(10), key = { _, review -> review.id }) { index, review ->
                        ReviewCard(
                            review = review,
                            groupIndex = index,
                            groupSize = minOf(10, reviewsState.reviews.size),
                            onVote = { voteType ->
                                reviewsViewModel.voteReview(review.id, voteType)
                            },
                            modifier = Modifier.padding(bottom = 4.dp),
                            onClick = { onNavigateToCourse(review.courseCode) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
    }
}

