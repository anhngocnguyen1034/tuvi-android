package com.example.tuvi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhnn.language.LanguageDataSource
import com.anhnn.language.LanguageManager
import com.example.tuvi.R
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import kotlinx.coroutines.launch

/**
 * Màn chọn ngôn ngữ — chỉ 3 ngôn ngữ app hỗ trợ (Việt / Anh / Trung).
 *
 * Thư viện `com.anhnn.language` mặc định hiển thị 24 ngôn ngữ và không cho lọc,
 * nên ta tự dựng UI nhưng tái dùng enum [LanguageManager.Language] (tên + cờ) và
 * [LanguageDataSource] để lưu/áp dụng locale — khớp với cơ chế attachBaseContext.
 */
private val SUPPORTED_LANGUAGES = listOf(
    LanguageManager.Language.VIETNAMESE,
    LanguageManager.Language.ENGLISH,
    LanguageManager.Language.CHINESE_SIMPLIFIED,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerScreen(
    onBack: () -> Unit,
    onLanguageSaved: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataSource = remember { LanguageDataSource(context) }
    val currentCode by dataSource.languageCode.collectAsStateWithLifecycle(initialValue = "")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TuViNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_language),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = TuViIvory,
                    navigationIconContentColor = TuViGold
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SUPPORTED_LANGUAGES.forEach { lang ->
                LanguageRowItem(
                    language = lang,
                    selected = currentCode == lang.code,
                    onClick = {
                        if (currentCode != lang.code) {
                            scope.launch {
                                dataSource.setLanguageCode(lang.code)
                                onLanguageSaved()
                            }
                        } else {
                            onBack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageRowItem(
    language: LanguageManager.Language,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) TuViNavyLight else TuViNavyCard)
            .border(
                width = 1.dp,
                color = if (selected) TuViGold.copy(alpha = 0.6f) else TuViGold.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(language.flagResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Text(
            text = language.displayName,
            color = if (selected) TuViIvory else TuViIvoryDim,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = TuViGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
