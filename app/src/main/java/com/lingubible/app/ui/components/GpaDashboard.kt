package com.lingubible.app.ui.components

import android.graphics.Paint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.domain.calculator.HonoursCalculator.RequiredAvgStatus
import com.lingubible.app.ui.viewmodels.GpaHonsUiState
import java.util.Locale

internal fun gpaNumber(value: Double?): String = value?.let { String.format(Locale.US, "%.3f", it) } ?: "—"

@Composable
fun GpaSectionTitle(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun GpaPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
internal fun GpaMetric(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                )
            )
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                        fadeIn(spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                        (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } +
                            fadeOut(spring(stiffness = Spring.StiffnessMedium)))
                },
                label = "gpaMetricRoll_${title}"
            ) { targetValue ->
                Text(
                    text = targetValue,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun GpaSummary(state: GpaHonsUiState) {
    GpaPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GpaMetric(
                title = "累積 GPA",
                subtitle = "Cumulative GPA",
                value = gpaNumber(state.cgpa),
                icon = Icons.Default.School,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            GpaMetric(
                title = "已計入學分",
                subtitle = "GPA credits",
                value = String.format(Locale.US, "%.0f", state.earnedCredits),
                icon = Icons.Default.AutoStories,
                accentColor = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        }

        // Classification Card
        val tier = state.currentHonoursTier
        val (classificationZh, classificationEn) = when {
            tier != null -> tier.titleZh to tier.titleEn
            state.cgpa == null -> "尚未計入成績" to "No grades yet"
            else -> "尚低於榮譽線" to "Below honours threshold"
        }
        val classificationColor = when {
            tier != null -> Color(0xFFF59E0B) // Amber / Gold
            state.cgpa != null -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(18.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(classificationColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tier != null) Icons.Default.EmojiEvents else Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = classificationColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "榮譽等級 · Classification",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedContent(
                        targetState = classificationZh to classificationEn,
                        transitionSpec = {
                            (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.95f)) togetherWith
                                (fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                                    scaleOut(spring(stiffness = Spring.StiffnessMedium), targetScale = 0.95f))
                        },
                        label = "classificationTransition"
                    ) { (zh, en) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = zh,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tier != null) classificationColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = en,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GpaTargetCard(state: GpaHonsUiState, onTarget: (String) -> Unit, onRemaining: (String) -> Unit) {
    GpaPanel {
        GpaSectionTitle("目標榮譽", "Target Honours")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.targetCgpaInput,
                onValueChange = onTarget,
                label = { Text("目標 GPA · Target", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.remainingCreditsInput,
                onValueChange = onRemaining,
                label = { Text("剩餘學分 · Credits", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                placeholder = { Text(maxOf(0, (120 - state.earnedCredits).toInt()).toString(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            "目標 GPA 0.00–4.00 · 留空預設以 120 − 已計學分推算",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = (state.targetCgpaInput.toFloatOrNull() ?: 3.5f).coerceIn(0f, 4f),
            onValueChange = { onTarget(String.format(Locale.US, "%.2f", it)) },
            valueRange = 0f..4f,
            modifier = Modifier.semantics { contentDescription = "目標累積 GPA · Target cGPA" }
        )
        FlowRow(
            modifier = Modifier.animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HonoursCalculator.HonoursTier.entries.forEach { tier ->
                val isSelected = state.targetCgpaInput.toDoubleOrNull() == tier.minCgpa
                FilterChip(
                    selected = isSelected,
                    onClick = { onTarget(String.format(Locale.US, "%.2f", tier.minCgpa)) },
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Text(
                            text = "${tier.titleZh} ${String.format(Locale.US, "%.2f", tier.minCgpa)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        val result = state.targetResult
        AnimatedContent(
            targetState = result,
            transitionSpec = {
                ((fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.97f)) togetherWith
                    (fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                        scaleOut(spring(stiffness = Spring.StiffnessMedium), targetScale = 0.97f)))
                    .using(SizeTransform(clip = true) { _, _ -> spring(stiffness = Spring.StiffnessMediumLow) })
            },
            label = "targetHonoursResult"
        ) { animatedResult ->
            val (statusBg, statusBorderColor) = when (animatedResult.status) {
                RequiredAvgStatus.FEASIBLE -> Color(0xFF10B981).copy(alpha = 0.12f) to Color(0xFF10B981).copy(alpha = 0.35f)
                RequiredAvgStatus.ACHIEVED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) to MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                RequiredAvgStatus.IMPOSSIBLE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) to MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                RequiredAvgStatus.NO_REMAINING -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.outlineVariant
            }

            Surface(
                color = statusBg,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            statusBorderColor,
                            statusBorderColor.copy(alpha = 0.10f)
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (animatedResult.status) {
                        RequiredAvgStatus.FEASIBLE -> {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "所需平均 GPA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Required average in remaining courses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedContent(
                                targetState = gpaNumber(animatedResult.required),
                                transitionSpec = {
                                    (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                        fadeIn(spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                                        (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } +
                                            fadeOut(spring(stiffness = Spring.StiffnessMedium)))
                                },
                                label = "targetRequiredGpaRoll"
                            ) { reqStr ->
                                Text(
                                    text = reqStr,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                        RequiredAvgStatus.ACHIEVED -> {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "已達目標推算條件",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Target secured under current credits & grades",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        RequiredAvgStatus.IMPOSSIBLE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "目標超出可達上限",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Target exceeds maximum · 最高可能: ${gpaNumber(animatedResult.projectedCgpa)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        RequiredAvgStatus.NO_REMAINING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "沒有剩餘學分",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "No remaining credits · 最終 cGPA: ${gpaNumber(animatedResult.projectedCgpa)}",
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
}

private data class TrendPoint(val label: String, val term: Double?, val cumulative: Double?)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GpaTrendCard(state: GpaHonsUiState) {
    var honours by rememberSaveable { mutableStateOf(false) }
    var awards by rememberSaveable { mutableStateOf(false) }
    var fullScale by rememberSaveable { mutableStateOf(true) }
    val points = remember(state.document, state.termStats) {
        var totalPoints = 0.0
        var totalCredits = 0.0
        state.document.terms.sortedWith(compareBy({ it.year }, { it.part.ordinal })).map { term ->
            val stats = state.termStats[term.id]
            totalPoints += stats?.points ?: 0.0
            totalCredits += stats?.gpaCredits ?: 0.0
            val academic = state.document.yearAcademic[term.year] ?: "${2021 + term.year}-${2022 + term.year}"
            val suffix = when (term.part.code) { "term1" -> "T1"; "term2" -> "T2"; else -> "S" }
            TrendPoint("${academic.take(4).takeLast(2)}/${academic.takeLast(2)}-$suffix", stats?.termGpa, if (totalCredits > 0) totalPoints / totalCredits else null)
        }
    }
    GpaPanel {
        GpaSectionTitle("GPA 走勢", "Cumulative & Term GPA")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = honours,
                onClick = { honours = !honours },
                shape = RoundedCornerShape(12.dp),
                label = { Text("榮譽線 Honours") }
            )
            FilterChip(
                selected = awards,
                onClick = { awards = !awards },
                shape = RoundedCornerShape(12.dp),
                label = { Text("榮譽榜 Awards") }
            )
            FilterChip(
                selected = fullScale,
                onClick = { fullScale = !fullScale },
                shape = RoundedCornerShape(12.dp),
                label = { Text(if (fullScale) "全刻度 0–4" else "自動刻度 Auto") }
            )
        }
        if (points.none { it.term != null }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "加入課程成績後即可查看走勢",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enter course grades to see your GPA trend",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val termColor = MaterialTheme.colorScheme.tertiary
            val cumulativeColor = MaterialTheme.colorScheme.primary
            val awardColor = MaterialTheme.colorScheme.secondary
            val gridColor = MaterialTheme.colorScheme.outlineVariant
            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            val values = points.flatMap { listOfNotNull(it.term, it.cumulative) }
            val min = if (fullScale) 0.0 else ((values.minOrNull() ?: 0.0) - 0.25).coerceAtLeast(0.0)
            val max = if (fullScale) 4.0 else ((values.maxOrNull() ?: 4.0) + 0.25).coerceAtMost(4.0)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                Canvas(Modifier.horizontalScroll(rememberScrollState()).width(maxOf(maxWidth, (points.size * 68 + 40).dp)).height(220.dp).semantics { contentDescription = "GPA trend; exact term and cumulative values are listed below" }) {
                    val left = 34.dp.toPx(); val bottom = size.height - 30.dp.toPx(); val top = 12.dp.toPx()
                    fun y(value: Double) = bottom - ((value - min) / (max - min)).toFloat() * (bottom - top)
                    fun x(index: Int) = left + (size.width - left - 16.dp.toPx()) * if (points.size == 1) 0.5f else index.toFloat() / (points.size - 1)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = labelColor.toArgb(); textSize = 10.sp.toPx() }
                    repeat(5) { tick ->
                        val value = min + (max - min) * tick / 4
                        drawLine(gridColor, Offset(left, y(value)), Offset(size.width, y(value)), 1.dp.toPx())
                        drawContext.canvas.nativeCanvas.drawText(String.format(Locale.US, "%.1f", value), 0f, y(value) + 4.dp.toPx(), paint)
                    }
                    val references = (if (honours) HonoursCalculator.HonoursTier.entries.map { it.minCgpa to gridColor } else emptyList()) + (if (awards) listOf(3.3 to awardColor, 3.7 to termColor) else emptyList())
                    references.filter { it.first in min..max }.forEach { (value, color) -> drawLine(color, Offset(left, y(value)), Offset(size.width, y(value)), 1.dp.toPx()) }
                    listOf(cumulativeColor to points.map { it.cumulative }, termColor to points.map { it.term }).forEach { (color, series) ->
                        val path = Path(); var connected = false
                        series.forEachIndexed { index, value ->
                            if (value == null) connected = false else {
                                if (connected) path.lineTo(x(index), y(value)) else path.moveTo(x(index), y(value))
                                connected = true; drawCircle(color, 3.dp.toPx(), Offset(x(index), y(value)))
                            }
                        }
                        drawPath(path, color, style = Stroke(2.dp.toPx(), pathEffect = if (color == termColor) PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())) else null))
                    }
                    paint.textAlign = Paint.Align.CENTER
                    points.forEachIndexed { index, point -> drawContext.canvas.nativeCanvas.drawText(point.label, x(index), size.height - 8.dp.toPx(), paint) }
                }
            }
            Text("實線：累積 GPA · Solid: cumulative\n虛線：學期 GPA · Dashed: term", style = MaterialTheme.typography.labelSmall)
            if (honours) Text("榮譽界線 · Honours: 1.67 / 2.00 / 2.50 / 3.00 / 3.50", style = MaterialTheme.typography.labelSmall)
            if (awards) Text("院長榜 3.30 · Dean’s / 校長榜 3.70 · President’s\n須同時符合年度學分及成績條件 · Credit and grade requirements also apply", style = MaterialTheme.typography.labelSmall)
            points.forEach { Text("${it.label}   學期 ${gpaNumber(it.term)}   累積 ${gpaNumber(it.cumulative)}", style = MaterialTheme.typography.bodySmall) }
        }
    }
}
