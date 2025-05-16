package com.example.timemanagerforjob.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.timemanagerforjob.R
import com.example.timemanagerforjob.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class AuthRepository @Inject constructor( private val context: Context )
{ private val googleSignInClient: GoogleSignInClient by lazy {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(context.getString(R.string.web_client_id))
        .build()
    GoogleSignIn.getClient(context, gso) }

    fun getSignInIntent(): Intent {
        Log.d("AuthRepository", "Building GoogleSignInOptions")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.web_client_id))
            .build()
        Log.d("AuthRepository", "GoogleSignInOptions built: $gso")
        val client = GoogleSignIn.getClient(context, gso)
        Log.d("AuthRepository", "GoogleSignInClient created: $client")
        val intent = client.signInIntent
        Log.d("AuthRepository", "Sign-in intent created: $intent")
        return intent
    }

    suspend fun handleSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            Result.Success(account)
        } catch (e: ApiException) {
            Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun signOut(): Result<Unit> {
        return try {
            googleSignInClient.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun getCurrentUser(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

}