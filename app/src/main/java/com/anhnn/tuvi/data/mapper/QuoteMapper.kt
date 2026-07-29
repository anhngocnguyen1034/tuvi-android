package com.anhnn.tuvi.data.mapper

import com.anhnn.tuvi.data.local.dto.QuoteDto
import com.anhnn.tuvi.domain.model.Quote

fun QuoteDto.toDomain() = Quote(
    id = id,
    noiDung = noiDung.trim(),
    tiengAnh = tiengAnh?.trim()?.takeIf { it.isNotEmpty() },
    tacGia = tacGia?.trim()?.takeIf { it.isNotEmpty() },
    tuKhoa = tuKhoa.map { it.trim() }.filter { it.isNotEmpty() },
)
