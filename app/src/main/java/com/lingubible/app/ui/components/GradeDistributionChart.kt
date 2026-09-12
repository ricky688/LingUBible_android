package com.lingubible.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.domain.model.GradeStatistics
import com.lingubible.app.domain.util.GradeCalculator

@Composable
fun GradeDistributionChart(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val stats: GradeStatistics = GradeCalculator.calculateGradeStatistics(distribution)
    val paretoPoints = GradeCalculator.calculateParetoCurve(distribution)
    val maxCount = (distribution.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stats Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grade Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Based on ${stats.totalCount} student submissions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (stats.mean != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.2f", stats.mean),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Mean GPA",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            val barColor = MaterialTheme.colorScheme.primary
            val curveColor = Color(0xFFE11D48)
            val grades = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height - 30f // Leave space for bottom labels
                    val barWidth = (w / grades.size) * 0.65f
                    val stepX = w / grades.size

                    // Draw Bars
                    grades.forEachIndexed { index, grade ->
                        val count = distribution[grade] ?: 0
                        val barHeight = (count.toFloat() / maxCount.toFloat()) * h
                        val left = index * stepX + (stepX - barWidth) / 2f
                        val top = h - barHeight

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }

                    // Draw Pareto Cumulative Percentage Line
                    if (paretoPoints.isNotEmpty()) {
                        val path = Path()
                        paretoPoints.forEachIndexed { index, point ->
                            val x = index * stepX + stepX / 2f
                            val y = h - (point.cumulativePercentage.toFloat() / 100f) * h
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = curveColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw points on curve
                        paretoPoints.forEachIndexed { index, point ->
                            val x = index * stepX + stepX / 2f
                            val y = h - (point.cumulativePercentage.toFloat() / 100f) * h
                            drawCircle(
                                color = curveColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // Grade Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                grades.forEach { grade ->
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
