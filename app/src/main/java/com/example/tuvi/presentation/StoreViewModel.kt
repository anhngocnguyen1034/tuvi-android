package com.example.tuvi.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.example.tuvi.data.billing.BillingManager
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.usecase.GetIapProductsUseCase
import com.example.tuvi.domain.usecase.GetQuotaUseCase
import com.example.tuvi.domain.usecase.VerifyPurchaseUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Một gói nạp lượt hiển thị trên màn cửa hàng: credit (backend) + giá (Play). */
data class StoreItem(
    val productId: String,
    val credits: Int,
    val priceText: String,
    val details: ProductDetails,
)

data class StoreUiState(
    val loading: Boolean = true,
    val items: List<StoreItem> = emptyList(),
    val remaining: Int? = null,
    val purchaseInFlight: Boolean = false,
    /** true khi Play/billing không khả dụng (thiết bị thiếu Play, hoặc lỗi kết nối). */
    val billingUnavailable: Boolean = false,
)

/** Sự kiện một lần để màn hình hiện Toast/Snackbar. */
sealed interface StoreEvent {
    data class CreditsGranted(val granted: Int, val remaining: Int) : StoreEvent
    object PurchasePending : StoreEvent
    object Cancelled : StoreEvent
    data class Failed(val reason: String?) : StoreEvent
}

class StoreViewModel(
    private val billing: BillingManager,
    private val getIapProducts: GetIapProductsUseCase,
    private val getQuota: GetQuotaUseCase,
    private val verifyPurchase: VerifyPurchaseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StoreEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<StoreEvent> = _events.asSharedFlow()

    init {
        observePurchaseUpdates()
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)

            // Số dư hiện tại (không chặn nếu lỗi mạng).
            getQuota().onSuccess { q ->
                _uiState.value = _uiState.value.copy(remaining = q.remaining)
            }

            val catalog = getIapProducts().getOrElse {
                _uiState.value = _uiState.value.copy(loading = false)
                _events.tryEmit(StoreEvent.Failed(it.message))
                return@launch
            }
            val creditsById = catalog.associate { it.productId to it.credits }

            val connected = billing.ensureConnected()
            if (!connected) {
                _uiState.value = _uiState.value.copy(loading = false, billingUnavailable = true)
                return@launch
            }

            val details = billing.queryProducts(catalog.map { it.productId })
            val items = details.mapNotNull { d ->
                val credits = creditsById[d.productId] ?: return@mapNotNull null
                val price = d.oneTimePurchaseOfferDetails?.formattedPrice ?: return@mapNotNull null
                StoreItem(d.productId, credits, price, d)
            }.sortedBy { it.credits }

            _uiState.value = _uiState.value.copy(loading = false, items = items)

            // Khôi phục giao dịch đã mua nhưng chưa verify/consume xong (vd app chết giữa chừng).
            reconcileOwnedPurchases()
        }
    }

    fun buy(activity: Activity, item: StoreItem) {
        _uiState.value = _uiState.value.copy(purchaseInFlight = true)
        val result = billing.launchPurchase(activity, item.details)
        if (result.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
            _uiState.value = _uiState.value.copy(purchaseInFlight = false)
            _events.tryEmit(StoreEvent.Failed(result.debugMessage))
        }
    }

    private fun observePurchaseUpdates() {
        viewModelScope.launch {
            billing.purchaseUpdates.collect { event ->
                when (event) {
                    is BillingManager.PurchaseEvent.Success -> handlePurchases(event.purchases)
                    is BillingManager.PurchaseEvent.UserCancelled -> {
                        _uiState.value = _uiState.value.copy(purchaseInFlight = false)
                        _events.tryEmit(StoreEvent.Cancelled)
                    }
                    is BillingManager.PurchaseEvent.Error -> {
                        _uiState.value = _uiState.value.copy(purchaseInFlight = false)
                        _events.tryEmit(StoreEvent.Failed(event.message))
                    }
                }
            }
        }
    }

    private suspend fun reconcileOwnedPurchases() {
        val owned = billing.queryOwnedPurchases()
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (owned.isNotEmpty()) handlePurchases(owned)
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        viewModelScope.launch {
            for (purchase in purchases) {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> grantAndConsume(purchase)
                    Purchase.PurchaseState.PENDING -> _events.tryEmit(StoreEvent.PurchasePending)
                }
            }
            _uiState.value = _uiState.value.copy(purchaseInFlight = false)
        }
    }

    /** Verify với backend (cấp credit) rồi consume để cho phép mua lại. */
    private suspend fun grantAndConsume(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        verifyPurchase(productId, purchase.purchaseToken)
            .onSuccess { result ->
                // Consume sau khi đã cấp credit thành công → cho phép mua tiếp.
                billing.consume(purchase.purchaseToken)
                _uiState.value = _uiState.value.copy(remaining = result.remaining)
                _events.tryEmit(StoreEvent.CreditsGranted(result.granted, result.remaining))
            }
            .onFailure {
                // Giữ nguyên giao dịch (chưa consume) để lần sau reconcile thử lại.
                _events.tryEmit(StoreEvent.Failed(it.message))
            }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StoreViewModel(
                    AppContainer.billingManager,
                    AppContainer.getIapProductsUseCase,
                    AppContainer.getQuotaUseCase,
                    AppContainer.verifyPurchaseUseCase,
                ) as T
            }
        }
    }
}
