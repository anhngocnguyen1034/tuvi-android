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

    /**
     * Gọi GET /api/me để lấy số dư token, số câu hỏi miễn phí và đơn giá AI mới nhất.
     * Trả về null nếu chưa đăng nhập hoặc backend không phản hồi.
     */
    suspend fun refreshProfile(): AuthUser?
}
