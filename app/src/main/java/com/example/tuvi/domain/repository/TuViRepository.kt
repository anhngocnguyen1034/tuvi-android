package com.example.tuvi.domain.repository

import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.model.TuViInterpretation

interface TuViRepository {
    suspend fun getTuViChart(input: TuViChartInput): TuViChart

    /** POST /api/interpret — chart in `data_la_so`, text in `ai_reading`. */
    suspend fun getTuViInterpretation(input: TuViChartInput): TuViInterpretation
}
