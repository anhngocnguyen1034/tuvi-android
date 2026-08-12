package com.anhnn.tuvi.domain.usecase

import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViVanHanResult // 👉 Đổi import sang model mới
import com.anhnn.tuvi.domain.repository.TuViRepository

/** Luận giải vận hạn năm `input.namXem` qua POST /api/interpret/van-han. */
class GetTuViVanHanUseCase(private val repository: TuViRepository) {
    // 👉 Đổi kiểu trả về của invoke thành Result<TuViVanHanResult>
    suspend operator fun invoke(input: TuViChartInput): Result<TuViVanHanResult> =
        runCatching { repository.getTuViVanHan(input) }
}