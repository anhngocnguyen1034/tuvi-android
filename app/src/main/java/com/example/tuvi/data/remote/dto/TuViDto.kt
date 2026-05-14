package com.example.tuvi.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class TuViRequest(
    val ngay: Int,
    val thang: Int,
    val nam: Int,
    val nam_xem: Int,
    val gioi_tinh: Int,
    val gio: Int? = null,
    val phut: Int? = null,
    val gio_sinh: Int? = null,
    val ten: String = "",
    val duong_lich: Boolean = true,
    val time_zone: Int = 7
)

/** Cho phép giá trị Int đến từ số (1) hoặc chuỗi ("1") trong JSON. */
object IntOrStringSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntOrString", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int? {
        if (decoder is JsonDecoder) {
            val el = decoder.decodeJsonElement()
            return (el as? JsonPrimitive)?.content?.toIntOrNull()
        }
        return decoder.decodeNullableSerializableValue(serializer<Int>().nullable)
    }

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value != null) encoder.encodeInt(value)
    }
}

@Serializable
data class TuViResponse(
    val thien_ban: ThienBanDto,
    @SerialName("dia_ban") val dia_ban: List<CungDto> = emptyList()
)

/** Response from POST /api/interpret (chart + optional Gemini text). */
@Serializable
data class InterpretResponse(
    val status: String? = null,
    @SerialName("data_la_so") val data_la_so: TuViResponse? = null,
    @SerialName("ai_reading") val ai_reading: String? = null,
)

@Serializable
data class ThienBanDto(
    val ten: String,
    val gioi_tinh: String,
    val ngay_duong: String,
    val ngay_am: String,
    @SerialName("ngay_am_lich_ten") val ngayAmLichTen: String? = null,
    @SerialName("thang_nhuan") val thangNhuan: Boolean? = null,
    @SerialName("gio_sinh") val gioSinh: String? = null,
    @SerialName("chi_gio_sinh") val chiGioSinh: String? = null,
    @SerialName("can_nam") val canNam: String? = null,
    @SerialName("chi_nam") val chiNam: String? = null,
    @SerialName("can_thang") val canThang: String? = null,
    @SerialName("chi_thang") val chiThang: String? = null,
    @SerialName("can_ngay") val canNgay: String? = null,
    @SerialName("chi_ngay") val chiNgay: String? = null,
    @SerialName("am_duong_nam_sinh") val amDuongNamSinh: String? = null,
    @SerialName("am_duong_menh") val amDuongMenh: String? = null,
    val menh: String? = null,
    @SerialName("ban_menh") val banMenh: String? = null,
    val cuc: String? = null,
    @Serializable(with = IntOrStringSerializer::class) @SerialName("hanh_cuc") val hanhCuc: Int? = null,
    @SerialName("menh_chu") val menhChu: String? = null,
    @SerialName("than_chu") val thanChu: String? = null,
    @SerialName("sinh_khac") val sinhKhac: String? = null,
    @SerialName("nam_xem") val namXem: Int? = null,
    @SerialName("tuoi_am") val tuoiAm: Int? = null
)

@Serializable
data class CungDto(
    @SerialName("cung_ten") val cungTen: String,
    @SerialName("cung_chu") val cungChu: String,
    @SerialName("hanh_cung") val hanhCung: String,
    @SerialName("thien_can") val thienCan: String? = null,
    @SerialName("dai_han") val daiHan: Int? = null,
    val thang: Int? = null,
    val sao: List<SaoDto>,
    val tuan: Boolean = false,
    val triet: Boolean = false
)

@Serializable
data class SaoDto(
    val ten: String,
    val dac_tinh: String? = "",
    val ngu_hanh: String? = null,
    @SerialName("vong_trang_sinh") val vongTrangSinh: Int? = 0,
    @SerialName("is_luu") val isLuu: Boolean = false
)
