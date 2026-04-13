package com.example.tuvi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.SavedChart
import com.example.tuvi.domain.usecase.DeleteSavedChartUseCase
import com.example.tuvi.domain.usecase.GetAllGroupsUseCase
import com.example.tuvi.domain.usecase.GetAllSavedChartsUseCase
import com.example.tuvi.domain.usecase.GetChartsByGroupUseCase
import com.example.tuvi.domain.usecase.SearchSavedChartsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SavedChartsViewModel(
    private val getAllCharts: GetAllSavedChartsUseCase,
    private val searchCharts: SearchSavedChartsUseCase,
    private val getByGroup: GetChartsByGroupUseCase,
    private val getAllGroups: GetAllGroupsUseCase,
    private val deleteChart: DeleteSavedChartUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedGroup = MutableStateFlow("Tất cả")
    val selectedGroup: StateFlow<String> = _selectedGroup

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedGroup(group: String) { _selectedGroup.value = group }

    val groups: StateFlow<List<String>> = getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val charts: StateFlow<List<SavedChart>> = combine(_searchQuery, _selectedGroup) { q, g -> q to g }
        .flatMapLatest { (q, g) ->
            when {
                q.isNotBlank() -> searchCharts(q)
                g != "Tất cả" -> getByGroup(g)
                else -> getAllCharts()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { deleteChart(id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SavedChartsViewModel(
                    AppContainer.getAllSavedChartsUseCase,
                    AppContainer.searchSavedChartsUseCase,
                    AppContainer.getChartsByGroupUseCase,
                    AppContainer.getAllGroupsUseCase,
                    AppContainer.deleteSavedChartUseCase
                ) as T
        }
    }
}
