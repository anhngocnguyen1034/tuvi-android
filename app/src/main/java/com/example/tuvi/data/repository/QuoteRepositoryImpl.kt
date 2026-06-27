package com.example.tuvi.data.repository

import android.content.Context
import com.example.tuvi.data.local.dto.QuoteDto
import com.example.tuvi.data.mapper.toDomain
import com.example.tuvi.domain.model.Quote
import com.example.tuvi.domain.repository.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class QuoteRepositoryImpl(
    private val context: Context,
    private val json: Json,
) : QuoteRepository {

    @Volatile
    private var cache: List<Quote>? = null

    override suspend fun getAll(): Result<List<Quote>> = withContext(Dispatchers.IO) {
        try {
            val quotes = cache ?: run {
                context.assets.open(ASSET_FILE).bufferedReader().use { reader ->
                    json.decodeFromString<List<QuoteDto>>(reader.readText())
                        .map { it.toDomain() }
                }.also { cache = it }
            }
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val ASSET_FILE = "quotes.json"
    }
}
