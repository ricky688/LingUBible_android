package com.lingubible.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.data.repository.SupportRepository
import com.lingubible.app.domain.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal enum class RecoveryStage { REQUEST, VERIFYING, SENDING, SENT, VALIDATING, RESET, COMPLETE }
internal data class RecoveryState(val stage: RecoveryStage = RecoveryStage.REQUEST, val email: String = "", val error: SupportError? = null, val verificationId: Int = 0)

internal class PasswordRecoveryViewModel(private val repository: SupportRepository) : ViewModel() {
    private val state = MutableStateFlow(RecoveryState())
    val uiState = state.asStateFlow()
    private var resetLink: String? = null
    private var job: Job? = null
    private var generation = 0
    private var verificationId = 0

    fun newRequest() {
        job?.cancel(); ++generation; resetLink = null
        state.value = RecoveryState(email = state.value.email)
    }
    fun beginRequest(email: String) {
        if (state.value.stage !in setOf(RecoveryStage.REQUEST, RecoveryStage.SENT)) return
        if (!validRecoveryEmail(email)) { state.update { it.copy(error = SupportError.EMAIL) }; return }
        state.value = RecoveryState(RecoveryStage.VERIFYING, email.trim().lowercase(), verificationId = ++verificationId)
    }
    fun verificationFailed() {
        if (state.value.stage == RecoveryStage.VERIFYING) state.update { it.copy(stage = RecoveryStage.REQUEST, error = SupportError.CAPTCHA) }
    }
    fun send(captcha: String, language: String, dark: Boolean) {
        if (state.value.stage != RecoveryStage.VERIFYING) return
        val run = generation
        state.update { it.copy(stage = RecoveryStage.SENDING) }
        job = viewModelScope.launch {
            val result = repository.requestRecovery(state.value.email, captcha, language, dark)
            if (run == generation) state.update { it.copy(stage = if (result.isSuccess) RecoveryStage.SENT else RecoveryStage.REQUEST, error = result.exceptionOrNull()?.supportError()) }
        }
    }
    fun openLink(link: String) {
        job?.cancel(); val run = ++generation; resetLink = null
        if (parsePasswordResetLink(link) == null) { state.value = RecoveryState(email = state.value.email, error = SupportError.INVALID_LINK); return }
        resetLink = link.trim()
        state.value = RecoveryState(stage = RecoveryStage.VALIDATING, email = state.value.email)
        job = viewModelScope.launch {
            val result = repository.validateReset(link)
            if (run == generation) {
                if (result.isFailure) resetLink = null
                state.update { it.copy(stage = if (result.isSuccess) RecoveryStage.RESET else RecoveryStage.REQUEST, error = result.exceptionOrNull()?.supportError()) }
            }
        }
    }
    fun complete(password: String, confirmation: String) {
        if (state.value.stage != RecoveryStage.RESET) return
        val link = resetLink ?: return
        val error = when { !validRecoveryPassword(password) -> SupportError.PASSWORD; password != confirmation -> SupportError.MISMATCH; else -> null }
        if (error != null) { state.update { it.copy(error = error) }; return }
        val run = generation
        state.update { it.copy(stage = RecoveryStage.SENDING, error = null) }
        job = viewModelScope.launch {
            val result = repository.completeReset(link, password, confirmation)
            if (run == generation) {
                val failure = result.exceptionOrNull()?.supportError()
                if (result.isSuccess || failure == SupportError.INVALID_LINK) resetLink = null
                state.update { it.copy(stage = if (result.isSuccess) RecoveryStage.COMPLETE else if (failure == SupportError.INVALID_LINK) RecoveryStage.REQUEST else RecoveryStage.RESET, error = failure) }
            }
        }
    }
}

internal fun Throwable.supportError() = (this as? SupportException)?.reason ?: SupportError.SERVICE
