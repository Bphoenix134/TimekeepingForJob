package com.example.timemanagerforjob.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.model.Result
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authEvent = MutableStateFlow<AuthEvent?>(null)
    val authEvent: StateFlow<AuthEvent?> = _authEvent.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        user = currentUser,
                        isLoading = false
                    )
                }
            } else {
                Log.d("AuthViewModel", "No current user found, requesting silent sign-in")
                _uiState.update { it.copy(isLoading = false) }
                _authEvent.value = AuthEvent.RequestSilentSignIn
            }
        }
    }

    fun performSignIn(activity: Activity, isSignUp: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activity, isSignUp)
            handleSignInResult(result)
        }
    }

    fun performSilentSignIn(activity: Activity) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.trySilentSignIn(activity)
            handleSignInResult(result)
        }
    }

    fun handleSignInResult(result: Result<GoogleIdTokenCredential>) {
        when (result) {
            is Result.Success -> {
                Log.d("AuthViewModel", "Sign-in successful: ${result.value.id}")
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        user = result.value,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            is Result.Failure -> {
                Log.e("AuthViewModel", "Sign-in failed", result.exception)
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        errorMessage = result.exception.message ?: "Ошибка входа",
                        isLoading = false
                    )
                }
                _authEvent.value = AuthEvent.ShowAuthScreen
            }
        }
    }

    fun startSignIn() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        _authEvent.value = AuthEvent.RequestSignIn
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

sealed class AuthEvent {
    object RequestSignIn : AuthEvent()
    object RequestSilentSignIn : AuthEvent()
    object ShowAuthScreen : AuthEvent()
}