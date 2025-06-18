package com.example.timemanagerforjob.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.timemanagerforjob.R
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val credentialManager: CredentialManager
) {
    private fun generateNonce(): String {
        val randomString = UUID.randomUUID().toString()
        val bytes = randomString.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }

    suspend fun signInWithGoogle(activity: Activity, isSignUp: Boolean = false): Result<GoogleIdTokenCredential>{
        return withContext(Dispatchers.IO) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(!isSignUp)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setAutoSelectEnabled(true)
                    .setNonce(generateNonce())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    credential.id.let {
                        appPreferences.saveUserEmail(it)
                        appPreferences.saveIdToken(credential.idToken)
                    }
                    Log.d("AuthRepository", "Sign-in successful: ${credential.id}")
                    Result.Success(credential)
                } else {
                    Log.e("AuthRepository", "Unexpected credential type")
                    Result.Failure(Exception("Unexpected credential type"))
                }
            } catch (e: GetCredentialException) {
                Log.e("AuthRepository", "Google Sign-In failed: ${e.message}")
                Result.Failure(e)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("AuthRepository", "Invalid Google ID token: ${e.message}")
                Result.Failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error: ${e.message}")
                Result.Failure(e)
            }
        }
    }

    suspend fun trySilentSignIn(activity: Activity): Result<GoogleIdTokenCredential> {
        return withContext(Dispatchers.IO) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setAutoSelectEnabled(true)
                    .setNonce(generateNonce())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    appPreferences.saveUserEmail(credential.id)
                    appPreferences.saveIdToken(credential.idToken)
                    Log.d("AuthRepository", "Silent sign-in successful: ${credential.id}")
                    Result.Success(credential)
                } else {
                    Log.e("AuthRepository", "Unexpected credential type")
                    Result.Failure(Exception("Unexpected credential type"))
                }
            } catch (e: GetCredentialException) {
                Log.e("AuthRepository", "Silent sign-in failed: ${e.message}")
                Result.Failure(e)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("AuthRepository", "Invalid Google ID token: ${e.message}")
                Result.Failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected error: ${e.message}")
                Result.Failure(e)
            }
        }
    }

    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(request)
                appPreferences.saveUserEmail(null)
                appPreferences.saveIdToken(null)
                Log.d("AuthRepository", "Signed out successfully")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign out failed: ${e.message}")
                Result.Failure(e)
            }
        }
    }

    fun getCurrentUser(): GoogleIdTokenCredential? {
        val email = appPreferences.getUserEmail()
        val idToken = appPreferences.getIdToken()
        return if (email != null && idToken != null) {
            try {
                GoogleIdTokenCredential.Builder()
                    .setId(email)
                    .setIdToken(idToken)
                    .build()
            } catch (e: Exception) {
                Log.e("AuthRepository", "Invalid saved credential: ${e.message}")
                null
            }
        } else {
            null
        }
    }
}