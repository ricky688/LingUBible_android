package com.lingubible.app.domain.model

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

data class PublicDetailLink(val kind: String, val value: String, val tab: Int = 0)

fun parsePublicDetailLink(text: String): PublicDetailLink? = runCatching {
    require(text.length <= 2048)
    val uri = URI(text)
    require(uri.scheme.equals("https", true) && uri.host?.lowercase() in setOf("lingubible.com", "www.lingubible.com") &&
        uri.rawUserInfo == null && uri.port in setOf(-1, 443) && uri.rawQuery == null && uri.rawFragment == null)
    val parts = uri.rawPath.removePrefix("/").split('/')
    require(parts.size in 2..3 && parts[0] in setOf("courses", "instructors"))
    val tabs = if (parts[0] == "courses") mapOf("overview" to 0, "reviews" to 0, "grades" to 1, "exams" to 2, "teaching" to 3, "readings" to 4, "materials" to 5)
        else mapOf("courses" to 0, "reviews" to 1, "grades" to 2)
    val tab = if (parts.size == 3) requireNotNull(tabs[parts[2]]) else 0
    val value = URLDecoder.decode(parts[1].replace("+", "%2B"), "UTF-8").trim()
    require(value.length in 1..200 && value.none { it.isISOControl() || it in "/\\" })
    if (parts[0] == "courses") require(value.matches(Regex("[A-Za-z]{2,10}[0-9]{3,5}[A-Za-z]?")))
    PublicDetailLink(parts[0], if (parts[0] == "courses") value.uppercase() else value, tab)
}.getOrNull()

fun publicDetailUrl(kind: String, value: String): String {
    val url = "https://www.lingubible.com/$kind/${URLEncoder.encode(value.trim(), "UTF-8").replace("+", "%20")}"
    require(parsePublicDetailLink(url) != null)
    return url
}

fun addRecentQuery(history: List<String>, query: String): List<String> {
    val clean = query.trim()
    if (clean.isEmpty() || clean.length > 200 || clean.any { it.isISOControl() }) return history
    return (listOf(clean) + history.filterNot { it.equals(clean, true) }).take(20)
}
