package com.example.tuvi.domain.repository

import com.example.tuvi.domain.model.SavedChart
import kotlinx.coroutines.flow.Flow

interface SavedChartRepository {
    fun getAllCharts(): Flow<List<SavedChart>>
    fun searchCharts(query: String): Flow<List<SavedChart>>
    fun getChartsByGroup(nhom: String): Flow<List<SavedChart>>
    fun getAllGroups(): Flow<List<String>>
    suspend fun saveChart(chart: SavedChart): Long
    suspend fun deleteChart(id: Long)
    suspend fun getChartById(id: Long): SavedChart?
}
