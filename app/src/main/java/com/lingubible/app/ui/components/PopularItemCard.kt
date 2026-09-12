package com.lingubible.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.model.Instructor
import java.util.Locale

@Composable
fun PopularCourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorited: Boolean = false,
    onFavoriteToggle: ((Boolean) -> Unit)? = null
) {
    val isDark = isAppDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.015f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
    val titleEnColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563)
    val titleZhColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF9CA3AF)
    val subtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .scale(scale)
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isPressed -> 4.dp
                isHovered -> 6.dp
                else -> 2.dp
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Left (Code & Titles) + Right (Average GPA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Course Code and Titles
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = course.titleEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = titleEnColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (course.titleZh.isNotBlank()) {
                        Text(
                            text = course.titleZh,
                            style = MaterialTheme.typography.bodySmall,
                            color = titleZhColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (course.department.isNotBlank()) {
                            CategoryBadge(
                                text = course.department,
                                bgColor = if (isDark) BadgeTheme.DepartmentBgDark else BadgeTheme.DepartmentBgLight,
                                textColor = if (isDark) BadgeTheme.DepartmentTextDark else BadgeTheme.DepartmentTextLight,
                                borderColor = if (isDark) BadgeTheme.DepartmentBorderDark else BadgeTheme.DepartmentBorderLight
                            )
                        }

                        // Default teaching language
                        CategoryBadge(
                            text = "English",
                            bgColor = if (isDark) BadgeTheme.LanguageBgDark else BadgeTheme.LanguageBgLight,
                            textColor = if (isDark) BadgeTheme.LanguageTextDark else BadgeTheme.LanguageTextLight,
                            borderColor = if (isDark) BadgeTheme.LanguageBorderDark else BadgeTheme.LanguageBorderLight
                        )
                    }
                }

                // Average GPA Display
                AverageGPADisplay(gpaString = course.avgGrade, reviewCount = course.reviewCount)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 StatBoxes: Workload, Difficulty, Usefulness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(label = "WORKLOAD", value = course.avgWorkload, modifier = Modifier.weight(1f))
                StatBox(label = "DIFFICULTY", value = course.avgDifficulty, modifier = Modifier.weight(1f))
                StatBox(label = "RATING", value = course.avgRating, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Review count on left, Favorite on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Reviews",
                        tint = subtextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${course.reviewCount} ${if (course.reviewCount == 1) "review" else "reviews"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                FavoriteButton(
                    isFavorited = isFavorited,
                    onToggle = { onFavoriteToggle?.invoke(it) }
                )
            }
        }
    }
}

@Composable
fun PopularInstructorCard(
    instructor: Instructor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorited: Boolean = false,
    onFavoriteToggle: ((Boolean) -> Unit)? = null
) {
    val isDark = isAppDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.015f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "instructorCardScale"
    )

    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
    val titleZhColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF9CA3AF)
    val subtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .scale(scale)
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isPressed -> 4.dp
                isHovered -> 6.dp
                else -> 2.dp
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = instructor.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (instructor.displayNameZh.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = instructor.displayNameZh,
                            style = MaterialTheme.typography.bodySmall,
                            color = titleZhColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (instructor.department.isNotBlank()) {
                        CategoryBadge(
                            text = instructor.department,
                            bgColor = if (isDark) BadgeTheme.DepartmentBgDark else BadgeTheme.DepartmentBgLight,
                            textColor = if (isDark) BadgeTheme.DepartmentTextDark else BadgeTheme.DepartmentTextLight,
                            borderColor = if (isDark) BadgeTheme.DepartmentBorderDark else BadgeTheme.DepartmentBorderLight
                        )
                    }
                }

                // Average GPA / Score Display
                AverageGPADisplay(
                    gpaString = if (instructor.avgRating > 0) String.format(Locale.US, "%.1f", instructor.avgRating) else "N/A",
                    reviewCount = instructor.reviewCount,
                    label = "SCORE"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // StatBoxes: Teaching & Grading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(label = "TEACHING", value = instructor.avgRating, modifier = Modifier.weight(1f))
                StatBox(label = "GRADING", value = instructor.avgRating, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Reviews",
                        tint = subtextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${instructor.reviewCount} ${if (instructor.reviewCount == 1) "review" else "reviews"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                FavoriteButton(
                    isFavorited = isFavorited,
                    onToggle = { onFavoriteToggle?.invoke(it) }
                )
            }
        }
    }
}

@Composable
fun AverageGPADisplay(
    gpaString: String,
    reviewCount: Int,
    label: String = "AVG GPA"
) {
    val isDark = isAppDarkTheme()
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val subtextColor = if (isDark) Color(0xFF64748B) else Color(0xFF9CA3AF)

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            letterSpacing = 0.5.sp
        )

        Row(verticalAlignment = Alignment.Bottom) {
            if (gpaString.isNotBlank() && gpaString != "N/A") {
                Text(
                    text = gpaString,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                if (reviewCount > 0) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "($reviewCount)",
                        fontSize = 11.sp,
                        color = subtextColor,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            } else {
                Text(
                    text = "N/A",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtextColor
                )
            }
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: Double,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val bgColor = getRatingGradientColor(value)
    val displayValue = if (value > 0.0) String.format(Locale.US, "%.1f", value).removeSuffix(".0") else "N/A"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(bgColor, RoundedCornerShape(8.dp))
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
fun CategoryBadge(
    text: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
