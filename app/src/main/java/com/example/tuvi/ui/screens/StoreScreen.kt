package com.example.tuvi.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.R
import com.example.tuvi.presentation.StoreEvent
import com.example.tuvi.presentation.StoreItem
import com.example.tuvi.presentation.StoreViewModel
import com.example.tuvi.ui.theme.BeVietnamProFamily
import com.example.tuvi.ui.theme.ChartDeepBg
import com.example.tuvi.ui.theme.ChartGold
import com.example.tuvi.ui.theme.ChartGoldDim
import com.example.tuvi.ui.theme.ChartIvory
import com.example.tuvi.ui.theme.ChartIvoryDim
import com.example.tuvi.ui.theme.ChartNavy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Màn cửa hàng nạp lượt AI: hiện các gói + giá Google Play, mua qua IAP. */
@Composable
fun StoreScreen(
    onBack: () -> Unit,
    viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val text = when (event) {
                is StoreEvent.CreditsGranted ->
                    context.getString(R.string.store_granted_toast, event.granted, event.remaining)
                StoreEvent.PurchasePending -> context.getString(R.string.store_pending_toast)
                StoreEvent.Cancelled -> context.getString(R.string.store_cancelled_toast)
                is StoreEvent.Failed -> context.getString(R.string.store_failed_toast)
            }
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ChartNavy, ChartDeepBg, ChartDeepBg))),
    ) {
        StoreTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.remaining?.let { remaining ->
                Text(
                    text = stringResource(R.string.store_remaining, remaining),
                    color = ChartIvory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BeVietnamProFamily,
                )
            }

            when {
                state.loading -> {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = ChartGold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                state.billingUnavailable -> CenteredHint(stringResource(R.string.store_unavailable))

                state.items.isEmpty() -> CenteredHint(stringResource(R.string.store_empty))

                else -> state.items.forEach { item ->
                    StoreItemCard(
                        item = item,
                        enabled = !state.purchaseInFlight,
                        onBuy = { context.findActivity()?.let { viewModel.buy(it, item) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItem,
    enabled: Boolean,
    onBuy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ChartNavy.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.store_credits, item.credits),
                color = ChartIvory,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = BeVietnamProFamily,
            )
            Text(
                text = item.priceText,
                color = ChartGoldDim,
                fontSize = 14.sp,
                fontFamily = BeVietnamProFamily,
            )
        }
        Button(
            onClick = onBuy,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChartNavy.copy(alpha = 0.6f),
                contentColor = ChartGold,
                disabledContainerColor = ChartNavy.copy(alpha = 0.25f),
                disabledContentColor = ChartGoldDim.copy(alpha = 0.45f),
            ),
            border = BorderStroke(1.dp, ChartGold.copy(alpha = 0.75f)),
        ) {
            Text(
                text = stringResource(R.string.store_buy_btn),
                fontWeight = FontWeight.SemiBold,
                fontFamily = BeVietnamProFamily,
            )
        }
    }
}

@Composable
private fun CenteredHint(text: String) {
    Spacer(Modifier.height(24.dp))
    Text(
        text = text,
        color = ChartIvoryDim,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        fontFamily = BeVietnamProFamily,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
}

@Composable
private fun StoreTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.settings_back),
                tint = ChartGold,
            )
        }
        Text(
            text = stringResource(R.string.store_title),
            color = ChartGold,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
