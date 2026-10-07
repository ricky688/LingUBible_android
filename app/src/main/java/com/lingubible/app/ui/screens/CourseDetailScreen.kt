package com.lingubible.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.lingubible.app.ui.components.CourseSyllabusButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.rememberCoroutineScope
import com.lingubible.app.domain.util.DocumentDownloadHelper
import kotlinx.coroutines.launch

data class ActivePdf(
    val title: String,
    val fileName: String,
    val url: String,
    val bucketId: String? = null,
    val fileId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseCode: String,
    onNavigateBack: () -> Unit,
    onNavigateToWriteReview: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    viewModel: CourseDetailViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFavorited by remember { mutableStateOf(false) }
    var activePdf by remember { mutableStateOf<ActivePdf?>(null) }
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

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Expressive Course Syllabus Action Button (Web App Parity)
                        CourseSyllabusButton(
                            syllabus = uiState.syllabus,
                            isLoading = uiState.isSyllabusLoading,
                            onClick = {
                                viewModel.resolveAndOpenSyllabus(courseCode) { syl ->
                                    activePdf = ActivePdf(
                                        title = "${course?.code ?: courseCode} 課程大綱",
                                        fileName = syl.fileName,
                                        url = syl.viewUrl,
                                        bucketId = "course_syllabus",
                                        fileId = syl.id
                                    )
                                }
                            }
                        )
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

                M3ButtonGroup(
                    selectedIndex = selectedTab,
                    items = sectionItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    equalWeight = true,
                    height = 38.dp,
                    spacing = 5.dp
                )

                // Tab Content with directional spring transition
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val forward = targetState > initialState
                        val direction = if (forward) 1 else -1
                        (slideInHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            initialOffsetX = { fullWidth -> (direction * fullWidth * 0.25f).toInt() }
                        ) + fadeIn(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )) togetherWith (slideOutHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            targetOffsetX = { fullWidth -> (-direction * fullWidth * 0.25f).toInt() }
                        ) + fadeOut(
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        ))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    label = "courseDetailTabTransition"
                ) { tabIndex ->
                    when (tabIndex) {
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
                            val isLoggedIn = uiState.currentUser != null
                            if (!isLoggedIn && uiState.pastPapers.isEmpty()) {
                                AuthRequiredPastPapersCard(
                                    onNavigateToAuth = onNavigateToAuth
                                )
                            } else if (uiState.pastPapers.isEmpty()) {
                                EmptyState(message = "此課程暫無歷屆試題\nNo past papers available")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(uiState.pastPapers) { paper ->
                                        PastPaperCard(
                                            paper = paper,
                                            onView = { p ->
                                                activePdf = ActivePdf(
                                                    title = "${p.courseCode} 歷屆試題 (${p.academicYear} ${p.term})",
                                                    fileName = p.fileName,
                                                    url = p.viewUrl,
                                                    bucketId = "past_exam_papers",
                                                    fileId = p.fileId
                                                )
                                            },
                                            onDownload = { p ->
                                                coroutineScope.launch {
                                                    DocumentDownloadHelper.downloadDocument(
                                                        context = context,
                                                        fileName = p.fileName,
                                                        url = p.downloadUrl,
                                                        bucketId = "past_exam_papers",
                                                        fileId = p.fileId,
                                                        clientProvider = viewModel.clientProvider
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Teaching Records Tab
                            if (uiState.teachingRecords.isEmpty()) {
                                EmptyState(message = "暫無任教記錄\nNo teaching records available")
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

        activePdf?.let { pdf ->
            PdfViewerModal(
                title = pdf.title,
                fileName = pdf.fileName,
                url = pdf.url,
                bucketId = pdf.bucketId,
                fileId = pdf.fileId,
                clientProvider = viewModel.clientProvider,
                onDismiss = { activePdf = null }
            )
        }
    }
}

@Composable
private fun AuthRequiredPastPapersCard(
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "登入以查閱歷屆試題",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign In to View Past Exam Papers",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "歷屆試題僅供嶺南大學學生查閱。請登入或註冊您的學校帳號以解鎖試題下載與檢視。\nPast exam papers are exclusively available to Lingnan University students. Please sign in or register with your student email.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateToAuth,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "立即登入 • Sign In",
                    fontWeight = FontWeight.Bold
                )
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

