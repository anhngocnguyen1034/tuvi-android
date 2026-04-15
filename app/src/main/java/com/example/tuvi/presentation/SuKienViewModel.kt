package com.example.tuvi.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.data.local.SuKienDao
import com.example.tuvi.data.local.SuKienEntity
import com.example.tuvi.di.AppContainer
import com.example.tuvi.notification.AlarmHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SuKienViewModel(
    application: Application,
    private val dao: SuKienDao,
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>().applicationContext

    /** Sự kiện của tháng đang hiển thị trong lịch. */
    private val _thang = MutableStateFlow(0)
    private val _nam   = MutableStateFlow(0)

    val suKienThang: StateFlow<List<SuKienEntity>> =
        combine(_thang, _nam) { thang, nam -> thang to nam }
            .flatMapLatest { (thang, nam) ->
                if (thang == 0) flowOf(emptyList())
                else dao.getThang(thang, nam)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThang(thang: Int, nam: Int) {
        _thang.value = thang
        _nam.value   = nam
    }

    /** Danh sách sự kiện của một ngày cụ thể (flow). */
    fun getSuKienNgay(ngay: Int, thang: Int, nam: Int): Flow<List<SuKienEntity>> =
        dao.getBuoi(ngay, thang, nam)

    /** Thêm mới sự kiện và đặt alarm nếu có. */
    fun them(
        tieuDe: String,
        ghiChu: String,
        ngay: Int, thang: Int, nam: Int,
        alarmEpoch: Long,
    ) = viewModelScope.launch {
        val id = dao.insert(
            SuKienEntity(
                tieuDe     = tieuDe.trim(),
                ghiChu     = ghiChu.trim(),
                ngayDuong  = ngay,
                thangDuong = thang,
                namDuong   = nam,
                alarmEpoch = alarmEpoch,
            )
        )
        if (alarmEpoch > 0) {
            AlarmHelper.schedule(ctx, SuKienEntity(id, tieuDe.trim(), ghiChu.trim(), ngay, thang, nam, alarmEpoch))
        }
    }

    /** Xóa sự kiện và hủy alarm tương ứng. */
    fun xoa(entity: SuKienEntity) = viewModelScope.launch {
        AlarmHelper.cancel(ctx, entity)
        dao.delete(entity)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val app = AppContainer.app
                return SuKienViewModel(app, AppContainer.suKienDao) as T
            }
        }
    }
}
