package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.domain.model.CourseSyllabus

/**
 * Material 3 Expressive Action Button for viewing the Course Syllabus PDF.
 *
 * Implements web app parity with src/pages/CourseDetail.tsx:
 * - Loading state: shows circular progress indicator and disables interaction while resolving
 * - Enabled state: displays syllabus title, filename, and opens the document view URL
 * - Fallback state: displays clear disabled state when no syllabus is available for the course
 * - Tactile motion: pure spring stiffness press-scale feedback without damping ratio overrides
 * - Bilingual typography: Traditional Chinese primary with English secondary
 */
@Composable
fun CourseSyllabusButton(
    syllabus: CourseSyllabus?,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = !isLoading && syllabus != null
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "syllabusButtonScale"
    )

    val cornerRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed && isEnabled) 20.dp else 12.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "syllabusButtonCornerMorph"
    )

    Surface(
        onClick = { if (isEnabled) onClick() },
        enabled = isEnabled,
        shape = RoundedCornerShape(cornerRadius),
        color = when {
            isLoading -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            syllabus != null -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        },
        contentColor = when {
            isLoading -> MaterialTheme.colorScheme.onSurfaceVariant
            syllabus != null -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isLoading -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                syllabus != null -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerHoverIcon(if (isEnabled) PointerIcon.Hand else PointerIcon.Default)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "Syllabus",
                        modifier = Modifier.size(20.dp),
                        tint = if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = when {
                            isLoading -> "正在載入大綱... / Loading Syllabus..."
                            syllabus != null -> "課程大綱 / Syllabus"
                            else -> "暫無課程大綱 / No Syllabus"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isLoading -> MaterialTheme.colorScheme.onSurfaceVariant
                            syllabus != null -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                    if (syllabus != null) {
                        val subtitle = if (syllabus.fileSize > 0L) {
                            "${syllabus.fileName} • ${com.lingubible.app.domain.util.FileUtils.formatFileSize(syllabus.fileSize)}"
                        } else {
                            syllabus.fileName
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (!isLoading) {
                        Text(
                            text = "未提供電子大綱 • Not Available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (isEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Syllabus",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
