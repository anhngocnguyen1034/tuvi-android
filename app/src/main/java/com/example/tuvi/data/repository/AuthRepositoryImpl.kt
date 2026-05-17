package com.example.tuvi.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.tuvi.R
import com.example.tuvi.data.remote.TuViApiService
import com.example.tuvi.data.remote.dto.LoginRequest
import com.example.tuvi.domain.model.AuthUser
import com.example.tuvi.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val apiService: TuViApiService,
) : AuthRepository {

    override fun currentUser(): AuthUser? = firebaseAuth.currentUser?.toDomain()

    override suspend fun signInWithGoogle(context: Context): AuthUser = withContext(Dispatchers.IO) {
        val webClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)
        val response = credentialManager.getCredential(context, request)
        val credential = response.credential
        if (credential !is CustomCredential || credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw IllegalStateException("Unexpected credential type: ${credential.type}")
        }
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

        val firebaseCred = GoogleAuthProvider.getCredential(googleIdToken, null)
        val firebaseUser = firebaseAuth.signInWithCredential(firebaseCred).await().user
            ?: throw IllegalStateException("Firebase did not return a user")

        // Best-effort: bắn idToken lên backend; KHÔNG block luồng login nếu backend chưa sẵn sàng.
        val loginResult = runCatching {
            apiService.login(LoginRequest(idToken = googleIdToken))
        }.onFailure { Log.w(TAG, "POST /api/login failed (frontend-first): ${it.message}") }

        val base = firebaseUser.toDomain()
        loginResult.getOrNull()?.let { resp ->
            base.copy(tokens = resp.tokens, freeQuestions = resp.freeQuestions)
        } ?: base
    }

    override suspend fun refreshProfile(): AuthUser? = withContext(Dispatchers.IO) {
        val firebaseUser = firebaseAuth.currentUser ?: return@withContext null
        runCatching { apiService.getMe() }
            .onFailure { Log.w(TAG, "GET /api/me failed: ${it.message}") }
            .getOrNull()
            ?.let { me ->
                firebaseUser.toDomain().copy(
                    tokens = me.tokens,
                    freeQuestions = me.freeQuestions,
                    aiQuestionCost = me.aiQuestionCost,
                )
            }
    }

    override suspend fun signOut(context: Context) = withContext(Dispatchers.IO) {
        firebaseAuth.signOut()
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }.onFailure {
            Log.w(TAG, "Clear credential state failed: ${it.message}")
        }
        Unit
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomain() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )

    private companion object {
        const val TAG = "AuthRepository"
    }
}
