package com.example.tuvi.data.remote

import com.example.tuvi.data.remote.dto.NgayInfoDto
import com.example.tuvi.data.remote.dto.ThangLichDto
import com.example.tuvi.data.remote.dto.HoiResponse
import com.example.tuvi.data.remote.dto.InterpretResponse
import com.example.tuvi.data.remote.dto.IapProductsDto
import com.example.tuvi.data.remote.dto.IapVerifyRequestDto
import com.example.tuvi.data.remote.dto.IapVerifyResponseDto
import com.example.tuvi.data.remote.dto.QuotaStatusDto
import com.example.tuvi.data.remote.dto.TuViRequest
import com.example.tuvi.data.remote.dto.TuViResponse
import com.example.tuvi.data.remote.dto.VanHanResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TuViApiService {
    @POST("/api/tuvi")
    suspend fun getTuVi(@Body request: TuViRequest): TuViResponse

    @POST("/api/interpret")
    suspend fun interpret(@Body request: TuViRequest): InterpretResponse

    /** Luận giải vận hạn 1 năm (đại hạn + tiểu hạn + sao lưu của `nam_xem`). */
    @POST("/api/interpret/van-han")
    suspend fun interpretVanHan(@Body request: TuViRequest): VanHanResponse

    /** Hỏi – đáp tự do: gửi `cau_hoi` của người dùng kèm lá số. */
    @POST("/api/interpret/hoi")
    suspend fun interpretHoi(@Body request: TuViRequest): HoiResponse

    /** Số lượt AI còn lại của thiết bị (cần header X-Device-Id). */
    @GET("/api/quota")
    suspend fun getQuota(): QuotaStatusDto

    /** Danh mục gói nạp lượt (product_id + credits). */
    @GET("/api/iap/products")
    suspend fun getIapProducts(): IapProductsDto

    /** Xác minh giao dịch Google Play → backend cấp credit (cần X-Device-Id). */
    @POST("/api/iap/verify")
    suspend fun verifyIap(@Body request: IapVerifyRequestDto): IapVerifyResponseDto

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
