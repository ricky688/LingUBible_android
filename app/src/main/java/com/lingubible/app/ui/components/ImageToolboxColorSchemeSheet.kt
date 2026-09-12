package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.settings.AppColorScheme
import com.lingubible.app.core.settings.PaletteStyle
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates a mathematically smooth 12-lobed scalloped flower path,
 * identical to the Material 3 Expressive flower badge in Image Toolbox.
 */
fun createScallopedFlowerPath(size: Size, lobes: Int = 12, depth: Float = 0.12f): Path {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rBase = minOf(cx, cy)
    val rMid = rBase * (1f - depth)
    val rAmp = rBase * depth
    val steps = lobes * 8
    for (step in 0..steps) {
        val angle = (step.toFloat() / steps) * 2f * Math.PI.toFloat()
        val r = rMid + rAmp * cos(lobes * angle)
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/**
 * Material 3 Expressive 12-Lobed Scalloped Flower Badge with multi-colored pie sectors.
 */
@Composable
fun ScallopedFlowerBadge(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    isAddButton: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.10f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "BadgeScale"
    )

    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val selectionRingColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(54.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(54.dp)) {
            val badgePath = createScallopedFlowerPath(size = size, lobes = 12, depth = 0.12f)

            // Draw outer scalloped flower background
            drawPath(
                path = badgePath,
                color = if (isAddButton) surfaceContainer else surfaceContainer.copy(alpha = 0.95f)
            )

            // Draw subtle outer border along scalloped path
            drawPath(
                path = badgePath,
                color = if (isSelected) selectionRingColor else outlineVariant.copy(alpha = 0.35f),
                style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.dp.toPx())
            )

            if (!isAddButton) {
                // Inset multi-tone circular pie (Primary top 180°, Secondary bottom-left 90°, Tertiary bottom-right 90°)
                val inset = size.width * 0.17f
                val pieSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                val pieOffset = Offset(inset, inset)

                // Top half: Primary Color (180° to 360°)
                drawArc(
                    color = primaryColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = pieOffset,
                    size = pieSize
                )

                // Bottom-left quadrant: Secondary Color (90° to 180°)
                drawArc(
                    color = secondaryColor,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = pieOffset,
                    size = pieSize
                )

                // Bottom-right quadrant: Tertiary Color (0° to 90°)
                drawArc(
                    color = tertiaryColor,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = pieOffset,
                    size = pieSize
                )
            }
        }

        if (isAddButton) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Custom Color",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Custom Material 3 Expressive Contrast Slider with thick track and vertical capsule thumb indicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveContrastSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0.0f..1.0f,
        modifier = modifier.fillMaxWidth(),
        thumb = {
            // Distinct vertical pill thumb matching Image Toolbox
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(10.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                drawStopIndicator = null
            )
        }
    )
}

/**
 * Palette Style Selector Dialog.
 */
@Composable
fun PaletteStyleDialog(
    currentStyle: PaletteStyle,
    onSelectStyle: (PaletteStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("調色盤風格", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PaletteStyle.entries.forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectStyle(style)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentStyle == style,
                            onClick = {
                                onSelectStyle(style)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = style.titleZh,
                                fontWeight = if (currentStyle == style) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = style.titleEn,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Custom Color Picker Dialog with Hue Slider & Hex Input.
 */
@Composable
fun CustomColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hue by remember {
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (initialColor.red * 255).toInt(),
            (initialColor.green * 255).toInt(),
            (initialColor.blue * 255).toInt(),
            hsv
        )
        mutableFloatStateOf(hsv[0])
    }

    var hexText by remember {
        val colorInt = (initialColor.red * 255).toInt().shl(16) or
                (initialColor.green * 255).toInt().shl(8) or
                (initialColor.blue * 255).toInt()
        mutableStateOf(String.format("#%06X", (0xFFFFFF and colorInt)))
    }

    val currentColor = remember(hue) {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.85f, 0.90f))
        Color(argb)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("自訂主題顏色", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Color Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentColor)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hexText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hue Rainbow Slider
                Text("色相 (Hue)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = hue,
                    onValueChange = {
                        hue = it
                        val argb = android.graphics.Color.HSVToColor(floatArrayOf(it, 0.85f, 0.90f))
                        val hex = String.format("#%06X", (0xFFFFFF and argb))
                        hexText = hex
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hex text input
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input
                        if (input.startsWith("#") && input.length == 7) {
                            try {
                                val parsed = android.graphics.Color.parseColor(input)
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(parsed, hsv)
                                hue = hsv[0]
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Hex 色碼 (例如 #E53935)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColor)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("套用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Image Toolbox Style Color Scheme Selection Modal Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun ImageToolboxColorSchemeSheet(
    activeColorScheme: AppColorScheme,
    paletteStyle: PaletteStyle,
    isInvertedColors: Boolean,
    isDynamicColor: Boolean,
    contrastLevel: Float,
    customColorHex: Long?,
    onSelectColorScheme: (AppColorScheme) -> Unit,
    onSelectPaletteStyle: (PaletteStyle) -> Unit,
    onToggleInvertedColors: (Boolean) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onContrastLevelChange: (Float) -> Unit,
    onCustomColorSelected: (Color) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPaletteStyleDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }

    if (showPaletteStyleDialog) {
        PaletteStyleDialog(
            currentStyle = paletteStyle,
            onSelectStyle = onSelectPaletteStyle,
            onDismiss = { showPaletteStyleDialog = false }
        )
    }

    if (showColorPickerDialog) {
        val initialColor = if (customColorHex != null) Color(customColorHex) else Color(activeColorScheme.primaryHex)
        CustomColorPickerDialog(
            initialColor = initialColor,
            onColorSelected = { color ->
                onCustomColorSelected(color)
            },
            onDismiss = { showColorPickerDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            android.util.Log.d("ImageToolbox", "onDismissRequest called!")
            onDismiss()
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        val isDark = com.lingubible.app.core.theme.isAppDarkTheme()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header: Squircle Badge + "色彩方案" Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "色彩方案",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card 1: 調色盤風格 (Palette Style)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "調色盤風格",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = paletteStyle.titleZh,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showPaletteStyleDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Palette Style",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Card 2: 顏色反轉 (Invert Colors)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.InvertColors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "顏色反轉",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "若啟用，將會將主題顏色更換為相反顏色",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isInvertedColors,
                        onCheckedChange = onToggleInvertedColors,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Card 3: 表情符號作為配色方案 (Emoji / Dynamic Scheme)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "表情符號作為配色方案",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "使用表情符號原色作為應用程式配色方案，而不是手動定義的配色方案",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isDynamicColor,
                        onCheckedChange = onToggleDynamicColor,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Card 4: 對比 (Contrast)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contrast,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "對比",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        // Pill displaying contrast value (e.g. 0, 0.5)
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (contrastLevel == 0f) "0" else String.format("%.1f", contrastLevel),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressiveContrastSlider(
                        value = contrastLevel,
                        onValueChange = onContrastLevelChange
                    )
                }
            }

            // Card 5: 簡單變體 (Simple Variants)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "簡單變體",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // FlowRow of 12-Lobed Scalloped Flower Badges
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 5
                    ) {
                        AppColorScheme.entries.forEach { scheme ->
                            val isSelected = !isDynamicColor && customColorHex == null && activeColorScheme == scheme
                            val primary = Color(scheme.primaryHex)
                            val secondary = Color(scheme.secondaryHex)
                            val tertiary = Color(scheme.tertiaryHex)
                            ScallopedFlowerBadge(
                                primaryColor = primary,
                                secondaryColor = secondary,
                                tertiaryColor = tertiary,
                                isSelected = isSelected,
                                onClick = {
                                    onSelectColorScheme(scheme)
                                }
                            )
                        }

                        // Custom color (+) button
                        val isCustomSelected = customColorHex != null
                        ScallopedFlowerBadge(
                            primaryColor = if (customColorHex != null) Color(customColorHex) else MaterialTheme.colorScheme.primary,
                            secondaryColor = MaterialTheme.colorScheme.secondary,
                            tertiaryColor = MaterialTheme.colorScheme.tertiary,
                            isSelected = isCustomSelected,
                            isAddButton = true,
                            onClick = {
                                showColorPickerDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Docked Action Bar: [ 🗑️ ]  [ ✏️ ]              [ 關閉 ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trash icon (Reset to defaults)
                Box(
                    modifier = Modifier
                        .size(width = 68.dp, height = 44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                        .clickable(onClick = onResetDefaults),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Reset Defaults",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Edit pencil icon (Open Color Picker)
                Box(
                    modifier = Modifier
                        .size(width = 68.dp, height = 44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f))
                        .clickable { showColorPickerDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Custom Color",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Close button ("關閉")
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .width(110.dp)
                ) {
                    Text(
                        text = "關閉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
