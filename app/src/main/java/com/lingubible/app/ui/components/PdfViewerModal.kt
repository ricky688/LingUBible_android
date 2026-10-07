package com.lingubible.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import android.app.Activity
import android.view.WindowManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.util.DocumentDownloadHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Full-screen Material 3 Expressive PDF Viewer Modal.
 *
 * Implements native in-app rendering using Android's native PdfRenderer:
 * - High-resolution page rasterization with pure coroutines
 * - Translucent frosted glass top header bar with system status bar inset awareness
 * - Dynamic page indicator badge (e.g. 第 1 / 4 頁 • Page 1 of 4)
 * - Pinch-to-zoom and pan navigation with reset control
 * - Dark mode inverted color reading mode (matching web app parity)
 * - Direct download to device Downloads folder (supporting authenticated Appwrite buckets)
 * - Share and external application handover via FileProvider
 */
@Composable
fun PdfViewerModal(
    title: String,
    fileName: String,
    url: String,
    bucketId: String? = null,
    fileId: String? = null,
    clientProvider: AppwriteClientProvider? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val pages = remember { mutableStateListOf<Bitmap>() }
    var totalPages by remember { mutableIntStateOf(0) }
    var cachedFile by remember { mutableStateOf<File?>(null) }

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isInverted by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val currentPage = remember {
        derivedStateOf {
            if (totalPages > 0) (lazyListState.firstVisibleItemIndex + 1).coerceAtMost(totalPages) else 1
        }
    }

    // Load and render PDF
    LaunchedEffect(url, fileId) {
        isLoading = true
        errorMessage = null
        pages.clear()
        totalPages = 0

        try {
            val key = if (!fileId.isNullOrBlank()) fileId else fileName
            val file = DocumentDownloadHelper.cacheDocumentLocally(
                context = context,
                cacheKey = key,
                url = url,
                bucketId = bucketId,
                fileId = fileId,
                clientProvider = clientProvider
            )
            cachedFile = file

            withContext(Dispatchers.IO) {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount

                withContext(Dispatchers.Main) {
                    totalPages = count
                    isLoading = false
                }

                // Render pages sequentially for crisp resolution
                val density = context.resources.displayMetrics.density
                val renderScale = (density * 1.5f).coerceIn(1.8f, 3.0f)

                for (i in 0 until count) {
                    val page = renderer.openPage(i)
                    val width = (page.width * renderScale).toInt()
                    val height = (page.height * renderScale).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(AndroidColor.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    withContext(Dispatchers.Main) {
                        pages.add(bitmap)
                    }
                }

                renderer.close()
                pfd.close()
            }
        } catch (e: Exception) {
            android.util.Log.e("PdfViewerModal", "Error loading/rendering PDF: ${e.message}", e)
            isLoading = false
            errorMessage = e.localizedMessage ?: "無法開啟文件 / Unable to open PDF"
        }
    }

    // Clean up bitmaps when dialog is disposed
    DisposableEffect(Unit) {
        onDispose {
            pages.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            pages.clear()
        }
    }

    val invertFilter = remember(isInverted) {
        if (isInverted) {
            ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f,  0f,  0f,  0f, 255f,
                         0f, -1f,  0f,  0f, 255f,
                         0f,  0f, -1f,  0f, 255f,
                         0f,  0f,  0f,  1f,   0f
                    )
                )
            )
        } else null
    }

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
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }

        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF121212) else Color(0xFFF4F6F9))
        ) {
            // Main Content Area
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在載入文件...\nLoading document...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "無法載入文件\nUnable to Load Document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("關閉 Close")
                            }
                            Button(
                                onClick = {
                                    // Retry
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        try {
                                            val key = if (!fileId.isNullOrBlank()) fileId else fileName
                                            cachedFile = DocumentDownloadHelper.cacheDocumentLocally(
                                                context = context,
                                                cacheKey = key,
                                                url = url,
                                                bucketId = bucketId,
                                                fileId = fileId,
                                                clientProvider = clientProvider
                                            )
                                        } catch (e: Exception) {
                                            errorMessage = e.localizedMessage
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("重試 Retry")
                            }
                        }
                    }
                }
                else -> {
                    // PDF Page Stream
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(1f, 3.5f)
                                    if (zoomScale > 1f) {
                                        val maxPanX = 600f * (zoomScale - 1f)
                                        val maxPanY = 800f * (zoomScale - 1f)
                                        panOffset = Offset(
                                            x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                            y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                                        )
                                    } else {
                                        panOffset = Offset.Zero
                                    }
                                }
                            }
                    ) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    translationX = panOffset.x
                                    translationY = panOffset.y
                                },
                            contentPadding = PaddingValues(
                                top = statusBarHeight + 64.dp, // space for top frosted header extending through status bar
                                bottom = navBarHeight + 32.dp,
                                start = 12.dp,
                                end = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            itemsIndexed(pages) { index, bitmap ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                        border = BorderStroke(
                                            0.5.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isInverted) Color.Black else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Page ${index + 1}",
                                            colorFilter = invertFilter,
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "- ${index + 1} -",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Frosted Glass Top Bar extending through status bar for unified edge-to-edge
            FrostedGlassHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                showSheen = true,
                showGlow = false
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back / Close button & Titles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = fileName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (totalPages > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) {
                                        Text(
                                            text = "${currentPage.value} / $totalPages",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right: Actions (Invert Color, Download, Share)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Reset zoom button when zoomed in
                        if (zoomScale > 1.05f) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ZoomOutMap,
                                    contentDescription = "Reset Zoom",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Invert colors (Night Reading mode)
                        IconButton(
                            onClick = { isInverted = !isInverted }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.InvertColors,
                                contentDescription = "Invert Colors",
                                tint = if (isInverted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Download Button
                        IconButton(
                            onClick = {
                                if (!isDownloading) {
                                    scope.launch {
                                        isDownloading = true
                                        DocumentDownloadHelper.downloadDocument(
                                            context = context,
                                            fileName = fileName,
                                            url = url,
                                            bucketId = bucketId,
                                            fileId = fileId,
                                            clientProvider = clientProvider
                                        )
                                        isDownloading = false
                                    }
                                }
                            },
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Download",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Share / Open External Button
                        IconButton(
                            onClick = {
                                cachedFile?.let { file ->
                                    DocumentDownloadHelper.shareDocument(
                                        context = context,
                                        file = file,
                                        title = title
                                    )
                                } ?: run {
                                    Toast.makeText(context, "請等待文件載入完成\nPlease wait for document to load", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
