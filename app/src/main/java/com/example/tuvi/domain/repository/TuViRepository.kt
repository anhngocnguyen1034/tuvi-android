package com.example.tuvi.domain.repository

import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput

interface TuViRepository {
    suspend fun getTuViChart(input: TuViChartInput): TuViChart
}
