package com.anhnn.tuvi.domain.model

import com.anhnn.tuvi.data.remote.dto.ThangHanData
import com.anhnn.tuvi.data.remote.dto.VongHanSummary

class TuViVanHanResult (
    val namXem: Int ,
    val chart: TuViChart,
    val aiReading: String,
    val timeline: List<ThangHanData>,
    val summary: VongHanSummary?,
)