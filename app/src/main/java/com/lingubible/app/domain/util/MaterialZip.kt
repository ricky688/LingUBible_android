package com.lingubible.app.domain.util

import com.lingubible.app.domain.model.StudyMaterial
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

suspend fun writeMaterialsZip(output: OutputStream, materials: List<StudyMaterial>, download: suspend (String) -> ByteArray) {
    require(materials.isNotEmpty()) { "Select at least one file" }
    ZipOutputStream(output).use { zip ->
        materials.forEachIndexed { index, material ->
            currentCoroutineContext().ensureActive()
            val safe = material.name.replace(Regex("[^\\p{L}\\p{N} ._-]"), "_").trim('.', ' ').take(150).ifBlank { "file" }
            val bytes = download(material.id)
            zip.putNextEntry(ZipEntry("${index + 1}_$safe"))
            zip.write(bytes)
            zip.closeEntry()
        }
    }
}
