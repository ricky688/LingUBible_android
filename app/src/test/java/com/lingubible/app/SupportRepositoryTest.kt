package com.lingubible.app

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.SupportRepository
import com.lingubible.app.domain.model.*
import io.appwrite.models.Execution
import io.appwrite.services.Functions
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class SupportRepositoryTest {
    @Test fun nativeRequestsUseExistingCustomBackendActionsAndDoNotAcceptHttpSuccessAlone() = runTest {
        val provider = mockk<AppwriteClientProvider>()
        val functions = mockk<Functions>()
        every { provider.functions } returns functions
        val execution = mockk<Execution>()
        every { execution.responseStatusCode } returns 200L
        every { execution.responseBody } returns "{\"success\":true}"
        val bodies = mutableListOf<String?>()
        coEvery { functions.createExecution(functionId="send-verification-email", body=captureNullable(bodies), async=false) } returns execution
        val repo = SupportRepository(provider)
        assertTrue(repo.requestRecovery(" A@LN.HK ","captcha","zh-TW",true).isSuccess)
        var body = Json.parseToJsonElement(bodies.last()!!).jsonObject
        assertEquals("sendPasswordReset",body["action"]?.jsonPrimitive?.content)
        assertEquals("a@ln.hk",body["email"]?.jsonPrimitive?.content)
        assertEquals("dark",body["theme"]?.jsonPrimitive?.content)
        assertEquals("captcha",body["recaptchaToken"]?.jsonPrimitive?.content)
        val link = "https://www.lingubible.com/reset-password?userId=student&token=${"A".repeat(64)}"
        assertTrue(repo.validateReset(link).isSuccess)
        assertEquals("validatePasswordResetToken",Json.parseToJsonElement(bodies.last()!!).jsonObject["action"]?.jsonPrimitive?.content)
        assertTrue(repo.completeReset(link,"Fj8!zQv2Lp9&","Fj8!zQv2Lp9&").isSuccess)
        body = Json.parseToJsonElement(bodies.last()!!).jsonObject
        assertEquals("completePasswordReset",body["action"]?.jsonPrimitive?.content)
        assertEquals("Fj8!zQv2Lp9&",body["password"]?.jsonPrimitive?.content)
        every { execution.responseBody } returns "{\"success\":false}"
        assertTrue(repo.requestRecovery("a@ln.hk","captcha","en",false).isFailure)
    }
    @Test fun invalidInputsNeverCallFunctionAndMissingServiceFailsClosed() = runTest {
        val provider=mockk<AppwriteClientProvider>()
        every { provider.functions } returns null
        val repo=SupportRepository(provider)
        assertEquals(SupportError.EMAIL,(repo.requestRecovery("a@evil","captcha","en",false).exceptionOrNull() as SupportException).reason)
        assertEquals(SupportError.CAPTCHA,(repo.requestRecovery("a@ln.hk","","en",false).exceptionOrNull() as SupportException).reason)
        assertTrue(repo.validateReset("https://evil").isFailure)
        assertTrue(repo.requestRecovery("a@ln.hk","captcha","en",false).isFailure)
    }
}
