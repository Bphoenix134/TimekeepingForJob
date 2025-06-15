package com.example.timemanagerforjob.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.model.Result
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
        _uiState.value = _uiState.value.copy(
            isAuthenticated = currentUser != null,
            user = currentUser
        )
    }

    fun startSignIn(activity: Activity, isSignUp: Boolean = false) {
        Log.d("AuthViewModel", "Starting sign-in process, isSignUp: $isSignUp")
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(activity, isSignUp)) {
                is Result.Success -> {
                    Log.d("AuthViewModel", "Sign-in successful: ${result.value.id}")
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        user = result.value,
                        isLoading = false
                    )
                }
                is Result.Failure -> {
                    Log.e("AuthViewModel", "Sign-in failed", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = false,
                        errorMessage = result.exception.message ?: "Ошибка входа",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is Result.Success -> {
                    Log.d("AuthViewModel", "Sign-out successful")
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = false,
                        user = null,
                        errorMessage = null
                    )
                }
                is Result.Failure -> {
                    Log.e("AuthViewModel", "Sign-out failed", result.exception)
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exception.message ?: "Ошибка выхода"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}