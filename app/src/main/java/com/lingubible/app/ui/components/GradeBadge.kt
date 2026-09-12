package com.lingubible.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.GradeColors

enum class GradeBadgeSize(val dp: Dp, val fontSize: Int) {
    SMALL(32.dp, 12),
    MEDIUM(40.dp, 15),
    LARGE(48.dp, 18)
}

@Composable
fun GradeBadge(
    grade: String,
    modifier: Modifier = Modifier,
    size: GradeBadgeSize = GradeBadgeSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    val displayGrade = if (grade == "-1" || grade.isBlank() || grade.equals("null", ignoreCase = true)) {
        "N/A"
    } else {
        grade.trim()
    }
    val gradientColors = GradeColors.getGradientForGrade(displayGrade)
    val gradient = Brush.linearGradient(gradientColors)

    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(brush = gradient, shape = CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .then(
                if (onClick != null) {
                    Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(onClick = onClick)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayGrade,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = size.fontSize.sp
        )
    }
}
