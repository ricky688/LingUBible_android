package com.lingubible.app

import com.lingubible.app.data.repository.SupportRepository
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.viewmodels.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NativeSupportViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<SupportRepository>()
    private val link = "https://www.lingubible.com/reset-password?userId=student&token=${"A".repeat(64)}"
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun requestRequiresValidationAndVerificationAndDeduplicatesTaps() = runTest(dispatcher) {
        val gate = CompletableDeferred<Result<Unit>>()
        coEvery { repository.requestRecovery(any(),any(),any(),any()) } coAnswers { gate.await() }
        val vm = PasswordRecoveryViewModel(repository)
        vm.beginRequest("a@evil.com"); assertEquals(SupportError.EMAIL,vm.uiState.value.error)
        vm.send("captcha","en",false); coVerify(exactly=0) { repository.requestRecovery(any(),any(),any(),any()) }
        vm.beginRequest(" A@LN.HK "); assertEquals(RecoveryStage.VERIFYING,vm.uiState.value.stage)
        vm.send("captcha","en",false); vm.send("captcha","en",false); runCurrent()
        coVerify(exactly=1) { repository.requestRecovery("a@ln.hk","captcha","en",false) }
        gate.complete(Result.success(Unit)); advanceUntilIdle()
        assertEquals(RecoveryStage.SENT,vm.uiState.value.stage)
    }
    @Test fun verificationFailureAndNetworkFailureKeepRecoverableRequest() = runTest(dispatcher) {
        val vm = PasswordRecoveryViewModel(repository)
        vm.beginRequest("a@ln.hk"); vm.verificationFailed()
        assertEquals(SupportError.CAPTCHA,vm.uiState.value.error)
        coEvery { repository.requestRecovery(any(),any(),any(),any()) } returns Result.failure(SupportException(SupportError.NETWORK))
        vm.beginRequest("a@ln.hk"); vm.send("captcha","en",false); advanceUntilIdle()
        assertEquals(RecoveryStage.REQUEST,vm.uiState.value.stage); assertEquals("a@ln.hk",vm.uiState.value.email)
        assertEquals(SupportError.NETWORK,vm.uiState.value.error)
    }
    @Test fun resetValidatesLinkAndPasswordsBeforeSubmittingAndPreventsReuse() = runTest(dispatcher) {
        coEvery { repository.validateReset(link) } returns Result.success(Unit)
        coEvery { repository.completeReset(link, any(), any()) } returns Result.success(Unit)
        val vm = PasswordRecoveryViewModel(repository)
        vm.openLink("https://evil/reset-password"); assertEquals(SupportError.INVALID_LINK,vm.uiState.value.error)
        coVerify(exactly=0) { repository.validateReset(any()) }
        vm.openLink(link); advanceUntilIdle(); assertEquals(RecoveryStage.RESET,vm.uiState.value.stage)
        vm.complete("short", "short"); assertEquals(SupportError.PASSWORD,vm.uiState.value.error)
        vm.complete("Fj8!zQv2Lp9&", "different"); assertEquals(SupportError.MISMATCH,vm.uiState.value.error)
        coVerify(exactly=0) { repository.completeReset(any(),any(),any()) }
        vm.complete("Fj8!zQv2Lp9&","Fj8!zQv2Lp9&"); vm.complete("Fj8!zQv2Lp9&","Fj8!zQv2Lp9&"); advanceUntilIdle()
        assertEquals(RecoveryStage.COMPLETE,vm.uiState.value.stage)
        vm.complete("Fj8!zQv2Lp9&","Fj8!zQv2Lp9&"); coVerify(exactly=1) { repository.completeReset(any(),any(),any()) }
        vm.newRequest(); assertEquals(RecoveryStage.REQUEST,vm.uiState.value.stage)
    }
    @Test fun newRequestCancelsValidationAndFreshViewModelNeverRestoresASecret() = runTest(dispatcher) {
        coEvery { repository.validateReset(link) } coAnswers { delay(5000); Result.success(Unit) }
        val vm = PasswordRecoveryViewModel(repository)
        vm.openLink(link); runCurrent(); vm.newRequest(); advanceUntilIdle()
        assertEquals(RecoveryStage.REQUEST,vm.uiState.value.stage)
        assertEquals(RecoveryStage.REQUEST,PasswordRecoveryViewModel(repository).uiState.value.stage)
    }
    @Test fun expiredLinkReturnsToRequestAndCannotChangePassword() = runTest(dispatcher) {
        coEvery { repository.validateReset(link) } returns Result.failure(SupportException(SupportError.INVALID_LINK))
        val vm = PasswordRecoveryViewModel(repository)
        vm.openLink(link); advanceUntilIdle(); vm.complete("Fj8!zQv2Lp9&","Fj8!zQv2Lp9&")
        assertEquals(SupportError.INVALID_LINK,vm.uiState.value.error)
        coVerify(exactly=0) { repository.completeReset(any(),any(),any()) }
    }
    @Test fun contactNeedsVerificationAndOnlySuccessfulResponseMarksSent() = runTest(dispatcher) {
        val draft = ContactDraft("Student", "a@example.com", "bugReport", List(30) { "word" }.joinToString(" "))
        val vm = ContactViewModel(repository)
        vm.begin(draft.copy(message="short")); assertEquals(SupportError.CONTACT,vm.uiState.value.error)
        vm.send("captcha","en",false); coVerify(exactly=0) { repository.contact(any(),any(),any(),any()) }
        coEvery { repository.contact(draft,"captcha","en",false) } returns Result.failure(SupportException(SupportError.RATE_LIMIT))
        vm.begin(draft); vm.send("captcha","en",false); vm.send("captcha","en",false); advanceUntilIdle()
        coVerify(exactly=1) { repository.contact(any(),any(),any(),any()) }
        assertFalse(vm.uiState.value.sent); assertEquals(SupportError.RATE_LIMIT,vm.uiState.value.error)
        coEvery { repository.contact(draft,"captcha","en",false) } returns Result.success(Unit)
        vm.begin(draft); vm.send("captcha","en",false); advanceUntilIdle(); assertTrue(vm.uiState.value.sent)
        vm.reset(); assertFalse(vm.uiState.value.sent)
    }
}
