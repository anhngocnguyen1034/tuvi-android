package com.example.tuvi.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gắn header bảo vệ cho RIÊNG các endpoint AI tốn quota:
 *   X-Device-Id        : định danh thiết bị (backend tính lượt free theo id này)
 *   X-Integrity-Token  : Play Integrity token (nếu lấy được)
 *
 * Các endpoint khác (tính lá số, lịch…) không bị gắn header.
 */
class AiGateInterceptor(
    private val deviceIdProvider: () -> String,
    private val integrityProvider: PlayIntegrityProvider?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath !in AI_PATHS) {
            return chain.proceed(request)
        }

        val deviceId = deviceIdProvider()
        val builder = request.newBuilder().header("X-Device-Id", deviceId)

        val token = integrityProvider?.tokenForNonce(IntegrityNonce.forDeviceId(deviceId))
        if (!token.isNullOrEmpty()) {
            builder.header("X-Integrity-Token", token)
        }
        return chain.proceed(builder.build())
    }

    private companion object {
        val AI_PATHS = setOf(
            "/api/interpret",
            "/api/interpret/van-han",
            "/api/interpret/hoi",
            "/api/quota",
            "/api/iap/verify",
        )
    }
}
