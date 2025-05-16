package com.example.timemanagerforjob.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.timemanagerforjob.domain.model.Result
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = authRepository.getCurrentUser()
        Log.d("AuthViewModel", "checkCurrentUser: currentUser = $currentUser")
        _uiState.value = AuthUiState(isAuthenticated = currentUser != null, user = currentUser)
    }

    fun startSignIn() {
        Log.d("AuthViewModel", "Starting sign-in process (mocked)")
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            _uiState.value = AuthUiState(
                isAuthenticated = true,
                user = null,
                isLoading = false
            )
        }
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Handling sign-in result, data: $data")
            when (val result = authRepository.handleSignInResult(data)) {
                is Result.Success -> {
                    Log.d("AuthViewModel", "Sign-in successful: ${result.value.email}")
                    _uiState.value = AuthUiState(
                        isAuthenticated = true,
                        user = result.value,
                        isLoading = false
                    )
                }
                is Result.Failure -> {
                    Log.e("AuthViewModel", "Sign-in failed", result.exception)
                    val errorMessage = when (result.exception) {
                        is ApiException -> "Google Sign-In failed: ${result.exception.statusCode}"
                        else -> "Sign-in failed: ${result.exception.message}"
                    }
                    _uiState.value = AuthUiState(
                        isAuthenticated = false,
                        errorMessage = errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSignInIntent() {
        _uiState.value = _uiState.value.copy(signInIntent = null)
    }
}