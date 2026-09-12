package com.lingubible.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onNavigateBack: () -> Unit,
    instructorRepository: InstructorRepository = koinInject(),
    reviewRepository: ReviewRepository = koinInject()
) {
    val isDark = isAppDarkTheme()
    var isLoading by remember { mutableStateOf(true) }
    var instructor by remember { mutableStateOf<Instructor?>(null) }
    var teachingRecords by remember { mutableStateOf<List<TeachingRecord>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(instructorName) {
        isLoading = true
        val instRes = instructorRepository.getInstructorByName(instructorName)
        val recRes = instructorRepository.getInstructorTeachingRecords(instructorName)
        val revRes = reviewRepository.getReviewsForInstructor(instructorName)

        instructor = instRes.getOrNull()
        teachingRecords = recRes.getOrDefault(emptyList())
        reviews = revRes.getOrDefault(emptyList())
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        if (isLoading) {
            M3LoadingState(
                modifier = Modifier.fillMaxSize(),
                message = "正在載入講師資料..."
            )
        } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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

                    // Courses Taught
                    if (teachingRecords.isNotEmpty()) {
                        item {
                            Text(
                                text = "任教課程 Courses Taught",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(teachingRecords.distinctBy { it.courseCode }) { record ->
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
                                    Text(
                                        text = record.courseCode,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${record.term} (${record.academicYear})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Student Reviews Header
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "學生評價 Student Reviews (${reviews.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (reviews.isEmpty()) {
                        item {
                            Text(
                                text = "此講師暫無學生評價記錄。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(reviews) { review ->
                            ReviewCard(
                                review = review,
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
            }
        }
    }
}
