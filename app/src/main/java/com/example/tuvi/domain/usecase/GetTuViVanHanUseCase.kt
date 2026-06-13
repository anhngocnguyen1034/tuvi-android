package com.example.tuvi.domain.usecase

import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.model.TuViInterpretation
import com.example.tuvi.domain.repository.TuViRepository

/** Luận giải vận hạn năm `input.namXem` qua POST /api/interpret/van-han. */
class GetTuViVanHanUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(input: TuViChartInput): Result<TuViInterpretation> =
        runCatching { repository.getTuViVanHan(input) }
}
