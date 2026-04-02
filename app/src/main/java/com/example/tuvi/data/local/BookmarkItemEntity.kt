package com.example.tuvi.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "url")          val url: String,
    @ColumnInfo(name = "title")        val title: String,
    @ColumnInfo(name = "favicon_path") val faviconPath: String = "",
    @ColumnInfo(name = "created_time") val createdTime: Long
)
