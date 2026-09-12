package com.lingubible.app.ui.common

import androidx.lifecycle.ViewModel
import com.lingubible.app.di.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MainUiState(
    val isLoading: Boolean = false,
    val appConfig: AppConfig = AppConfig(),
    val errorMessage: String? = null
)

class MainViewModel(
    private val appConfig: AppConfig = AppConfig()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState(appConfig = appConfig))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
}
