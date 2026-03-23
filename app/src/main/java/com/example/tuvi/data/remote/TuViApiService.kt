package com.example.tuvi.data.remote

import com.example.tuvi.data.remote.dto.TuViRequest
import com.example.tuvi.data.remote.dto.TuViResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TuViApiService {
    @POST("/api/tuvi")
    suspend fun getTuVi(@Body request: TuViRequest): TuViResponse
}
