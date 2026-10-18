package com.lingubible.app.domain.model

import com.lingubible.app.domain.util.EmailValidator
import java.net.URI
import java.net.URLDecoder

internal class ResetCredentials(val userId: String, val token: String) {
    override fun toString() = "ResetCredentials([redacted])"
}

internal fun parsePasswordResetLink(value: String): ResetCredentials? = runCatching {
    require(value.length <= 2048)
    val uri = URI(value.trim())
    require(uri.scheme == "https" && uri.host in setOf("lingubible.com", "www.lingubible.com"))
    require(uri.port in setOf(-1, 443) && uri.userInfo == null && uri.fragment == null && uri.path == "/reset-password")
    val pairs = uri.rawQuery.orEmpty().split('&').map { part ->
        val pair = part.split('=', limit = 2)
        require(pair.size == 2)
        URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair[1], "UTF-8")
    }
    require(pairs.size == 2 && pairs.map { it.first }.toSet() == setOf("userId", "token"))
    val values = pairs.toMap()
    val id = values.getValue("userId")
    val token = values.getValue("token")
    require(id.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,35}")))
    require(token.matches(Regex("[A-Za-z0-9_-]{64}")))
    ResetCredentials(id, token)
}.getOrNull()

internal fun validRecoveryEmail(email: String) = email.trim().length <= 254 && EmailValidator.isValidLingnanEmail(email)

// Matches the web reset form's required rules; the server still validates the token and password.
internal fun validRecoveryPassword(password: String): Boolean {
    val common = listOf("password", "123456", "123456789", "qwerty", "abc123", "admin", "letmein", "welcome", "monkey", "dragon", "master", "hello", "login", "root", "pass", "test", "guest", "user", "demo", "sample", "lingnan", "student", "university", "college", "school")
    return password.length in 12..40 && password.any { it in 'A'..'Z' } && password.any { it in 'a'..'z' } &&
        password.any { it in '0'..'9' } && password.any { it in "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?" } &&
        common.none { password.contains(it, ignoreCase = true) }
}

internal enum class SupportError { EMAIL, PASSWORD, MISMATCH, INVALID_LINK, RATE_LIMIT, CAPTCHA, CONTACT, NETWORK, SERVICE }
internal class SupportException(val reason: SupportError) : Exception(reason.name)

internal data class ContactDraft(val name: String = "", val email: String = "", val type: String = "general", val message: String = "")
internal val CONTACT_TYPES = listOf("general", "bugReport", "commentReport", "accountIssue", "featureRequest", "other")
internal fun validContact(draft: ContactDraft) = draft.name.trim().isNotEmpty() && draft.name.length <= 200 &&
    draft.name.trim().split(Regex("\\s+")).size <= 10 && draft.email.length <= 254 &&
    draft.email.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) && draft.type in CONTACT_TYPES &&
    draft.message.length <= 10000 && draft.message.trim().split(Regex("\\s+")).size >= 30
