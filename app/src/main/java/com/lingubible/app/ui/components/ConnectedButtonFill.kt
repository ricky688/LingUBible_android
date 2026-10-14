package com.lingubible.app.ui.components

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

internal fun Modifier.connectedButtonPerimeterFill(
    focus: State<Float>,
    focused: Boolean,
    pressWave: State<Float>,
    primary: Color,
    highlight: Color
): Modifier = drawWithCache {
    val ring = Path().apply { fillType = PathFillType.EvenOdd }
    val bounds = Rect(0f, 0f, size.width, size.height)
    val featherCorner = 8.dp.toPx()
    val labelTint = Paint().apply { colorFilter = ColorFilter.tint(highlight) }
    onDrawWithContent {
        // Reading animation state here keeps each frame in drawing rather than composition.
        val progress = focus.value.coerceIn(0f, 1f)
        drawPerimeterFill(ring, bounds, featherCorner, progress, primary, if (focused) 1f else progress)
        val wave = pressWave.value.coerceIn(0f, 1f)
        if (wave > 0f && wave < 1f) {
            drawPerimeterFill(ring, bounds, featherCorner, wave, highlight.copy(alpha = 0.16f * (1f - wave)), 1f)
        }
        drawContent()
        if (progress > 0f) {
            // Tint only the label pixels reached by the fill, preserving contrast at its moving edge.
            ring.perimeterRing(bounds, (progress - (1f - progress) * 0.09f).coerceAtLeast(0f), featherCorner)
            clipPath(ring) {
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(bounds, labelTint)
                    this@onDrawWithContent.drawContent()
                    canvas.restore()
                }
            }
        }
    }
}

private fun DrawScope.drawPerimeterFill(
    ring: Path,
    bounds: Rect,
    corner: Float,
    fraction: Float,
    color: Color,
    opacity: Float
) {
    val progress = fraction.coerceIn(0f, 1f)
    if (progress <= 0f) return
    if (progress >= 1f) {
        drawRect(color)
        return
    }
    // Nested feathered rings fill inward; reversing clears the center and washes out at the edges.
    val bands = 8
    for (band in 0 until bands) {
        val inset = (progress - (1f - progress) * 0.18f * band / bands).coerceAtLeast(0f)
        if (inset == 0f) continue
        ring.perimeterRing(bounds, inset, corner)
        drawPath(ring, color, alpha = opacity / (bands - band * opacity))
    }
}

private fun Path.perimeterRing(bounds: Rect, inset: Float, corner: Float) {
    reset()
    addRect(bounds)
    if (inset >= 1f) return
    val inner = Rect(
        bounds.width * inset / 2f, bounds.height * inset / 2f,
        bounds.width * (1f - inset / 2f), bounds.height * (1f - inset / 2f)
    )
    val radius = minOf(corner, inner.width / 2f, inner.height / 2f, inner.top)
    addRoundRect(RoundRect(inner, CornerRadius(radius)))
}
