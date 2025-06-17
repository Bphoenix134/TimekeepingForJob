package com.example.timemanagerforjob.auth

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val user: GoogleIdTokenCredential? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)