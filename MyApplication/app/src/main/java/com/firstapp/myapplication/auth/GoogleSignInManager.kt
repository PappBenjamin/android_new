package com.firstapp.myapplication.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.firstapp.myapplication.R
import kotlinx.coroutines.tasks.await

class GoogleSignInManager(context: Context) {

    private val googleSignInClient: GoogleSignInClient
    private val context = context

    init {
        // Configure Google Sign In with OAuth 2.0
        val webClientId = context.getString(R.string.google_web_client_id)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)  // Request ID Token for OAuth
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the sign-in intent to launch the Google Sign-In flow
     */
    fun getSignInIntent() = googleSignInClient.signInIntent

    /**
     * Handle the result from Google Sign-In and extract OAuth credentials
     */

    suspend fun handleSignInResult(data: Intent?): GoogleSignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()

            // Extract OAuth credentials
            GoogleSignInResult.Success(
                idToken = account.idToken,  // OAuth ID Token
                accessToken = account.serverAuthCode,  // Server auth code for backend
                email = account.email,
                name = account.displayName,
                photoUrl = account.photoUrl?.toString()
            )
        } catch (e: ApiException) {
            GoogleSignInResult.Error("Sign in failed: ${e.message}")
        } catch (e: Exception) {
            GoogleSignInResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Get the currently signed-in account
     */

    fun getSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Sign out the current user
     */
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Revoke access
     */

    suspend fun revokeAccess() {
        try {
            googleSignInClient.revokeAccess().await()
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String?,           // OAuth ID Token
        val accessToken: String?,       // Server auth code
        val email: String?,
        val name: String?,
        val photoUrl: String?
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
}
