package com.example.tuvi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tab_sessions")
data class TabSessionEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val sortOrder: Int,
    val isActive: Boolean = false,
    /** Danh sách URL lịch sử navigation của tab, mỗi URL cách nhau bằng '\n'. */
    val navHistoryJson: String = "",
    /** Index hiện tại trong navHistory. -1 = chưa load. */
    val navHistoryIndex: Int = -1
)
