package com.example.tuvi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tuvi.R
import com.example.tuvi.domain.model.AuthUser
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight

/**
 * Card hiển thị tên + email + số dư token của user. Có slot [trailing] (mặc định trống)
 * cho phép caller chèn icon action (settings, logout, …) bên phải.
 */
@Composable
fun AccountProfileCard(
    user: AuthUser?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val displayName = user?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.settings_account_unknown_name)
    val email = user?.email
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.settings_account_unknown_email)
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TuViNavyLight.copy(alpha = 0.9f), TuViNavyCard.copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.35f), TuViGoldDark.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(TuViGold.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            if (!user?.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = initial,
                    color = TuViGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = TuViIvory,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = email,
                color = TuViIvoryDim,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (user != null) {
                Spacer(Modifier.height(4.dp))
                BalanceLine(tokens = user.tokens)
            }
        }
        trailing()
    }
}

@Composable
private fun BalanceLine(tokens: Int?) {
    val tokensText = tokens?.toString()
        ?: stringResource(R.string.settings_balance_dash)
    Text(
        text = tokenAnnotated(stringResource(R.string.settings_balance_format, tokensText)),
        inlineContent = tokenInlineContent(sizeSp = 13.sp),
        color = TuViGoldLight,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}
