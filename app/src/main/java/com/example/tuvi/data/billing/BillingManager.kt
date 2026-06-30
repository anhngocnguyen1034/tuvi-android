package com.example.tuvi.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Bọc Google Play Billing cho gói nạp lượt AI (sản phẩm tiêu hao — INAPP).
 *
 * Vòng đời mua: launchBillingFlow → PurchasesUpdatedListener phát [purchaseUpdates]
 * → tầng trên gọi backend /api/iap/verify để cấp credit → [consume] để cho phép
 * mua lại. [queryOwnedPurchases] dùng để khôi phục giao dịch chưa kịp verify.
 *
 * KHÔNG giữ tham chiếu Activity; chỉ nhận Activity tại thời điểm launch.
 */
class BillingManager(context: Context) {

    /** Phát ra mỗi lần Play cập nhật trạng thái mua. */
    sealed interface PurchaseEvent {
        data class Success(val purchases: List<Purchase>) : PurchaseEvent
        /** Người dùng huỷ hộp thoại mua. */
        object UserCancelled : PurchaseEvent
        data class Error(val code: Int, val message: String) : PurchaseEvent
    }

    private val _purchaseUpdates = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 8)
    val purchaseUpdates: SharedFlow<PurchaseEvent> = _purchaseUpdates.asSharedFlow()

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                _purchaseUpdates.tryEmit(PurchaseEvent.Success(purchases.orEmpty()))
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _purchaseUpdates.tryEmit(PurchaseEvent.UserCancelled)
            else ->
                _purchaseUpdates.tryEmit(
                    PurchaseEvent.Error(result.responseCode, result.debugMessage)
                )
        }
    }

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(purchasesListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    /** Bảo đảm đã kết nối tới Play. Trả về true nếu sẵn sàng. */
    suspend fun ensureConnected(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                private var resumed = false
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (resumed) return
                    resumed = true
                    cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (resumed) return
                    resumed = true
                    cont.resume(false)
                }
            })
        }
    }

    /** Hỏi Play chi tiết các gói (giá đã bản địa hoá) theo danh sách product id. */
    suspend fun queryProducts(productIds: List<String>): List<ProductDetails> {
        if (productIds.isEmpty() || !ensureConnected()) return emptyList()
        val products = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        val result = client.queryProductDetails(params)
        return result.productDetailsList.orEmpty()
    }

    /** Mở hộp thoại mua cho một gói. Kết quả phát qua [purchaseUpdates]. */
    fun launchPurchase(activity: Activity, product: ProductDetails): BillingResult {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        return client.launchBillingFlow(activity, params)
    }

    /** Tiêu thụ giao dịch (sau khi backend đã cấp credit) để cho phép mua lại. */
    suspend fun consume(purchaseToken: String): Boolean {
        if (!ensureConnected()) return false
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        val result = client.consumePurchase(params)
        return result.billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }

    /**
     * Xác nhận giao dịch (chỉ cần khi KHÔNG tiêu thụ). Với gói tiêu hao ta dùng
     * [consume]; hàm này để phòng trường hợp cần acknowledge trước verify.
     */
    suspend fun acknowledge(purchaseToken: String): Boolean {
        if (!ensureConnected()) return false
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return suspendCancellableCoroutine { cont ->
            client.acknowledgePurchase(params) { result ->
                cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
            }
        }
    }

    /** Liệt kê các giao dịch INAPP đang sở hữu (để khôi phục verify còn dang dở). */
    suspend fun queryOwnedPurchases(): List<Purchase> {
        if (!ensureConnected()) return emptyList()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = client.queryPurchasesAsync(params)
        return result.purchasesList
    }

    fun close() {
        if (client.isReady) client.endConnection()
    }
}
