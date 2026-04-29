package com.example.tuvi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed

@Composable
fun SaveChartDialog(
    onDismiss: () -> Unit,
    onConfirm: (nhom: String) -> Unit
) {
    val defaultGroups = listOf(
        stringResource(R.string.save_chart_group_family),
        stringResource(R.string.save_chart_group_friends),
        stringResource(R.string.save_chart_group_colleagues),
        stringResource(R.string.save_chart_group_other)
    )
    val otherGroup = stringResource(R.string.save_chart_group_other)
    var selectedGroup by remember { mutableStateOf(defaultGroups.first()) }
    var customGroup by remember { mutableStateOf("") }
    val isCustom = selectedGroup == "__custom__"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TuViNavyCard)
                .border(1.dp, TuViGoldDark, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.save_chart_dialog_title),
                color = TuViGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.save_chart_dialog_subtitle),
                color = TuViIvoryDim,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))

            defaultGroups.forEach { group ->
                GroupOption(
                    label = group,
                    selected = selectedGroup == group,
                    onClick = { selectedGroup = group }
                )
                Spacer(Modifier.height(8.dp))
            }

            GroupOption(
                label = stringResource(R.string.save_chart_group_custom),
                selected = isCustom,
                onClick = { selectedGroup = "__custom__" }
            )

            if (isCustom) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = customGroup,
                    onValueChange = { customGroup = it },
                    placeholder = { Text(stringResource(R.string.save_chart_custom_hint), color = TuViIvoryDim) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TuViGold,
                        unfocusedBorderColor = TuViNavyLight,
                        focusedTextColor = TuViIvory,
                        unfocusedTextColor = TuViIvory,
                        cursorColor = TuViGold,
                        focusedContainerColor = TuViNavyLight,
                        unfocusedContainerColor = TuViNavyLight
                    ),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel), color = TuViIvoryDim)
                }
                TextButton(
                    onClick = {
                        val finalGroup = if (isCustom) customGroup.trim().ifBlank { otherGroup }
                        else selectedGroup
                        onConfirm(finalGroup)
                    }
                ) {
                    Text(stringResource(R.string.btn_save), color = TuViGold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GroupOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) TuViGoldDark.copy(alpha = 0.2f) else TuViNavyLight)
            .border(
                1.dp,
                if (selected) TuViGold else TuViNavyCard,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if (selected) TuViGoldLight else TuViIvory,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = TuViGold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
