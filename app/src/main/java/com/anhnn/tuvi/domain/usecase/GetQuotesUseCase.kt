package com.anhnn.tuvi.domain.usecase

import com.anhnn.tuvi.domain.model.Quote
import com.anhnn.tuvi.domain.repository.QuoteRepository

class GetQuotesUseCase(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(): Result<List<Quote>> = repository.getAll()
}
