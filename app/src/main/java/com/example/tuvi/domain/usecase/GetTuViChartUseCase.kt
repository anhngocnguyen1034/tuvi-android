package com.example.tuvi.domain.usecase

import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.repository.TuViRepository

class GetTuViChartUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(input: TuViChartInput): Result<TuViChart> =
        runCatching { repository.getTuViChart(input) }
}
