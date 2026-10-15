package com.lingubible.app.ui.components

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.core.util.TimetableExportOrientation
import com.lingubible.app.core.util.TimetableImageExport
import com.lingubible.app.domain.model.TimeConflict
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.model.TimetableTerm
import com.lingubible.app.ui.common.hapticClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Material 3 Expressive Preview Modal for Timetable Image Export.
 *
 * Provides live image preview, interactive orientation selection (Landscape default vs.
 * 9:16 Portrait standard phone ratio), expressive shape-changing buttons on press with
 * optimized bounded ripples (replacing default press-down scale/elevation drops), and
 * interactive fullscreen pinch-to-zoom / pan inspection.
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
    var showFullscreenViewer by remember { mutableStateOf(false) }

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
                // 1. Header with title & expressive shape-morphing close button
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

                    ExpressiveMorphIconButton(
                        onClick = onDismiss,
                        restingCorner = 18.dp,
                        pressedCorner = 8.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 36.dp,
                        contentDescription = "Close"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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

                // 3. Image Preview Area (Interactive Card with Shape Morphing & Fullscreen Zoom on Press)
                val previewCardInteractionSource = remember { MutableInteractionSource() }
                val isPreviewPressed by previewCardInteractionSource.collectIsPressedAsState()
                val previewCorner by animateDpAsState(
                    targetValue = if (isPreviewPressed) 24.dp else 16.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "previewCornerMorph"
                )
                val previewCardShape = RoundedCornerShape(previewCorner)

                Surface(
                    onClick = hapticClick {
                        if (previewBitmap != null && !isRendering) {
                            showFullscreenViewer = true
                        }
                    },
                    enabled = previewBitmap != null && !isRendering,
                    shape = previewCardShape,
                    color = if (isDark) Color(0xFF090D16) else Color(0xFFF1F5F9),
                    border = BorderStroke(
                        1.dp,
                        if (isPreviewPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    interactionSource = previewCardInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 340.dp)
                        .clip(previewCardShape)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                                Box(
                                    modifier = Modifier.fillMaxSize(),
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

                        // Fullscreen Zoom hint pill in bottom-end corner
                        if (!isRendering && previewBitmap != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen Zoom",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "全螢幕放大檢視",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Resolution & usage details pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
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

                // 5. Action Buttons with Material 3 Expressive Shape Changing & Bounded Ripple
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expressive Outlined Button: Cancel (Morphs from 16dp to 28dp pill on press)
                    ExpressiveMorphButton(
                        onClick = onDismiss,
                        restingCorner = 16.dp,
                        pressedCorner = 28.dp,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "取消 Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Expressive Primary Button: Save (Morphs from 16dp to 28dp pill on press)
                    ExpressiveMorphButton(
                        onClick = { onConfirmSave(selectedOrientation) },
                        restingCorner = 16.dp,
                        pressedCorner = 28.dp,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "儲存圖片 Save",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Fullscreen interactive zoom & pan viewer
    if (showFullscreenViewer && previewBitmap != null) {
        TimetableFullscreenPreviewDialog(
            bitmap = previewBitmap!!,
            orientation = selectedOrientation,
            onDismiss = { showFullscreenViewer = false },
            onConfirmSave = {
                showFullscreenViewer = false
                onConfirmSave(selectedOrientation)
            }
        )
    }
}

/**
 * Fullscreen Interactive Viewer allowing pinch-to-zoom, pan, double-tap zoom/reset,
 * and quick save with Material 3 Expressive shape morphing buttons and bounded ripples.
 */
@Composable
private fun TimetableFullscreenPreviewDialog(
    bitmap: Bitmap,
    orientation: TimetableExportOrientation,
    onDismiss: () -> Unit,
    onConfirmSave: () -> Unit
) {
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            val dialogWindow = (parent as? DialogWindowProvider)?.window
                ?: (view.context as? Activity)?.window
            dialogWindow?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D16))
        ) {
            // Interactive Zoom & Pan Viewport
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (zoomScale > 1.1f) {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                } else {
                                    zoomScale = 2.5f
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (zoomScale * zoom).coerceIn(1f, 5f)
                            zoomScale = newScale
                            if (newScale > 1f) {
                                val maxPanX = 1400f * (newScale - 1f)
                                val maxPanY = 2200f * (newScale - 1f)
                                panOffset = Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                    y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                                )
                            } else {
                                panOffset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Timetable Fullscreen Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 72.dp)
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                )
            }

            // Top Bar Overlay: Material 3 TopAppBar with Frosted Glass styling & clean alignment
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = if (orientation == TimetableExportOrientation.LANDSCAPE) "全螢幕課表檢視 · 橫向 16:9" else "全螢幕課表檢視 · 直向 9:16",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "雙指縮放 · 雙擊重設 · 拖曳檢視",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveToolbarIconButton(
                            onClick = onDismiss,
                            contentDescription = "Close"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Quick 1-tap reset pill when zoomed in
                        if (zoomScale > 1.05f) {
                            Surface(
                                onClick = hapticClick {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Zoom",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${(zoomScale * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Prominent Primary Action Pill: Save
                        ExpressiveSaveButton(
                            onClick = onConfirmSave
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xEE111827),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x33FFFFFF))
                        ),
                        shape = androidx.compose.ui.graphics.RectangleShape
                    )
            )
        }
    }
}

/**
 * Uncontained icon button inside Material 3 Floating Toolbar.
 * Sits directly on the toolbar surface without a solid idle background,
 * morphs shape on touch with pure spring physics, and provides strictly bounded ripple highlights.
 */
@Composable
private fun ExpressiveToolbarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restingCorner: Dp = 20.dp,
    pressedCorner: Dp = 10.dp,
    size: Dp = 40.dp,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) pressedCorner else restingCorner,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tbIconCorner"
    )
    val animatedShape = RoundedCornerShape(cornerRadius)

    val containerColor by animateColorAsState(
        targetValue = if (isPressed) Color.White.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tbIconBg"
    )

    Surface(
        onClick = hapticClick(onClick),
        enabled = enabled,
        shape = animatedShape,
        color = containerColor,
        contentColor = Color.White,
        interactionSource = interactionSource,
        modifier = modifier
            .size(size)
            .clip(animatedShape)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Zoom level indicator & 1-tap reset button inside Material 3 Floating Toolbar.
 * When zoom is modified (>1.05x), highlights as active pill with RestartAlt icon.
 */
@Composable
private fun ExpressiveZoomResetButton(
    zoomScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isZoomed = zoomScale > 1.05f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 20.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "zoomResetCorner"
    )
    val animatedShape = RoundedCornerShape(cornerRadius)

    val containerColor by animateColorAsState(
        targetValue = when {
            isPressed -> Color.White.copy(alpha = 0.25f)
            isZoomed -> Color.White.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "zoomResetBg"
    )

    val borderStroke = if (isZoomed) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    } else {
        null
    }

    Surface(
        onClick = hapticClick(onClick),
        shape = animatedShape,
        color = containerColor,
        contentColor = Color.White,
        border = borderStroke,
        interactionSource = interactionSource,
        modifier = modifier
            .clip(animatedShape)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isZoomed) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset Zoom",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", zoomScale)}×",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Prominent primary action button inside Material 3 Floating Toolbar.
 * Adheres to Material 3 Expressive standards with pill-to-squircle spring morphing and bounded ripple.
 */
@Composable
private fun ExpressiveSaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 20.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "saveBtnCorner"
    )
    val animatedShape = RoundedCornerShape(cornerRadius)

    Surface(
        onClick = hapticClick(onClick),
        shape = animatedShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interactionSource,
        modifier = modifier
            .clip(animatedShape)
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = animatedShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "儲存 Save",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Material 3 Expressive Action Button with dynamic shape morphing on press
 * (corner radius morphing with pure spring physics, replacing the press-down effect)
 * and strictly bounded high-contrast ripple highlight.
 */
@Composable
private fun ExpressiveMorphButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restingCorner: Dp = 16.dp,
    pressedCorner: Dp = 28.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring shape morph replacing traditional scale/elevation drop
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) pressedCorner else restingCorner,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnCornerMorph"
    )
    val animatedShape = RoundedCornerShape(cornerRadius)

    Surface(
        onClick = hapticClick(onClick),
        enabled = enabled,
        shape = animatedShape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource,
        modifier = modifier
            .clip(animatedShape)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Material 3 Expressive Icon Button with dynamic shape morphing on press
 * and strictly bounded ripple highlight.
 */
@Composable
private fun ExpressiveMorphIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restingCorner: Dp = 18.dp,
    pressedCorner: Dp = 8.dp,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    border: BorderStroke? = null,
    size: Dp = 36.dp,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) pressedCorner else restingCorner,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconBtnCornerMorph"
    )
    val animatedShape = RoundedCornerShape(cornerRadius)

    Surface(
        onClick = hapticClick(onClick),
        enabled = enabled,
        shape = animatedShape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource,
        modifier = modifier
            .size(size)
            .clip(animatedShape)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Orientation Choice Card with dynamic shape morphing on press and strictly bounded ripple.
 */
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Expressive shape morph on press: morphs between 18dp squircle and 8dp pill/squircle
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 18.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "choiceCornerMorph"
    )
    val animatedShape = RoundedCornerShape(cornerRadius)

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
        onClick = hapticClick(onClick),
        shape = animatedShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        interactionSource = interactionSource,
        modifier = modifier
            .clip(animatedShape)
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
