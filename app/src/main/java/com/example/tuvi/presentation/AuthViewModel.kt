package com.example.tuvi.presentation

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.AuthUser
import com.example.tuvi.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState

    @Immutable
    data class SignedIn(val user: AuthUser) : AuthUiState

    @Immutable
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(
        authRepository.currentUser()?.let(AuthUiState::SignedIn) ?: AuthUiState.Idle,
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isSignedIn: Boolean get() = authRepository.currentUser() != null

    fun signInWithGoogle(context: Context) {
        if (_uiState.value is AuthUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { authRepository.signInWithGoogle(context) }
                .onSuccess { _uiState.value = AuthUiState.SignedIn(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message.orEmpty()) }
        }
    }

    fun signOut(context: Context) {
        _uiState.value = AuthUiState.Idle
        viewModelScope.launch {
            authRepository.signOut(context)
        }
    }

    /** Cho UI clear lỗi sau khi đã hiển thị toast. */
    fun consumeError() {
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthViewModel(AppContainer.authRepository) }
        }
    }
}
