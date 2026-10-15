package com.lingubible.app.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lingubible.app.core.util.TimetableExportOrientation
import com.lingubible.app.core.util.TimetableImageExport
import com.lingubible.app.domain.model.TimeConflict
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.model.TimetableTerm
import androidx.compose.animation.core.tween
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.ui.common.hapticClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Material 3 Expressive Preview Modal for Timetable Image Export.
 *
 * Provides live image preview and orientation selection between
 * Landscape (Default 16:9 widescreen, 2400x1400) and Portrait (9:16 standard phone ratio, 1440x2560).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimetableExportPreviewModal(
    term: TimetableTerm,
    selectedSections: List<TimetableSection>,
    conflicts: List<TimeConflict>,
    creditsByCode: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirmSave: (TimetableExportOrientation) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()

    // Default orientation is Landscape as specified
    var selectedOrientation by remember { mutableStateOf(TimetableExportOrientation.LANDSCAPE) }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(true) }

    LaunchedEffect(selectedOrientation, term, selectedSections, conflicts, isDark) {
        isRendering = true
        withContext(Dispatchers.Default) {
            val bmp = TimetableImageExport.generateBitmap(
                orientation = selectedOrientation,
                term = term,
                selectedSections = selectedSections,
                conflicts = conflicts,
                isDark = isDark,
                creditsByCode = creditsByCode
            )
            withContext(Dispatchers.Main) {
                previewBitmap?.recycle()
                previewBitmap = bmp
                isRendering = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            previewBitmap?.recycle()
            previewBitmap = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header with title & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "課表圖片匯出預覽",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Timetable Image Export Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = hapticClick(onDismiss),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Orientation Selector: Landscape (Default) vs Portrait (Standard Phone Ratio)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Landscape Option
                    OrientationChoiceCard(
                        title = "橫向寬螢幕",
                        subtitle = "Landscape (預設)",
                        aspectLabel = "16:9",
                        icon = Icons.Default.StayCurrentLandscape,
                        isSelected = selectedOrientation == TimetableExportOrientation.LANDSCAPE,
                        onClick = { selectedOrientation = TimetableExportOrientation.LANDSCAPE },
                        modifier = Modifier.weight(1f)
                    )

                    // Portrait Option
                    OrientationChoiceCard(
                        title = "直向標準手機",
                        subtitle = "Portrait (手機比例)",
                        aspectLabel = "9:16",
                        icon = Icons.Default.StayCurrentPortrait,
                        isSelected = selectedOrientation == TimetableExportOrientation.PORTRAIT,
                        onClick = { selectedOrientation = TimetableExportOrientation.PORTRAIT },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3. Image Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 340.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF090D16) else Color(0xFFF1F5F9))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = previewBitmap to isRendering,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "previewCrossfade"
                    ) { (bmp, rendering) ->
                        if (rendering || bmp == null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "繪製課表預覽中... / Generating preview...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val ratio = if (selectedOrientation == TimetableExportOrientation.LANDSCAPE) {
                                2400f / 1400f
                            } else {
                                1440f / 2560f
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Timetable Image Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }

                // 4. Resolution & usage details pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedOrientation == TimetableExportOrientation.LANDSCAPE) {
                                Icons.Default.AspectRatio
                            } else {
                                Icons.Default.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (selectedOrientation == TimetableExportOrientation.LANDSCAPE) {
                                    "2400 × 1400 · 寬螢幕橫向比例 (16:9)"
                                } else {
                                    "1440 × 2560 · 標準手機比例 (9:16)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedOrientation == TimetableExportOrientation.LANDSCAPE) {
                                    "課堂卡片寬廣清晰，適合電腦、平板與列印保存"
                                } else {
                                    "直向寬度自適應最適比例，完美符合手機螢幕與桌布分享"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 5. Actions (Cancel / Confirm Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = hapticClick(onDismiss),
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("取消 Cancel")
                    }

                    Button(
                        onClick = hapticClick { onConfirmSave(selectedOrientation) },
                        modifier = Modifier
                            .weight(1.3f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "儲存圖片 Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrientationChoiceCard(
    title: String,
    subtitle: String,
    aspectLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "choiceBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "choiceFg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "choiceBorder"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .clickable(onClick = hapticClick(onClick))
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = aspectLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
