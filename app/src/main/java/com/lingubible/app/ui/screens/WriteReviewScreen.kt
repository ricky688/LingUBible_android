package com.lingubible.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3LoadingIndicator
import com.lingubible.app.ui.components.RatingBar
import com.lingubible.app.ui.viewmodels.WriteReviewViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    initialCourseCode: String = "",
    onNavigateBack: () -> Unit,
    viewModel: WriteReviewViewModel = koinViewModel()
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var courseCode by remember { mutableStateOf(initialCourseCode) }
    var courseTitle by remember { mutableStateOf("") }
    var instructorName by remember { mutableStateOf("") }
    var term by remember { mutableStateOf("Term 1") }
    var academicYear by remember { mutableStateOf("2024-2025") }
    var grade by remember { mutableStateOf("A") }

    var teachingRating by remember { mutableDoubleStateOf(4.0) }
    var gradingRating by remember { mutableDoubleStateOf(4.0) }
    var workloadRating by remember { mutableDoubleStateOf(3.0) }
    var difficultyRating by remember { mutableDoubleStateOf(3.0) }

    var comment by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(true) }

    LaunchedEffect(initialCourseCode) {
        if (initialCourseCode.isNotBlank()) {
            viewModel.setInitialCourse(initialCourseCode)
            courseCode = initialCourseCode
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePadding()
                .mouseScrollbar(scrollState)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Step 1: Course Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. 課程與講師基本資訊",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = {
                            courseCode = it
                            viewModel.updateCourseInfo(it, courseTitle, instructorName)
                        },
                        label = { Text("課程代碼 Course Code (例: CLC9001)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = courseTitle,
                        onValueChange = {
                            courseTitle = it
                            viewModel.updateCourseInfo(courseCode, it, instructorName)
                        },
                        label = { Text("課程名稱 Course Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructorName,
                        onValueChange = {
                            instructorName = it
                            viewModel.updateCourseInfo(courseCode, courseTitle, it)
                        },
                        label = { Text("講師姓名 Instructor / Lecturer") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Step 2: Term & Grade
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. 修讀學期與最終成績",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = academicYear,
                            onValueChange = { academicYear = it },
                            label = { Text("學年 Year") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = term,
                            onValueChange = { term = it },
                            label = { Text("學期 Term") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("最終成績 Final Grade (A, A-, B+, B, 等)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Step 3: Star Ratings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. 細項評分",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    RatingRow("教學質量 Teaching", teachingRating) { teachingRating = it }
                    RatingRow("給分公平 Grading", gradingRating) { gradingRating = it }
                    RatingRow("課業負擔 Workload", workloadRating) { workloadRating = it }
                    RatingRow("課程難度 Difficulty", difficultyRating) { difficultyRating = it }
                }
            }

            // Step 4: Comments & Anon
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "4. 心得評論與設定",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("分享你的真實修課心得 (至少 20 字)...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "以匿名身分發佈",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }


                val submitInteractionSource = remember { MutableInteractionSource() }
                val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
                val isSubmitHovered by submitInteractionSource.collectIsHoveredAsState()
                val submitScale by animateFloatAsState(
                    targetValue = when {
                        isSubmitPressed -> 0.96f
                        isSubmitHovered -> 1.025f
                        else -> 1.0f
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "submitScale"
                )

                // Submit Button - Material 3 Expressive
                Button(
                    onClick = {
                        viewModel.updateCourseInfo(courseCode, courseTitle, instructorName)
                        viewModel.updateTerm(term, academicYear, grade)
                        viewModel.updateRatings(teachingRating, gradingRating, workloadRating, difficultyRating)
                        viewModel.updateComment(comment)
                        viewModel.submitReview(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .graphicsLayer {
                            scaleX = submitScale
                            scaleY = submitScale
                        }
                        .shadow(
                            elevation = when {
                                isSubmitPressed -> 10.dp
                                isSubmitHovered -> 8.dp
                                else -> 5.dp
                            },
                            shape = RoundedCornerShape(27.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isSubmitHovered) 0.5f else 0.35f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isSubmitHovered) 0.5f else 0.35f)
                        ),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    interactionSource = submitInteractionSource,
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        M3LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "提交評價 Submit Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RatingRow(
    label: String,
    rating: Double,
    onRatingChanged: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        RatingBar(
            rating = rating,
            starSize = 22.dp,
            starColor = StarGold,
            onRatingChanged = onRatingChanged
        )
    }
}
