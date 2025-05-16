package com.example.timemanagerforjob.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val user: GoogleSignInAccount? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signInIntent: Intent? = null
)