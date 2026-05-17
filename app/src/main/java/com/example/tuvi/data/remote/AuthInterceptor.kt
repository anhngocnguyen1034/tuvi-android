package com.example.tuvi.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Đính `Authorization: Bearer <Firebase ID token>` cho các request cần xác thực.
 * Lấy idToken mới mỗi lần (Firebase tự cache, chỉ làm mới khi sắp hết hạn).
 */
class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val user = firebaseAuth.currentUser
        if (user == null) return chain.proceed(original)

        val token = runCatching {
            runBlocking { user.getIdToken(false).await().token }
        }.getOrNull()

        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
