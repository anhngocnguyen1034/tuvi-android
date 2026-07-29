package com.anhnn.tuvi.domain.usecase

import com.anhnn.tuvi.domain.model.TuViChart
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.repository.TuViRepository

class GetTuViChartUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(input: TuViChartInput): Result<TuViChart> =
        runCatching { repository.getTuViChart(input) }
}
