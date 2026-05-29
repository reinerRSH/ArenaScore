package com.example.arena.Model.Di.Auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context,
    private val serverClientId: String
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun getGoogleIdToken(): String? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        return if (credential is GoogleIdTokenCredential) {
            credential.idToken
        } else null
    }
}
