package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.domain.model.TimeConflict

private fun formatDayZhEn(day: String): String = when (day.uppercase()) {
    "SUN" -> "週日 Sun"
    "MON" -> "週一 Mon"
    "TUE" -> "週二 Tue"
    "WED" -> "週三 Wed"
    "THU" -> "週四 Thu"
    "FRI" -> "週五 Fri"
    "SAT" -> "週六 Sat"
    else -> day
}

/**
 * Ultra-Modern Material 3 Expressive Conflict Alert Card for Calendar & Timetable
 *
 * Implements:
 * - Frosted glass translucent acrylic styling with specular gradient border
 * - Atmospheric ambient crimson glow with subtle breathing pulse
 * - Expressive leading squircle badge with category-coded error tint
 * - Bilingual hierarchy: Traditional Chinese primary headline with English subtitle
 * - Rich course cards with left-accent category indicators and clash connector
 * - Actionable buttons: Resolve / Change Section and View in Calendar Grid
 * - Pure spring stiffness motion transitions
 */
@Composable
fun TimeConflictAlertCard(
    conflicts: List<TimeConflict>,
    modifier: Modifier = Modifier,
    onSelectConflict: ((TimeConflict) -> Unit)? = null,
    onResolveClick: (() -> Unit)? = null
) {
    if (conflicts.isEmpty()) return

    val isDark = isAppDarkTheme()
    var isExpanded by remember { mutableStateOf(false) }

    // Subtle breathing pulse for warning border
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulse"
    )

    // Ambient Glow and Glassmorphism Colors
    val cardBg = if (isDark) Color(0xFF281113).copy(alpha = 0.90f) else Color(0xFFFEF2F2).copy(alpha = 0.94f)
    val specularBorder = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFFEF4444).copy(alpha = pulseBorderAlpha),
                Color(0xFFB91C1C).copy(alpha = 0.35f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFFFCA5A5).copy(alpha = pulseBorderAlpha),
                Color(0xFFEF4444).copy(alpha = 0.45f)
            )
        )
    }
    val glassSheen = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.10f else 0.40f),
            Color.Transparent
        )
    )

    AnimatedVisibility(
        visible = conflicts.isNotEmpty(),
        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Atmospheric Ambient Crimson Glow Underlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopStart)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LingnanRed.copy(alpha = if (isDark) 0.28f else 0.16f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B).copy(alpha = if (isDark) 0.18f else 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Frosted Glass Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.2.dp, specularBorder), RoundedCornerShape(20.dp))
                    .drawBehind { drawRect(brush = glassSheen) }
                    .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Header Row: Leading Squircle Badge, Bilingual Title, Count Badge / Expand Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Expressive Leading Squircle Badge
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LingnanRed.copy(alpha = if (isDark) 0.25f else 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Conflict Warning",
                                    tint = if (isDark) Color(0xFFF87171) else LingnanRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Bilingual Headline & Supporting text
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "時間衝突警示",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color(0xFF991B1B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = LingnanRed.copy(alpha = if (isDark) 0.35f else 0.15f)
                                    ) {
                                        Text(
                                            text = "${conflicts.size} 處重疊",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFFCA5A5) else LingnanRed,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Schedule Conflict Detected · 課程上課時段重疊",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                                )
                            }
                        }

                        // Multiple conflicts toggle badge
                        if (conflicts.size > 1) {
                            val rotation by animateFloatAsState(
                                targetValue = if (isExpanded) 180f else 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "arrowRotation"
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2),
                                modifier = Modifier
                                    .clickable { isExpanded = !isExpanded }
                                    .pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isExpanded) "收合" else "全部 (${conflicts.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .rotate(rotation)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Conflicts List
                    val displayedConflicts = if (isExpanded) conflicts else conflicts.take(1)

                    displayedConflicts.forEachIndexed { index, conflict ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        val itemShape = if (displayedConflicts.size == 1) {
                            RoundedCornerShape(14.dp)
                        } else {
                            when (index) {
                                0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                displayedConflicts.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                else -> RoundedCornerShape(4.dp)
                            }
                        }

                        // Conflict Detail Item with Expressive Split Course Cards
                        Surface(
                            shape = itemShape,
                            color = if (isDark) Color(0xFF1E1113).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.98f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.7f) else Color(0xFFFECACA)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (onSelectConflict != null) {
                                        Modifier
                                            .clickable { onSelectConflict(conflict) }
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    } else Modifier
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                // Side-by-side Clash Courses View
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Section A Mini Card
                                    Box(modifier = Modifier.weight(1f)) {
                                        CourseMiniCard(
                                            courseCode = conflict.sectionA.courseCode,
                                            courseTitle = conflict.sectionA.courseTitle,
                                            section = conflict.sectionA.section,
                                            colorHex = conflict.sectionA.colorHex,
                                            isDark = isDark
                                        )
                                    }

                                    // Center Clash Badge
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "⚡",
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Section B Mini Card
                                    Box(modifier = Modifier.weight(1f)) {
                                        CourseMiniCard(
                                            courseCode = conflict.sectionB.courseCode,
                                            courseTitle = conflict.sectionB.courseTitle,
                                            section = conflict.sectionB.section,
                                            colorHex = conflict.sectionB.colorHex,
                                            isDark = isDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Time Slot & Overlap Interval Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF3B1216).copy(alpha = 0.6f) else Color(0xFFFFF1F2),
                                    border = BorderStroke(0.5.dp, if (isDark) Color(0xFF991B1B) else Color(0xFFFECDD3)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${formatDayZhEn(conflict.day)} · ${conflict.timeDescription}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Text(
                                            text = "時段衝堂 Clashing",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row: Resolve / Change Section
                    if (onResolveClick != null || onSelectConflict != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onSelectConflict != null) {
                                OutlinedButton(
                                    onClick = { onSelectConflict(conflicts.first()) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Text(
                                        text = "在日曆中查看",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (onResolveClick != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledTonalButton(
                                    onClick = onResolveClick,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = LingnanRed.copy(alpha = if (isDark) 0.30f else 0.15f),
                                        contentColor = if (isDark) Color(0xFFFCA5A5) else LingnanRed
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Text(
                                        text = "調整班別解決衝突",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
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

@Composable
private fun CourseMiniCard(
    courseCode: String,
    courseTitle: String,
    section: String,
    colorHex: String,
    isDark: Boolean
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(0.75.dp, chipColor.copy(alpha = if (isDark) 0.5f else 0.35f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Category Color Accent Bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(chipColor)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = courseCode,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Section $section",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
