package com.lingubible.app.domain.model

import java.net.URI
import java.net.URLDecoder

data class TimetableShare(val term: TimetableTerm, val sectionIds: Set<String>)

fun parseTimetableShare(text: String): Result<TimetableShare> = runCatching {
    require(text.trim().length <= 4096) { "連結過長 · Link is too long" }
    val uri = URI(text.trim())
    require(uri.scheme.equals("https", true) && uri.host?.lowercase() in setOf("lingubible.com", "www.lingubible.com") &&
        uri.rawUserInfo == null && uri.port in setOf(-1, 443) && uri.path == "/planner" && uri.fragment.isNullOrEmpty()) { "請貼上 LingUBible 課表連結 · Use a LingUBible timetable link" }
    val query = uri.rawQuery.orEmpty().split('&').filter { it.isNotBlank() }.map { part ->
        URLDecoder.decode(part.substringBefore('='), "UTF-8") to URLDecoder.decode(part.substringAfter('=', ""), "UTF-8")
    }
    require(query.count { it.first == "term" } == 1 && query.count { it.first == "sections" } == 1) { "連結資料不完整 · Incomplete link" }
    val term = DEFAULT_TERMS.firstOrNull { it.id == query.first { it.first == "term" }.second }
        ?: error("不支援此學期 · Unsupported term")
    val ids = query.first { it.first == "sections" }.second.split('.').toSet()
    require(ids.size in 1..60 && ids.all { it.matches(Regex("[A-Za-z0-9-]{1,32}")) }) { "課程編號無效（上限 60 個）· Invalid section IDs (maximum 60)" }
    TimetableShare(term, ids)
}

fun timetableShareLink(term: TimetableTerm, ids: Set<String>): String {
    require(DEFAULT_TERMS.any { it.id == term.id } && ids.size in 1..60 && ids.all { it.matches(Regex("[A-Za-z0-9-]{1,32}")) }) { "無法分享課表（上限 60 個時段）· Cannot share timetable (maximum 60 sections)" }
    return "https://www.lingubible.com/planner?term=${term.id}&sections=${ids.sorted().joinToString(".")}"
}

data class TimetableImportPreview(
    val term: TimetableTerm, val sections: List<TimetableSection>, val incomingIds: Set<String>,
    val newCount: Int, val existingCount: Int, val missingIds: Set<String>, val conflictCount: Int
)
