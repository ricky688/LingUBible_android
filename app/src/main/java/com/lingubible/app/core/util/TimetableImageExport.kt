package com.lingubible.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object TimetableImageExport {
    suspend fun capture(context: Context, headerLayer: GraphicsLayer, bodyLayer: GraphicsLayer, background: Int): File {
        val header = checkNotNull(headerLayer.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false))
        try {
            val body = checkNotNull(bodyLayer.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false))
            try {
                return withContext(Dispatchers.IO) {
                    require(header.width == body.width && body.height > 0) { "Timetable not ready" }
                    val image = Bitmap.createBitmap(header.width, header.height + body.height, Bitmap.Config.ARGB_8888)
                    try {
                        Canvas(image).apply {
                            drawColor(background)
                            drawBitmap(header, 0f, 0f, null)
                            drawBitmap(body, 0f, header.height.toFloat(), null)
                        }
                        val file = File.createTempFile("timetable-", ".png", context.cacheDir)
                        try {
                            file.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                            file
                        } catch (error: Exception) {
                            file.delete()
                            throw error
                        }
                    } finally {
                        image.recycle()
                    }
                }
            } finally {
                body.recycle()
            }
        } finally {
            header.recycle()
        }
    }

    fun saveToDocument(context: Context, source: File, destination: Uri) {
        source.inputStream().use { input ->
            val output = context.contentResolver.openOutputStream(destination, "wt")
                ?: error("Cannot open image destination")
            output.use { input.copyTo(it) }
        }
    }
}
