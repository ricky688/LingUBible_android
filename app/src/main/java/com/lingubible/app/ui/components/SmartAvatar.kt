package com.lingubible.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.domain.model.AvatarPresets
import com.lingubible.app.domain.model.CustomAvatar

@Composable
fun SmartAvatar(
    avatar: CustomAvatar? = null,
    userId: String? = null,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    border: BorderStroke? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val effectiveAvatar = remember(avatar, userId) {
        when {
            avatar != null -> avatar
            !userId.isNullOrBlank() -> AvatarPresets.getDefaultAvatar(userId)
            else -> AvatarPresets.getDefaultAvatar("")
        }
    }

    val bgGradient = remember(effectiveAvatar.backgroundIndex) {
        val bg = AvatarPresets.getBackground(effectiveAvatar.backgroundIndex)
        Brush.linearGradient(bg.colors)
    }

    val emojiFontSize = (size.value * 0.56f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgGradient)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(onClick = onClick)
                        .pointerHoverIcon(PointerIcon.Hand)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = effectiveAvatar.animal,
            fontSize = emojiFontSize,
            lineHeight = emojiFontSize,
            fontWeight = FontWeight.Normal
        )
    }
}
