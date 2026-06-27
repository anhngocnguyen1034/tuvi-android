package com.example.tuvi.domain.repository

import com.example.tuvi.domain.model.Quote

interface QuoteRepository {
    suspend fun getAll(): Result<List<Quote>>
}
