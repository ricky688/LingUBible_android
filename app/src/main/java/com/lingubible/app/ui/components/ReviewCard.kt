package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.Review

@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
    onVote: ((voteType: String) -> Unit)? = null
) {
    val isDark = isAppDarkTheme()
    val bgColor = MaterialTheme.colorScheme.surfaceContainer
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: User Info & Grade Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (review.isAnonymous)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (review.isAnonymous) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = (review.courseCode.take(1).ifBlank { "L" }).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = review.courseCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (review.instructorName.isNotBlank()) {
                                val instDisplay = if (review.instructorNameZh.isNotBlank()) {
                                    " • ${review.instructorName} (${review.instructorNameZh})"
                                } else {
                                    " • ${review.instructorName}"
                                }
                                Text(
                                    text = instDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (review.courseTitle.isNotBlank() && review.courseTitle != review.courseCode) {
                            val courseTitleDisplay = if (review.courseTitleZh.isNotBlank()) {
                                "${review.courseTitle}  ${review.courseTitleZh}"
                            } else {
                                review.courseTitle
                            }
                            Text(
                                text = courseTitleDisplay,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (review.isAnonymous) "匿名同學" else "嶺南人",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (review.term.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = BadgeTheme.OfferedBgLight,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = review.term,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BadgeTheme.OfferedTextLight,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Circular GradeBadge
                GradeBadge(
                    grade = review.grade,
                    size = GradeBadgeSize.MEDIUM
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ratings Row: Teaching, Grading, Workload, Difficulty
            Surface(
                color = if (isDark) Color(0xFF1E242B) else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    RatingItem("教學", review.teachingRating)
                    RatingItem("給分", review.gradingRating)
                    RatingItem("工作量", review.workloadRating)
                    RatingItem("難度", review.difficultyRating)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comment Body
            if (review.comment.isNotBlank()) {
                Column {
                    Text(
                        text = review.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    if (review.comment.length > 120) {
                        Text(
                            text = if (isExpanded) "收起" else "閱讀更多",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable { isExpanded = !isExpanded }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(
                color = borderColor.copy(alpha = 0.6f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Date & Voting Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.createdAt.take(10).ifBlank { "最近" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote Button
                    val isUpvoted = review.userVote == "up"
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onVote?.invoke("up") },
                        color = if (isUpvoted) Color(0xFFDCFCE7) else if (isDark) Color(0xFF27272A) else Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isUpvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Upvote",
                                modifier = Modifier.size(15.dp),
                                tint = if (isUpvoted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${review.upvotes}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isUpvoted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUpvoted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Downvote Button
                    val isDownvoted = review.userVote == "down"
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onVote?.invoke("down") },
                        color = if (isDownvoted) Color(0xFFFEE2E2) else if (isDark) Color(0xFF27272A) else Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDownvoted) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                contentDescription = "Downvote",
                                modifier = Modifier.size(15.dp),
                                tint = if (isDownvoted) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${review.downvotes}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isDownvoted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDownvoted) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingItem(label: String, rating: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        RatingBar(
            rating = rating,
            starSize = 12.dp,
            starColor = StarGold
        )
    }
}
