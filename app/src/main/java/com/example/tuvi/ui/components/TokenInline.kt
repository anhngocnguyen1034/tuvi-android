package com.example.tuvi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.R

const val TOKEN_ICON_ID = "tk_icon"

/**
 * Trả về map inlineContent cho [Text] để render icon đồng xu (ic_token) thay cho
 * placeholder [TOKEN_ICON_ID]. Truyền vào [Text] cùng với AnnotatedString tạo bởi
 * [tokenAnnotated].
 */
@Composable
fun tokenInlineContent(sizeSp: TextUnit = 14.sp): Map<String, InlineTextContent> =
    mapOf(
        TOKEN_ICON_ID to InlineTextContent(
            Placeholder(
                width = sizeSp,
                height = sizeSp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            )
        ) {
            Image(
                painter = painterResource(R.drawable.ic_token),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(start = 1.dp),
            )
        }
    )

/**
 * Thay mọi cụm "token" trong [template] bằng inline icon (placeholder [TOKEN_ICON_ID]).
 * Khoảng trắng đứng ngay trước "token" cũng bị nuốt để icon dính sát với số.
 */
fun tokenAnnotated(template: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < template.length) {
        val idx = template.indexOf("token", i, ignoreCase = true)
        if (idx < 0) {
            append(template.substring(i))
            return@buildAnnotatedString
        }
        val cutTo = if (idx > 0 && template[idx - 1] == ' ') idx - 1 else idx
        append(template.substring(i, cutTo))
        append(' ')
        appendInlineContent(TOKEN_ICON_ID, "[token]")
        i = idx + "token".length
    }
}
