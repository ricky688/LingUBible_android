package com.lingubible.app.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.lingubible.app.domain.model.CalendarCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Scallop Shape (12-lobed wavy flower badge).
 * Matches the signature scalloped flower badge in media_1789043193023.png.
 */
class ScallopShape(
    private val lobes: Int = 12,
    private val amplitudeRatio: Float = 0.10f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = minOf(centerX, centerY)
        val minRadius = maxRadius * (1f - amplitudeRatio * 2f)
        val midRadius = (maxRadius + minRadius) / 2f
        val amplitude = (maxRadius - minRadius) / 2f

        val stepCount = lobes * 8
        for (i in 0..stepCount) {
            val angle = (i.toFloat() / stepCount) * (2f * PI.toFloat())
            val r = midRadius + amplitude * cos(lobes * angle)
            val x = centerX + r * cos(angle)
            val y = centerY + r * sin(angle)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Material 3 Expressive Burst shape (notched multi-point badge).
 */
class ExpressiveBurstShape(
    private val points: Int = 12,
    private val depthRatio: Float = 0.14f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = minOf(centerX, centerY)
        val minRadius = maxRadius * (1f - depthRatio)

        val totalPoints = points * 2
        for (i in 0..totalPoints) {
            val angle = (i.toFloat() / totalPoints) * (2f * PI.toFloat())
            val r = if (i % 2 == 0) maxRadius else minRadius
            val x = centerX + r * cos(angle)
            val y = centerY + r * sin(angle)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Material 3 Expressive Squircle / Continuous Rounded Shape.
 */
val ExpressiveSquircleShape = RoundedCornerShape(38)

/**
 * Maps each CalendarCategory to an expressive leading shape matching media_1789043193023.png.
 */
fun CalendarCategory.getExpressiveShape(): Shape {
    return when (this) {
        CalendarCategory.HOLIDAY -> CircleShape
        CalendarCategory.REGISTRATION -> ScallopShape(lobes = 12, amplitudeRatio = 0.11f)
        CalendarCategory.ASSESSMENT -> ScallopShape(lobes = 14, amplitudeRatio = 0.09f)
        CalendarCategory.TERM -> ScallopShape(lobes = 12, amplitudeRatio = 0.10f)
        CalendarCategory.EXAM -> ExpressiveBurstShape(points = 12, depthRatio = 0.14f)
        CalendarCategory.ADD_DROP -> ExpressiveSquircleShape
        CalendarCategory.DEADLINE -> ScallopShape(lobes = 10, amplitudeRatio = 0.12f)
        CalendarCategory.GRADUATION -> ExpressiveBurstShape(points = 10, depthRatio = 0.16f)
        CalendarCategory.EVENT -> CircleShape
        CalendarCategory.HOSTEL -> ExpressiveSquircleShape
    }
}

/**
 * Pastel badge background colors matching the authentic Material 3 Expressive palette in media_1789043193023.png.
 */
fun CalendarCategory.getExpressiveBadgeBgColor(isDark: Boolean): Color {
    return if (isDark) {
        when (this) {
            CalendarCategory.HOLIDAY -> Color(0xFF6B4E9B)       // Dark Lavender
            CalendarCategory.REGISTRATION -> Color(0xFF3B679B)  // Dark Sky Blue
            CalendarCategory.ASSESSMENT -> Color(0xFF8C3E75)    // Dark Soft Pink
            CalendarCategory.TERM -> Color(0xFF8B2B2B)          // Dark Crimson
            CalendarCategory.EXAM -> Color(0xFF7A5910)          // Dark Amber
            CalendarCategory.ADD_DROP -> Color(0xFF4A6B22)      // Dark Olive Lime
            CalendarCategory.DEADLINE -> Color(0xFF8A461E)      // Dark Rust
            CalendarCategory.GRADUATION -> Color(0xFF434982)    // Dark Indigo
            CalendarCategory.EVENT -> Color(0xFF475569)         // Dark Slate
            CalendarCategory.HOSTEL -> Color(0xFF135A52)        // Dark Teal
        }
    } else {
        when (this) {
            CalendarCategory.HOLIDAY -> Color(0xFFD0BCFF)       // Pastel Lavender (item 1 in reference)
            CalendarCategory.REGISTRATION -> Color(0xFFA0C4FF)  // Pastel Sky Blue (item 2 in reference)
            CalendarCategory.ASSESSMENT -> Color(0xFFF2A1D2)    // Pastel Rose Pink (item 3 in reference)
            CalendarCategory.TERM -> Color(0xFFFFB4AB)          // Pastel Coral
            CalendarCategory.EXAM -> Color(0xFFFFE088)          // Pastel Warm Amber
            CalendarCategory.ADD_DROP -> Color(0xFFC4EFA4)      // Pastel Fresh Lime
            CalendarCategory.DEADLINE -> Color(0xFFFFB77C)      // Pastel Peach
            CalendarCategory.GRADUATION -> Color(0xFFBAC3FF)    // Pastel Periwinkle
            CalendarCategory.EVENT -> Color(0xFFCBD5E1)         // Pastel Slate
            CalendarCategory.HOSTEL -> Color(0xFF99F6E4)        // Pastel Teal
        }
    }
}

/**
 * Contrasting badge glyph / icon tint.
 */
fun CalendarCategory.getExpressiveBadgeContentColor(isDark: Boolean): Color {
    return if (isDark) {
        Color.White
    } else {
        when (this) {
            CalendarCategory.HOLIDAY -> Color(0xFF381E72)
            CalendarCategory.REGISTRATION -> Color(0xFF003258)
            CalendarCategory.ASSESSMENT -> Color(0xFF56003E)
            CalendarCategory.TERM -> Color(0xFF690005)
            CalendarCategory.EXAM -> Color(0xFF553B00)
            CalendarCategory.ADD_DROP -> Color(0xFF1B3700)
            CalendarCategory.DEADLINE -> Color(0xFF5A1C00)
            CalendarCategory.GRADUATION -> Color(0xFF1B2366)
            CalendarCategory.EVENT -> Color(0xFF1E293B)
            CalendarCategory.HOSTEL -> Color(0xFF003732)
        }
    }
}

/**
 * Category letter glyph (e.g. "A", "B", "C") matching media_1789043193023.png.
 */
fun CalendarCategory.getCategoryGlyph(): String {
    return when (this) {
        CalendarCategory.HOLIDAY -> "A"       // Holiday / Leave
        CalendarCategory.REGISTRATION -> "B"  // Registration
        CalendarCategory.ASSESSMENT -> "C"    // Assessment / Appeal
        CalendarCategory.TERM -> "T"          // Term / Semester
        CalendarCategory.EXAM -> "E"          // Exam
        CalendarCategory.ADD_DROP -> "D"      // Add/Drop
        CalendarCategory.DEADLINE -> "L"      // Deadline
        CalendarCategory.GRADUATION -> "G"    // Graduation
        CalendarCategory.EVENT -> "V"         // Event
        CalendarCategory.HOSTEL -> "H"        // Hostel
    }
}

fun CalendarCategory.getExpressiveIcon(): ImageVector {
    return when (this) {
        CalendarCategory.TERM -> Icons.Outlined.School
        CalendarCategory.EXAM -> Icons.Outlined.Assignment
        CalendarCategory.HOLIDAY -> Icons.Outlined.Celebration
        CalendarCategory.ADD_DROP -> Icons.Outlined.SwapHoriz
        CalendarCategory.REGISTRATION -> Icons.Outlined.AppRegistration
        CalendarCategory.DEADLINE -> Icons.Outlined.NotificationImportant
        CalendarCategory.ASSESSMENT -> Icons.Outlined.Grading
        CalendarCategory.GRADUATION -> Icons.Outlined.WorkspacePremium
        CalendarCategory.EVENT -> Icons.Outlined.Event
        CalendarCategory.HOSTEL -> Icons.Outlined.Home
    }
}

/**
 * Generates contextual corner radii for Material 3 Expressive grouped list items.
 * Uses 28.dp outer corner radius to match media_1789043193023.png.
 */
fun getGroupedItemShape(
    index: Int,
    totalCount: Int,
    outerRadius: Dp = 28.dp,
    innerRadius: Dp = 4.dp
): RoundedCornerShape {
    return when {
        totalCount == 1 -> RoundedCornerShape(outerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = outerRadius,
            topEnd = outerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        index == totalCount - 1 -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = outerRadius,
            bottomEnd = outerRadius
        )
        else -> RoundedCornerShape(innerRadius)
    }
}
