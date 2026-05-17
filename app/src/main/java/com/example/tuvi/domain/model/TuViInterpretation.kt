package com.example.tuvi.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TuViInterpretation(
    val chart: TuViChart,
    val aiReading: String,
    val tokensRemaining: Int? = null,
    val freeQuestionsRemaining: Int? = null,
)
