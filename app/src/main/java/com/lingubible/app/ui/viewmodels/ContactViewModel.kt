package com.lingubible.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.data.repository.SupportRepository
import com.lingubible.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ContactState(val verifying: Boolean = false, val sending: Boolean = false, val sent: Boolean = false, val error: SupportError? = null)
internal class ContactViewModel(private val repository: SupportRepository) : ViewModel() {
    private val state = MutableStateFlow(ContactState())
    val uiState = state.asStateFlow()
    private var pending: ContactDraft? = null
    fun begin(draft: ContactDraft) {
        if (state.value.verifying || state.value.sending) return
        if (!validContact(draft)) { state.value = ContactState(error = SupportError.CONTACT); return }
        pending = draft
        state.value = ContactState(verifying = true)
    }
    fun cancelVerification() { pending = null; state.value = ContactState() }
    fun verificationFailed() { pending = null; state.value = ContactState(error = SupportError.CAPTCHA) }
    fun send(captcha: String, language: String, dark: Boolean) {
        val draft = pending ?: return
        if (!state.value.verifying) return
        pending = null; state.value = ContactState(sending = true)
        viewModelScope.launch {
            val result = repository.contact(draft, captcha, language, dark)
            state.value = ContactState(sent = result.isSuccess, error = result.exceptionOrNull()?.supportError())
        }
    }
    fun reset() { if (!state.value.sending) { pending = null; state.value = ContactState() } }
}
