package com.example.tuvi.domain.usecase

import com.example.tuvi.domain.model.CungSlug
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.model.TuViInterpretation
import com.example.tuvi.domain.repository.TuViRepository

class GetTuViInterpretUseCase(private val repository: TuViRepository) {
    suspend operator fun invoke(
        input: TuViChartInput,
        cung: CungSlug,
    ): Result<TuViInterpretation> =
        runCatching { repository.getTuViInterpretation(input, cung) }
}
