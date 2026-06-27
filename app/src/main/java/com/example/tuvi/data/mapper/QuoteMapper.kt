package com.example.tuvi.data.mapper

import com.example.tuvi.data.local.dto.QuoteDto
import com.example.tuvi.domain.model.Quote

fun QuoteDto.toDomain() = Quote(
    id = id,
    noiDung = noiDung.trim(),
    tiengAnh = tiengAnh?.trim()?.takeIf { it.isNotEmpty() },
    tacGia = tacGia?.trim()?.takeIf { it.isNotEmpty() },
    tuKhoa = tuKhoa.map { it.trim() }.filter { it.isNotEmpty() },
)
