package com.example.tuvi.domain.usecase

import com.example.tuvi.domain.model.SavedChart
import com.example.tuvi.domain.repository.SavedChartRepository
import kotlinx.coroutines.flow.Flow

class GetAllSavedChartsUseCase(private val repo: SavedChartRepository) {
    operator fun invoke(): Flow<List<SavedChart>> = repo.getAllCharts()
}

class SearchSavedChartsUseCase(private val repo: SavedChartRepository) {
    operator fun invoke(query: String): Flow<List<SavedChart>> =
        if (query.isBlank()) repo.getAllCharts() else repo.searchCharts(query)
}

class GetChartsByGroupUseCase(private val repo: SavedChartRepository) {
    operator fun invoke(nhom: String): Flow<List<SavedChart>> =
        if (nhom == "Tất cả") repo.getAllCharts() else repo.getChartsByGroup(nhom)
}

class GetAllGroupsUseCase(private val repo: SavedChartRepository) {
    operator fun invoke(): Flow<List<String>> = repo.getAllGroups()
}

class SaveChartUseCase(private val repo: SavedChartRepository) {
    suspend operator fun invoke(chart: SavedChart): Result<Long> = runCatching {
        repo.saveChart(chart)
    }
}

class DeleteSavedChartUseCase(private val repo: SavedChartRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteChart(id)
}

class GetSavedChartByIdUseCase(private val repo: SavedChartRepository) {
    suspend operator fun invoke(id: Long): SavedChart? = repo.getChartById(id)
}
