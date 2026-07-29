package com.anhnn.tuvi.domain.usecase

import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViInterpretation
import com.anhnn.tuvi.domain.repository.TuViRepository

/** Hỏi – đáp tự do về lá số: gửi `cauHoi` qua POST /api/interpret/hoi. */
class GetTuViHoiUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(
        input: TuViChartInput,
        cauHoi: String,
    ): Result<TuViInterpretation> =
        runCatching { repository.getTuViHoi(input, cauHoi) }
}
