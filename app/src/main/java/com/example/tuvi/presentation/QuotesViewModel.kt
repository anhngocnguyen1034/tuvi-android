package com.example.tuvi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.Quote
import com.example.tuvi.domain.usecase.GetQuotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface QuotesUiState {
    data object Loading : QuotesUiState
    data class Success(
        val dailyQuote: Quote,
        val displayed: List<Quote>,
        val categories: List<String>,
        val searchQuery: String,
        val selectedCategory: String?,
        val totalCount: Int,
    ) : QuotesUiState

    data class Error(val message: String) : QuotesUiState
}

class QuotesViewModel(
    private val getQuotes: GetQuotesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuotesUiState>(QuotesUiState.Loading)
    val uiState: StateFlow<QuotesUiState> = _uiState.asStateFlow()

    private var allQuotes: List<Quote> = emptyList()
    private var searchQuery = ""
    private var selectedCategory: String? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = QuotesUiState.Loading
            getQuotes().fold(
                onSuccess = { quotes ->
                    allQuotes = quotes
                    publish()
                },
                onFailure = { e ->
                    _uiState.value = QuotesUiState.Error(
                        e.message ?: "Không thể tải châm ngôn"
                    )
                },
            )
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        publish()
    }

    fun setSelectedCategory(category: String?) {
        selectedCategory = category
        publish()
    }

    fun randomQuote(): Quote? {
        if (allQuotes.isEmpty()) return null
        return allQuotes.random()
    }

    private fun publish() {
        if (allQuotes.isEmpty()) return
        val daily = dailyQuote(allQuotes)
        val filtered = filterQuotes(allQuotes, searchQuery, selectedCategory)
        _uiState.value = QuotesUiState.Success(
            dailyQuote = daily,
            displayed = filtered,
            categories = topCategories(allQuotes),
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            totalCount = allQuotes.size,
        )
    }

    companion object {
        private const val TOP_CATEGORY_COUNT = 18

        fun dailyQuote(quotes: List<Quote>): Quote {
            val cal = Calendar.getInstance()
            val index = (cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.YEAR) * 367) % quotes.size
            return quotes[index]
        }

        fun filterQuotes(
            quotes: List<Quote>,
            query: String,
            category: String?,
        ): List<Quote> {
            val q = query.trim()
            return quotes.filter { quote ->
                val matchesCategory = category.isNullOrBlank() ||
                    quote.tuKhoa.any { it.equals(category, ignoreCase = true) }
                val matchesQuery = q.isBlank() ||
                    quote.noiDung.contains(q, ignoreCase = true) ||
                    quote.tacGia?.contains(q, ignoreCase = true) == true ||
                    quote.tiengAnh?.contains(q, ignoreCase = true) == true ||
                    quote.tuKhoa.any { it.contains(q, ignoreCase = true) }
                matchesCategory && matchesQuery
            }
        }

        fun topCategories(quotes: List<Quote>): List<String> {
            val counts = linkedMapOf<String, Int>()
            quotes.forEach { quote ->
                quote.tuKhoa.forEach { kw ->
                    counts[kw] = (counts[kw] ?: 0) + 1
                }
            }
            return counts.entries
                .sortedByDescending { it.value }
                .take(TOP_CATEGORY_COUNT)
                .map { it.key }
        }

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuotesViewModel(AppContainer.getQuotesUseCase) as T
        }
    }
}
