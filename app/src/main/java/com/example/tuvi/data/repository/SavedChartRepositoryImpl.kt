package com.example.tuvi.data.repository

import com.example.tuvi.data.local.SavedChartDao
import com.example.tuvi.data.mapper.toDomain
import com.example.tuvi.data.mapper.toEntity
import com.example.tuvi.domain.model.SavedChart
import com.example.tuvi.domain.repository.SavedChartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedChartRepositoryImpl(private val dao: SavedChartDao) : SavedChartRepository {

    override fun getAllCharts(): Flow<List<SavedChart>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun searchCharts(query: String): Flow<List<SavedChart>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override fun getChartsByGroup(nhom: String): Flow<List<SavedChart>> =
        dao.getByGroup(nhom).map { list -> list.map { it.toDomain() } }

    override fun getAllGroups(): Flow<List<String>> = dao.getAllGroups()

    override suspend fun saveChart(chart: SavedChart): Long = dao.insert(chart.toEntity())

    override suspend fun deleteChart(id: Long) = dao.deleteById(id)

    override suspend fun getChartById(id: Long): SavedChart? = dao.getById(id)?.toDomain()
}
