package com.example.tuvi.data.remote

import com.example.tuvi.data.remote.dto.NgayInfoDto
import com.example.tuvi.data.remote.dto.ThangLichDto
import com.example.tuvi.data.remote.dto.InterpretResponse
import com.example.tuvi.data.remote.dto.LoginRequest
import com.example.tuvi.data.remote.dto.LoginResponse
import com.example.tuvi.data.remote.dto.TuViRequest
import com.example.tuvi.data.remote.dto.TuViResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TuViApiService {
    @POST("/api/tuvi")
    suspend fun getTuVi(@Body request: TuViRequest): TuViResponse

    @POST("/api/interpret")
    suspend fun interpret(@Body request: TuViRequest): InterpretResponse

    /** Backend verify idToken qua Firebase Admin, tạo/cập nhật user trong MongoDB. */
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /** Lấy toàn bộ lịch của một tháng dương lịch. */
    @GET("/api/lich/{nam}/{thang}")
    suspend fun getLichThang(
        @Path("nam") nam: Int,
        @Path("thang") thang: Int,
    ): ThangLichDto

    /** Lấy thông tin đầy đủ của một ngày dương lịch. */
    @GET("/api/lich/{nam}/{thang}/{ngay}")
    suspend fun getNgayInfo(
        @Path("nam") nam: Int,
        @Path("thang") thang: Int,
        @Path("ngay") ngay: Int,
    ): NgayInfoDto

    /** Thông tin lịch hôm nay. */
    @GET("/api/lich/hom-nay")
    suspend fun getHomNay(): NgayInfoDto
}
