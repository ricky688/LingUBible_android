package com.lingubible.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.*
import com.lingubible.app.ui.viewmodels.CourseDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseCode: String,
    onNavigateBack: () -> Unit,
    onNavigateToWriteReview: () -> Unit,
    viewModel: CourseDetailViewModel = koinViewModel()
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFavorited by remember { mutableStateOf(false) }
    val tabs = listOf("課程評價", "成績分佈", "歷屆試題", "任教記錄")

    val reviewsListState = rememberLazyListState()
    val isScrollingUp by rememberIsScrollingUp(reviewsListState)

    LaunchedEffect(courseCode) {
        viewModel.loadCourseDetail(courseCode)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        floatingActionButton = {
            if (uiState.reviews.isNotEmpty()) {
                ExpressiveReviewFab(
                    onClick = onNavigateToWriteReview,
                    expanded = isScrollingUp,
                    text = "撰寫評價",
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp, end = 8.dp)
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            M3LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                message = "正在載入課程詳情..."
            )
        } else {
            val course = uiState.course
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header Info Card matching Web's transparent-info-card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = courseCode,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Credit Badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = BadgeTheme.FacultyBgLight,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${course?.credits ?: 3} 學分",
                                        color = BadgeTheme.FacultyTextLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Favorite Button
                                FavoriteButton(
                                    isFavorited = isFavorited,
                                    onToggle = { isFavorited = !isFavorited },
                                    size = 22.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title
                        Text(
                            text = course?.titleEn ?: courseCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!course?.titleZh.isNullOrBlank()) {
                            Text(
                                text = course!!.titleZh,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rating & Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val rating = course?.avgRating ?: 0.0
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(getRatingGradientColor(rating))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (rating > 0) String.format("%.1f ★", rating) else "未評分",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "${course?.reviewCount ?: 0} 則評價",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Average GPA Display
                            AverageGPADisplay(
                                gpaString = if (!course?.avgGrade.isNullOrBlank()) course!!.avgGrade else "3.42",
                                reviewCount = course?.reviewCount ?: 0
                            )
                        }
                    }
                }

                // Material 3 Expressive Connected Button Group for Sections
                val sectionItems = remember(tabs) {
                    tabs.mapIndexed { index, title ->
                        M3ButtonGroupItem(
                            label = title,
                            onClick = { selectedTab = index }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ScrollableM3ButtonGroup(
                        selectedIndex = selectedTab,
                        items = sectionItems,
                        modifier = Modifier.fillMaxWidth(),
                        height = 38.dp,
                        spacing = 5.dp
                    )
                }

                // Tab Content
                when (selectedTab) {
                    0 -> {
                        // Reviews Tab
                        if (uiState.reviews.isEmpty()) {
                            EmptyState(
                                message = "此課程尚無評價，快來搶先分享吧！",
                                onActionClick = onNavigateToWriteReview,
                                actionText = "搶先評價 Be First to Review"
                            )
                        } else {
                            LazyColumn(
                                state = reviewsListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .mouseScrollbar(reviewsListState),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.reviews) { review ->
                                    ReviewCard(
                                        review = review,
                                        onVote = { voteType ->
                                            viewModel.voteReview(review.id, voteType)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Grades Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                GradeDistributionChart(distribution = uiState.gradeDistribution)
                            }
                        }
                    }
                    2 -> {
                        // Past Papers Tab
                        if (uiState.pastPapers.isEmpty()) {
                            EmptyState(message = "此課程暫無歷屆試題。")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.pastPapers) { paper ->
                                    PastPaperCard(paper = paper)
                                }
                            }
                        }
                    }
                    3 -> {
                        // Teaching Records Tab
                        if (uiState.teachingRecords.isEmpty()) {
                            EmptyState(message = "暫無任教記錄。")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.teachingRecords) { record ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                val instText = if (record.instructorNameZh.isNotBlank()) {
                                                    "${record.instructorName} (${record.instructorNameZh})"
                                                } else {
                                                    record.instructorName
                                                }
                                                Text(
                                                    text = instText,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${record.term} (${record.academicYear})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (record.section.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = BadgeTheme.DepartmentBgLight,
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "Sec: ${record.section}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = BadgeTheme.DepartmentTextLight,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    onActionClick: (() -> Unit)? = null,
    actionText: String = "搶先評價 Be First to Review"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ExpressiveInlineReviewButton(
                onClick = onActionClick,
                text = actionText
            )
        }
    }
}
