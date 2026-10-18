package com.lingubible.app

import com.lingubible.app.domain.model.*
import com.lingubible.app.data.repository.checkSupportResponse
import org.junit.Assert.*
import org.junit.Test

class SupportFormsTest {
    private val link = "https://www.lingubible.com/reset-password?userId=student.1&token=${"A".repeat(64)}"
    @Test fun resetLinksHaveStrictOriginPathAndUnambiguousBoundedCredentials() {
        assertEquals("student.1", parsePasswordResetLink(link)?.userId)
        assertNotNull(parsePasswordResetLink(link.replace("www.", "")))
        listOf(link.replace("https", "http"), link.replace(".com/", ".com.evil/"), link.replace("www.lingubible.com", "evil@www.lingubible.com"), link.replace("reset-password", "planner"), "$link&token=duplicate", "$link#fragment", link.replace("A".repeat(64), "short"), "$link&extra=1", link.replace(".com/", ".com:444/")).forEach { assertNull(it, parsePasswordResetLink(it)) }
        assertFalse(parsePasswordResetLink(link).toString().contains("student.1"))
    }
    @Test fun recoveryRequiresSchoolEmailAndWebPasswordRequirements() {
        assertTrue(validRecoveryEmail(" Student@LN.HK ")); assertTrue(validRecoveryEmail("staff@ln.edu.hk"))
        assertFalse(validRecoveryEmail("a@ln.hk.evil")); assertFalse(validRecoveryEmail("a@gmail.com"))
        assertTrue(validRecoveryPassword("Fj8!zQv2Lp9&"))
        listOf("Short2!", "aaaaaaaaaaa2!", "AAAAAAAAAAA2!", "Abcdefghijk!", "Abcdefghijk2", "Password123456!", "A1!".repeat(14)).forEach { assertFalse(it, validRecoveryPassword(it)) }
    }
    @Test fun contactValidationMatchesBackendWordBoundariesAndCapsInput() {
        val valid = ContactDraft("A Student", "a@example.com", "general", List(30) { "word" }.joinToString(" "))
        assertTrue(validContact(valid)); assertFalse(validContact(valid.copy(message="too short")))
        assertFalse(validContact(valid.copy(name=List(11) { "word" }.joinToString(" "))))
        assertFalse(validContact(valid.copy(type="invalid"))); assertFalse(validContact(valid.copy(email="a@b")))
    }
    @Test fun executionMustReportRealSuccessAndErrorsNeverExposeServerText() {
        checkSupportResponse(200, "{\"success\":true}")
        for ((code,body,reason) in listOf(Triple(429L,"{}",SupportError.RATE_LIMIT), Triple(400L,"{\"messageKey\":\"password.linkAlreadyUsed\"}",SupportError.INVALID_LINK),Triple(200L,"{\"success\":false,\"messageKey\":\"error.recaptchaFailed\"}",SupportError.CAPTCHA),Triple(200L,"{}",SupportError.SERVICE),Triple(500L,"{\"success\":true}",SupportError.SERVICE),Triple(200L,"not json",SupportError.SERVICE),Triple(200L,"{\"success\":\"true\"}",SupportError.SERVICE),Triple(200L,"{\"success\":{}}",SupportError.SERVICE))) {
            try { checkSupportResponse(code,body); fail("Must reject") } catch (e:SupportException) { assertEquals(reason,e.reason) }
        }
    }
}
