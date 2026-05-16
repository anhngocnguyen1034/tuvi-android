package com.example.tuvi.domain.repository

import android.content.Context
import com.example.tuvi.domain.model.AuthUser

interface AuthRepository {
    /** Người dùng đã đăng nhập (Firebase) hay chưa. */
    fun currentUser(): AuthUser?

    /**
     * Mở Credential Manager để chọn tài khoản Google → đổi idToken lấy FirebaseAuth →
     * best-effort POST `/api/login` để backend tạo/cập nhật user.
     * Throw nếu user huỷ chọn account hoặc Firebase trả lỗi.
     */
    suspend fun signInWithGoogle(context: Context): AuthUser

    suspend fun signOut(context: Context)
}
