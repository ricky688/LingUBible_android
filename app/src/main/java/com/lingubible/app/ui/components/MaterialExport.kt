package com.lingubible.app.ui.components

import android.app.Activity
import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.lingubible.app.domain.model.StudyMaterial
import com.lingubible.app.domain.repository.AuthRepository
import com.lingubible.app.domain.repository.MaterialRepository
import com.lingubible.app.domain.util.writeMaterialsZip
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

internal class MaterialExport(val busy: Boolean, val error: String?, val clearError: () -> Unit, val download: (List<StudyMaterial>) -> Unit)

@Composable
internal fun rememberMaterialExport(bucket: String, zipName: String): MaterialExport {
    val context = LocalContext.current
    val repository = koinInject<MaterialRepository>()
    val auth = koinInject<AuthRepository>()
    val scope = rememberCoroutineScope()
    var pending by rememberSaveable(bucket) { mutableStateOf<String?>(null) }
    var owner by rememberSaveable(bucket) { mutableStateOf<String?>(null) }
    var writing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        val chosen = pending?.let { runCatching { Json.decodeFromString<List<StudyMaterial>>(it) }.getOrNull() }.orEmpty()
        val account = owner
        if (result.resultCode != Activity.RESULT_OK || uri == null || chosen.isEmpty()) {
            pending = null
        } else {
            pending = null
            writing = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        suspend fun bytes(id: String): ByteArray {
                            check(account != null && auth.currentUser.value?.id == account) { "登入狀態已變更 · Sign in again" }
                            val data = repository.downloadFileBytes(bucket, id).getOrThrow()
                            check(auth.currentUser.value?.id == account) { "登入狀態已變更 · Sign in again" }
                            return data
                        }
                        val output = requireNotNull(context.contentResolver.openOutputStream(uri, "wt"))
                        if (chosen.size == 1) output.use { it.write(bytes(chosen.single().id)) }
                        else writeMaterialsZip(output, chosen, ::bytes)
                    }
                    android.widget.Toast.makeText(context, "檔案已儲存 · Files saved", android.widget.Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    withContext(NonCancellable + Dispatchers.IO) { runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) } }
                    if (e is CancellationException) throw e
                    error = e.localizedMessage ?: "下載失敗 · Download failed"
                } finally { pending = null; writing = false }
            }
        }
    }
    return MaterialExport(writing || pending != null, error, { error = null }) { chosen ->
        if (chosen.isNotEmpty() && pending == null && !writing) {
            error = null
            val account = auth.currentUser.value?.id
            if (account == null) error = "請先登入 · Sign in first"
            else {
                owner = account
                pending = Json.encodeToString(chosen)
                val single = chosen.singleOrNull()
                val mime = if (single == null) "application/zip" else android.webkit.MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(single.name.substringAfterLast('.', "").lowercase()) ?: "application/octet-stream"
                try {
                    save.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = mime
                        putExtra(Intent.EXTRA_TITLE, (single?.name ?: zipName).replace('/', '_').replace('\\', '_'))
                    })
                } catch (e: Exception) { pending = null; error = e.localizedMessage ?: "無法開啟儲存位置 · Could not open file picker" }
            }
        }
    }
}
