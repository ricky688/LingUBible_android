package com.lingubible.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingCircles(
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "FloatingCircles")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Circle 1: Top-left subtle primary
        val x1 = width * 0.15f + cos(time) * 30f
        val y1 = height * 0.12f + sin(time) * 25f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.07f), Color.Transparent),
                center = Offset(x1, y1),
                radius = 160f
            ),
            center = Offset(x1, y1),
            radius = 160f
        )

        // Circle 2: Top-right subtle blue
        val x2 = width * 0.82f + cos(time + 1.2f) * 25f
        val y2 = height * 0.22f + sin(time + 1.2f) * 30f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.05f), Color.Transparent),
                center = Offset(x2, y2),
                radius = 140f
            ),
            center = Offset(x2, y2),
            radius = 140f
        )

        // Circle 3: Center-left warm amber
        val x3 = width * 0.25f + cos(time + 2.5f) * 35f
        val y3 = height * 0.55f + sin(time + 2.5f) * 20f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.05f), Color.Transparent),
                center = Offset(x3, y3),
                radius = 150f
            ),
            center = Offset(x3, y3),
            radius = 150f
        )

        // Circle 4: Center-right subtle primary
        val x4 = width * 0.75f + cos(time + 3.8f) * 20f
        val y4 = height * 0.70f + sin(time + 3.8f) * 30f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.06f), Color.Transparent),
                center = Offset(x4, y4),
                radius = 180f
            ),
            center = Offset(x4, y4),
            radius = 180f
        )
    }
}
