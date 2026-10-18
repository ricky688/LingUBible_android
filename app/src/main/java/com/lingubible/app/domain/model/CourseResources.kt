package com.lingubible.app.domain.model

import kotlinx.serialization.json.*

@kotlinx.serialization.Serializable
data class StudyMaterial(val id: String, val name: String, val size: Long) {
    val isPdf get() = name.endsWith(".pdf", ignoreCase = true)
    val displayName get() = name.replaceFirst(Regex("^[A-Za-z]+[0-9]+[_ .-]?"), "").substringBeforeLast('.')
}
data class CourseReading(val title: String, val source: String, val required: Boolean, val kind: String, val url: String?)

fun materialCourseCode(name: String): String? = Regex("^([A-Za-z]+[0-9]+)(?=[_ .-])").find(name)?.groupValues?.get(1)?.uppercase()

fun parseCourseReadings(raw: String): List<CourseReading> {
    val entries = runCatching { Json.parseToJsonElement(raw).jsonArray }.getOrNull() ?: return emptyList()
    return entries.mapNotNull { entry ->
        val item = entry as? JsonObject ?: return@mapNotNull null
        fun field(key: String) = (item[key] as? JsonPrimitive)?.contentOrNull?.trim().orEmpty()
        val source = field("SOURCE"); val title = field("FULL_NAME").ifBlank { source }
        if (title.isBlank()) return@mapNotNull null
        val text = "$source $title"
        val doi = (Regex("(?:https?://)?(?:www\\.|dx\\.)?doi\\.org/(\\S+)", RegexOption.IGNORE_CASE).find(text)
            ?: Regex("https?://doi-org(?:[.-][a-z0-9-]+)*\\.oclc\\.org/(\\S+)", RegexOption.IGNORE_CASE).find(text))?.groupValues?.get(1)?.let { found ->
            var value = found
            while (value.isNotEmpty()) {
                val end = value.last()
                val opener = mapOf(')' to '(', ']' to '[', '}' to '{')[end]
                if (opener != null) {
                    if (value.count { it == end } <= value.count { it == opener }) break
                } else if (end !in ".,;:>\"’”") break
                value = value.dropLast(1)
            }
            value.takeIf { it.isNotEmpty() }
        }
        val url = when {
            doi != null -> "https://doi.org/$doi"
            source.startsWith("https://", true) || source.startsWith("http://", true) -> source
            source.startsWith("www.", true) -> "https://$source"
            else -> null
        }
        val type = field("TYPE").lowercase()
        CourseReading(title, source, "required" in type || "essential" in type, if (doi != null) "論文 Article" else if (url != null) "網站 Website" else "書籍 Book", url)
    }
}
