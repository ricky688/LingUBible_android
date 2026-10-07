package com.lingubible.app.domain.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.lingubible.app.data.remote.AppwriteClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Universal document download & cache helper for LingUBible.
 * Supports Appwrite Storage authenticated downloads, OkHttp streaming,
 * local caching for in-app PDF rendering, and public Downloads saving.
 */
object DocumentDownloadHelper {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Download document bytes either via Appwrite Storage SDK (supporting authenticated buckets)
     * or via HTTP GET fallback, and save to public Downloads folder.
     */
    suspend fun downloadDocument(
        context: Context,
        fileName: String,
        url: String,
        bucketId: String? = null,
        fileId: String? = null,
        clientProvider: AppwriteClientProvider? = null,
        onSuccess: ((Uri?) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ): Result<Uri?> = withContext(Dispatchers.IO) {
        try {
            val sanitizedFileName = sanitizeFileName(fileName)
            val bytes = fetchBytes(url, bucketId, fileId, clientProvider)

            // Save to public Downloads directory
            val uri = saveToPublicDownloads(context, sanitizedFileName, bytes)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "已下載至下載資料夾：$sanitizedFileName\nSaved to Downloads folder",
                    Toast.LENGTH_LONG
                ).show()
                onSuccess?.invoke(uri)
            }
            Result.success(uri)
        } catch (e: Exception) {
            android.util.Log.e("DocumentDownloadHelper", "Failed to download document: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "下載失敗 / Download failed: ${e.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
                onError?.invoke(e)
            }
            Result.failure(e)
        }
    }

    /**
     * Fetch bytes from Appwrite storage (with user session) or fallback to OkHttp
     */
    suspend fun fetchBytes(
        url: String,
        bucketId: String? = null,
        fileId: String? = null,
        clientProvider: AppwriteClientProvider? = null
    ): ByteArray = withContext(Dispatchers.IO) {
        // 1. Try Appwrite Storage first if bucketId and fileId are provided
        if (!bucketId.isNullOrBlank() && !fileId.isNullOrBlank() && clientProvider?.storage != null) {
            try {
                return@withContext clientProvider.storage.getFileDownload(bucketId, fileId)
            } catch (e: Exception) {
                android.util.Log.w("DocumentDownloadHelper", "Appwrite storage getFileDownload failed, trying HTTP: ${e.message}")
            }
        }

        // 2. Fallback to HTTP request
        val request = Request.Builder()
            .url(url)
            .addHeader("origin", "https://lingubible.com")
            .addHeader("User-Agent", "LingUBible-Android")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: ${response.message}")
            }
            response.body?.bytes() ?: throw IllegalStateException("Empty response body")
        }
    }

    /**
     * Cache document to app internal cache for in-app PDF rendering
     */
    suspend fun cacheDocumentLocally(
        context: Context,
        cacheKey: String,
        url: String,
        bucketId: String? = null,
        fileId: String? = null,
        clientProvider: AppwriteClientProvider? = null
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "pdf_cache")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val safeName = "doc_" + cacheKey.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".pdf"
        val targetFile = File(cacheDir, safeName)

        // If file already exists and is non-empty, reuse it
        if (targetFile.exists() && targetFile.length() > 0) {
            return@withContext targetFile
        }

        val bytes = fetchBytes(url, bucketId, fileId, clientProvider)
        FileOutputStream(targetFile).use { fos ->
            fos.write(bytes)
            fos.flush()
        }
        targetFile
    }

    /**
     * Save byte array to Android public Downloads directory
     */
    private fun saveToPublicDownloads(
        context: Context,
        fileName: String,
        bytes: ByteArray
    ): Uri? {
        val mimeType = "application/pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LingUBible")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("Failed to create MediaStore entry in Downloads")

            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(bytes)
                os.flush()
            } ?: throw IllegalStateException("Failed to open output stream for download")

            return uri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDir = File(downloadsDir, "LingUBible")
            if (!subDir.exists()) subDir.mkdirs()
            val file = File(subDir, fileName)
            FileOutputStream(file).use { os ->
                os.write(bytes)
                os.flush()
            }
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = uri
            }
            context.sendBroadcast(intent)
            return uri
        }
    }

    /**
     * Open or share file via system chooser
     */
    fun shareDocument(
        context: Context,
        file: File,
        title: String = "分享文件 / Share Document"
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    private fun sanitizeFileName(name: String): String {
        var clean = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (!clean.lowercase().endsWith(".pdf")) {
            clean += ".pdf"
        }
        return clean
    }
}
