package com.anhnn.tuvi.domain.usecase

import com.anhnn.tuvi.domain.model.CungSlug
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViInterpretation
import com.anhnn.tuvi.domain.repository.TuViRepository

class GetTuViInterpretUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(
        input: TuViChartInput,
        cung: CungSlug,
    ): Result<TuViInterpretation> =
        runCatching { repository.getTuViInterpretation(input, cung) }
}
