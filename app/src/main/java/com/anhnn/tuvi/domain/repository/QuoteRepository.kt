package com.anhnn.tuvi.domain.repository

import com.anhnn.tuvi.domain.model.Quote

interface QuoteRepository {
    suspend fun getAll(): Result<List<Quote>>
}
