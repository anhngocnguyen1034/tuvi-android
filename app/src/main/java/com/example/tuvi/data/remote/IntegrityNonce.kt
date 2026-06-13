package com.example.tuvi.data.remote

import android.util.Base64
import java.security.MessageDigest

/**
 * nonce = base64url(SHA256(deviceId)) KHÔNG padding.
 * Phải khớp tuyệt đối với `play_integrity.expected_nonce(device_id)` ở backend,
 * nếu lệch backend sẽ trả 403 (nonce không khớp).
 */
object IntegrityNonce {
    fun forDeviceId(deviceId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(deviceId.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
        )
    }
}
