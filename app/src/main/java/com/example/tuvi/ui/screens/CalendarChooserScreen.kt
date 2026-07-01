package com.example.tuvi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViNavy

@Composable
fun CalendarChooserScreen(
    onBack: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuViNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.settings_back),
                tint = TuViGold
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.calendar_chooser_title),
            color = TuViGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.calendar_chooser_message),
            color = TuViIvory,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onOpenCalendar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TuViGold,
                contentColor = TuViNavy
            )
        ) {
            Text(stringResource(R.string.calendar_chooser_btn), fontWeight = FontWeight.SemiBold)
        }
    }
}
