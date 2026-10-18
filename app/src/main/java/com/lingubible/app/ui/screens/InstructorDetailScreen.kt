package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import com.lingubible.app.ui.components.FadingLazyColumn
import com.lingubible.app.ui.common.hapticClick

import com.lingubible.app.core.navigation.screenContentPadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.components.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.lingubible.app.domain.model.Instructor
import com.lingubible.app.domain.model.Review
import com.lingubible.app.domain.model.TeachingRecord
import com.lingubible.app.domain.repository.InstructorRepository
import com.lingubible.app.domain.repository.ReviewRepository
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3LoadingIndicator
import com.lingubible.app.ui.components.M3LoadingState
import com.lingubible.app.ui.components.RatingBar
import com.lingubible.app.ui.components.ReviewCard
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorDetailScreen(
    instructorName: String,
    initialTab: Int = 0,
    onNavigateBack: () -> Unit,
    showReviews: Boolean = false,
    onCourse: (String) -> Unit = {},
    instructorRepository: InstructorRepository = koinInject(),
    reviewRepository: ReviewRepository = koinInject()
) {
    val isDark = isAppDarkTheme()
    val listState = rememberLazyListState()
    var reviewsFocused by androidx.compose.runtime.saveable.rememberSaveable(instructorName, showReviews) { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var instructor by remember { mutableStateOf<Instructor?>(null) }
    var teachingRecords by remember { mutableStateOf<List<TeachingRecord>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var tab by rememberSaveable(instructorName) { mutableIntStateOf(if (showReviews) 1 else initialTab.coerceIn(0, 2)) }
    var historyFilters by rememberTeachingHistoryFilters(instructorName)
    val history = remember(teachingRecords, historyFilters) { filterTeachingHistory(teachingRecords, historyFilters) }
    val distribution = remember(reviews, instructorName, teachingRecords, historyFilters) { instructorGradeDistribution(reviews, instructorName, teachingRecords, historyFilters) }
    var historyError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var reviewFilters by com.lingubible.app.ui.components.rememberReviewFilters(instructorName)
    val filteredReviews = remember(reviews, reviewFilters) { com.lingubible.app.domain.model.filterReviews(reviews, reviewFilters) }
    var reviewLimit by androidx.compose.runtime.saveable.rememberSaveable(reviewFilters) { mutableIntStateOf(30) }
    var reviewError by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }

    LaunchedEffect(instructorName, retry) {
        isLoading = true
        val instRes = instructorRepository.getInstructorByName(instructorName)
        val recRes = instructorRepository.getInstructorTeachingRecords(instructorName)
        val revRes = reviewRepository.getReviewsForInstructor(instructorName)

        instructor = instRes.getOrNull()
        teachingRecords = recRes.getOrDefault(emptyList())
        historyError = recRes.exceptionOrNull()?.localizedMessage
        reviews = revRes.getOrDefault(emptyList())
        reviewError = revRes.exceptionOrNull()?.localizedMessage
        isLoading = false
    }

    LaunchedEffect(isLoading, showReviews) {
        if (!isLoading && showReviews && !reviewsFocused) {
            tab = 1
            listState.scrollToItem(0)
            reviewsFocused = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        if (isLoading) {
            M3LoadingState(
                modifier = Modifier.fillMaxSize(),
                message = "正在載入講師資料..."
            )
        } else {
                FadingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = screenContentPadding(start = 16.dp, top = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Header Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = instructorName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = instructor?.displayName ?: instructorName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val zhName = instructor?.displayNameZh.orEmpty()
                                    if (zhName.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = zhName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (!instructor?.department.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .background(
                                                    color = BadgeTheme.FacultyBgLight,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = instructor!!.department,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BadgeTheme.FacultyTextLight,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val rating = instructor?.avgRating ?: 0.0
                                        RatingBar(
                                            rating = rating,
                                            starSize = 14.dp,
                                            starColor = StarGold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${reviews.size} 則評價",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "detail-tabs") {
                        ScrollableM3ButtonGroup(tab, listOf("任教記錄 History", "學生評價 Reviews", "成績分佈 Grades").mapIndexed { index, label ->
                            M3ButtonGroupItem(label, onClick = { tab = index })
                        })
                    }
                    if (tab != 1) {
                        item(key = "history-filters") {
                            TeachingHistoryControls(teachingRecords, historyFilters) { historyFilters = it }
                            historyError?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { retry++ }) { Text("重試 Retry") } }
                        }
                        if (tab == 2) {
                            item(key = "grades") {
                                reviewError?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { retry++ }) { Text("重試 Retry") } }
                                if (distribution.isEmpty()) Text("沒有符合篩選的成績 · No matching grades")
                                else GradeDistributionChart(distribution)
                            }
                        } else {
                            item(key = "history-count") { Text("${history.size} / ${teachingRecords.size} 筆任教記錄 Teaching records") }
                            if (history.isEmpty()) item { Text("沒有符合篩選的任教記錄 · No matching teaching records") }
                            itemsIndexed(history, key = { _, it -> "teaching:${it.id}" }) { index, record ->
                                Surface(shape = com.lingubible.app.ui.components.getGroupedItemShape(index, history.size, outerRadius = 16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    onClick = hapticClick { onCourse(record.courseCode) }) {
                                    ListItem(headlineContent = { Text(record.courseCode, style = MaterialTheme.typography.titleMedium) },
                                        supportingContent = { Column {
                                            Text(teachingTermLabel(record))
                                            Text(listOf(teachingSessionLabel(record.section), teachingLanguageLabel(record.teachingLanguage).ifBlank { "語言未知 Unknown language" }).filter { it.isNotBlank() }.joinToString(" · "))
                                        } }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                                }
                            }
                        }
                    } else {
                    // Student Reviews Header
                    item(key = "student-reviews") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "學生評價 Student Reviews (${reviews.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    item(key = "review-filters") {
                        com.lingubible.app.ui.components.ReviewBrowserControls(reviews, reviewFilters, { reviewFilters = it }, filteredReviews.size)
                        reviewError?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { retry++ }) { Text("重試 Retry") } }
                    }
                    if (filteredReviews.isEmpty()) {
                        item {
                            Text(
                                text = "沒有符合篩選的評價 · No matching reviews",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        itemsIndexed(filteredReviews.take(reviewLimit), key = { _, it -> it.id }) { index, review ->
                            ReviewCard(
                                review = review,
                                groupIndex = index,
                                groupSize = minOf(reviewLimit, filteredReviews.size),
                                onVote = { voteType ->
                                    scope.launch {
                                        val voteRes = reviewRepository.voteReview(review.id, voteType)
                                        if (voteRes.isSuccess) {
                                            val newState = voteRes.getOrThrow()
                                            reviews = reviews.map {
                                                if (it.id == review.id) {
                                                    it.copy(
                                                        upvotes = newState.upvotes,
                                                        downvotes = newState.downvotes,
                                                        userVote = newState.userVote
                                                    )
                                                } else it
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    if (reviewLimit < filteredReviews.size) item { OutlinedButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { reviewLimit += 30 }) { Text("載入更多 Load more") } }
                    }
            }
        }
    }
}
