package com.anhnn.tuvi.domain.usecase

import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViInterpretation
import com.anhnn.tuvi.domain.repository.TuViRepository

/** Luận giải vận hạn năm `input.namXem` qua POST /api/interpret/van-han. */
class GetTuViVanHanUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(input: TuViChartInput): Result<TuViInterpretation> =
        runCatching { repository.getTuViVanHan(input) }
}
