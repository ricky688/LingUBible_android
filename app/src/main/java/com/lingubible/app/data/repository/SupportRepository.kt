package com.lingubible.app.data.repository

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

internal class SupportRepository(private val provider: AppwriteClientProvider) {
    suspend fun requestRecovery(email: String, captcha: String, language: String, dark: Boolean): Result<Unit> {
        if (!validRecoveryEmail(email)) return Result.failure(SupportException(SupportError.EMAIL))
        if (captcha.isBlank() || captcha.length > 8192) return Result.failure(SupportException(SupportError.CAPTCHA))
        return execute(buildJsonObject {
            put("action", "sendPasswordReset"); put("email", email.trim().lowercase()); put("recaptchaToken", captcha)
            put("language", language); put("theme", if (dark) "dark" else "light")
        })
    }
    suspend fun validateReset(link: String): Result<Unit> {
        val credentials = parsePasswordResetLink(link) ?: return Result.failure(SupportException(SupportError.INVALID_LINK))
        return execute(buildJsonObject { put("action", "validatePasswordResetToken"); put("userId", credentials.userId); put("token", credentials.token) })
    }
    suspend fun completeReset(link: String, password: String, confirmation: String): Result<Unit> {
        val credentials = parsePasswordResetLink(link) ?: return Result.failure(SupportException(SupportError.INVALID_LINK))
        if (!validRecoveryPassword(password)) return Result.failure(SupportException(SupportError.PASSWORD))
        if (password != confirmation) return Result.failure(SupportException(SupportError.MISMATCH))
        return execute(buildJsonObject { put("action", "completePasswordReset"); put("userId", credentials.userId); put("token", credentials.token); put("password", password) })
    }
    suspend fun contact(draft: ContactDraft, captcha: String, language: String, dark: Boolean): Result<Unit> {
        if (!validContact(draft)) return Result.failure(SupportException(SupportError.CONTACT))
        if (captcha.isBlank() || captcha.length > 8192) return Result.failure(SupportException(SupportError.CAPTCHA))
        return execute(buildJsonObject {
            put("action", "sendContactForm"); put("name", draft.name.trim()); put("email", draft.email.trim()); put("type", draft.type)
            put("message", draft.message.trim()); put("recaptchaToken", captcha); put("language", language); put("theme", if (dark) "dark" else "light")
        })
    }
    private suspend fun execute(body: JsonObject): Result<Unit> = try {
        val functions = provider.functions ?: throw SupportException(SupportError.SERVICE)
        val execution = functions.createExecution(functionId = "send-verification-email", body = body.toString(), async = false)
        checkSupportResponse(execution.responseStatusCode, execution.responseBody)
        Result.success(Unit)
    } catch (e: CancellationException) { throw e
    } catch (e: SupportException) { Result.failure(e)
    } catch (_: java.io.IOException) { Result.failure(SupportException(SupportError.NETWORK))
    } catch (_: Exception) { Result.failure(SupportException(SupportError.SERVICE)) }
}

internal fun checkSupportResponse(status: Long, body: String) {
    val result = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()
    val success = result?.get("success") as? JsonPrimitive
    if (status in 200..299 && success?.isString == false && success.booleanOrNull == true) return
    val key = (result?.get("messageKey") as? JsonPrimitive)?.contentOrNull.orEmpty()
    val reason = when {
        status == 429L || key.contains("rateLimit", true) -> SupportError.RATE_LIMIT
        key.contains("recaptcha", true) -> SupportError.CAPTCHA
        key in setOf("password.invalidToken", "password.tokenExpired", "password.linkAlreadyUsed") -> SupportError.INVALID_LINK
        key == "password.tooShort" -> SupportError.PASSWORD
        else -> SupportError.SERVICE
    }
    throw SupportException(reason)
}
