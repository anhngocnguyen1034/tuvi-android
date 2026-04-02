package com.example.tuvi.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BookmarkDao_Impl(
  __db: RoomDatabase,
) : BookmarkDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBookmarkItemEntity: EntityInsertAdapter<BookmarkItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfBookmarkItemEntity = object : EntityInsertAdapter<BookmarkItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `bookmarks` (`id`,`url`,`title`,`favicon_path`,`created_time`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BookmarkItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.url)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.faviconPath)
        statement.bindLong(5, entity.createdTime)
      }
    }
  }

  public override suspend fun insertBookmark(item: BookmarkItemEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfBookmarkItemEntity.insertAndReturnId(_connection, item)
    _result
  }

  public override fun getAllBookmarks(): Flow<List<BookmarkItemEntity>> {
    val _sql: String = "SELECT * FROM bookmarks ORDER BY created_time DESC"
    return createFlow(__db, false, arrayOf("bookmarks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUrl: Int = getColumnIndexOrThrow(_stmt, "url")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFaviconPath: Int = getColumnIndexOrThrow(_stmt, "favicon_path")
        val _columnIndexOfCreatedTime: Int = getColumnIndexOrThrow(_stmt, "created_time")
        val _result: MutableList<BookmarkItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BookmarkItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUrl: String
          _tmpUrl = _stmt.getText(_columnIndexOfUrl)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFaviconPath: String
          _tmpFaviconPath = _stmt.getText(_columnIndexOfFaviconPath)
          val _tmpCreatedTime: Long
          _tmpCreatedTime = _stmt.getLong(_columnIndexOfCreatedTime)
          _item = BookmarkItemEntity(_tmpId,_tmpUrl,_tmpTitle,_tmpFaviconPath,_tmpCreatedTime)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun isBookmarked(url: String): Flow<Boolean> {
    val _sql: String = "SELECT COUNT(*) > 0 FROM bookmarks WHERE url = ?"
    return createFlow(__db, false, arrayOf("bookmarks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, url)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBookmarkByUrl(url: String) {
    val _sql: String = "DELETE FROM bookmarks WHERE url = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, url)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBookmarkById(id: Long) {
    val _sql: String = "DELETE FROM bookmarks WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateTitle(id: Long, title: String) {
    val _sql: String = "UPDATE bookmarks SET title = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, title)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
