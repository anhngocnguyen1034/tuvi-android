package com.example.tuvi.presentation

import android.content.Context

fun TuViError.resolve(context: Context): String = when (this) {
    is TuViError.Res -> context.getString(resId)
    is TuViError.Raw -> message
}
