package com.example.tuvi.domain.usecase

import com.example.tuvi.domain.model.Quote
import com.example.tuvi.domain.repository.QuoteRepository

class GetQuotesUseCase(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(): Result<List<Quote>> = repository.getAll()
}
