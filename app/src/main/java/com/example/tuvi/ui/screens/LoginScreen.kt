package com.example.tuvi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.ui.theme.BeVietnamProFamily
import com.example.tuvi.ui.theme.ChartDeepBg
import com.example.tuvi.ui.theme.ChartGold
import com.example.tuvi.ui.theme.ChartGoldDim
import com.example.tuvi.ui.theme.ChartIvory
import com.example.tuvi.ui.theme.ChartIvoryDim
import com.example.tuvi.ui.theme.ChartNavy
import com.example.tuvi.ui.theme.TuViTheme

@Composable
fun LoginScreen(
    loading: Boolean,
    onSignInWithGoogle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ChartNavy, ChartDeepBg, ChartDeepBg))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.login_title),
                color = ChartGold,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                fontFamily = BeVietnamProFamily,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                color = ChartIvoryDim,
                fontSize = 14.sp,
                fontFamily = BeVietnamProFamily,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSignInWithGoogle,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChartNavy.copy(alpha = 0.55f),
                    contentColor = ChartIvory,
                    disabledContainerColor = ChartNavy.copy(alpha = 0.30f),
                    disabledContentColor = ChartGoldDim.copy(alpha = 0.45f),
                ),
                border = BorderStroke(1.dp, ChartGold.copy(alpha = 0.80f)),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = ChartGold,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_google_btn),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        fontFamily = BeVietnamProFamily,
                    )
                }
            }
            Text(
                text = stringResource(R.string.login_disclaimer),
                color = ChartIvoryDim.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = BeVietnamProFamily,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Login - dark", showBackground = true)
@Composable
private fun LoginScreenPreview() {
    TuViTheme(darkTheme = true) {
        LoginScreen(loading = false, onSignInWithGoogle = {})
    }
}

@Preview(name = "Login loading - dark", showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    TuViTheme(darkTheme = true) {
        LoginScreen(loading = true, onSignInWithGoogle = {})
    }
}
