package com.example.tuvi.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TuViDatabase_Impl : TuViDatabase() {
  private val _savedChartDao: Lazy<SavedChartDao> = lazy {
    SavedChartDao_Impl(this)
  }

  private val _historyDao: Lazy<HistoryDao> = lazy {
    HistoryDao_Impl(this)
  }

  private val _bookmarkDao: Lazy<BookmarkDao> = lazy {
    BookmarkDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3,
        "9b1ce097dfd0fda9e1af456d9f303dd1", "c6a9849b8b5e2f32ef92b6354fe14708") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `saved_charts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ten` TEXT NOT NULL, `ngay_sinh` TEXT NOT NULL, `gioi_tinh` TEXT NOT NULL, `nhom` TEXT NOT NULL, `ngay_luu` INTEGER NOT NULL, `input_json` TEXT NOT NULL, `chart_json` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `browser_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `title` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `title` TEXT NOT NULL, `favicon_path` TEXT NOT NULL, `created_time` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9b1ce097dfd0fda9e1af456d9f303dd1')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `saved_charts`")
        connection.execSQL("DROP TABLE IF EXISTS `browser_history`")
        connection.execSQL("DROP TABLE IF EXISTS `bookmarks`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsSavedCharts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavedCharts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("ten", TableInfo.Column("ten", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("ngay_sinh", TableInfo.Column("ngay_sinh", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("gioi_tinh", TableInfo.Column("gioi_tinh", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("nhom", TableInfo.Column("nhom", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("ngay_luu", TableInfo.Column("ngay_luu", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("input_json", TableInfo.Column("input_json", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedCharts.put("chart_json", TableInfo.Column("chart_json", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavedCharts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavedCharts: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSavedCharts: TableInfo = TableInfo("saved_charts", _columnsSavedCharts,
            _foreignKeysSavedCharts, _indicesSavedCharts)
        val _existingSavedCharts: TableInfo = read(connection, "saved_charts")
        if (!_infoSavedCharts.equals(_existingSavedCharts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |saved_charts(com.example.tuvi.data.local.SavedChartEntity).
              | Expected:
              |""".trimMargin() + _infoSavedCharts + """
              |
              | Found:
              |""".trimMargin() + _existingSavedCharts)
        }
        val _columnsBrowserHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBrowserHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBrowserHistory.put("url", TableInfo.Column("url", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBrowserHistory.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBrowserHistory.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBrowserHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBrowserHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBrowserHistory: TableInfo = TableInfo("browser_history", _columnsBrowserHistory,
            _foreignKeysBrowserHistory, _indicesBrowserHistory)
        val _existingBrowserHistory: TableInfo = read(connection, "browser_history")
        if (!_infoBrowserHistory.equals(_existingBrowserHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |browser_history(com.example.tuvi.data.local.HistoryItemEntity).
              | Expected:
              |""".trimMargin() + _infoBrowserHistory + """
              |
              | Found:
              |""".trimMargin() + _existingBrowserHistory)
        }
        val _columnsBookmarks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBookmarks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmarks.put("url", TableInfo.Column("url", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmarks.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmarks.put("favicon_path", TableInfo.Column("favicon_path", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmarks.put("created_time", TableInfo.Column("created_time", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBookmarks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBookmarks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBookmarks: TableInfo = TableInfo("bookmarks", _columnsBookmarks,
            _foreignKeysBookmarks, _indicesBookmarks)
        val _existingBookmarks: TableInfo = read(connection, "bookmarks")
        if (!_infoBookmarks.equals(_existingBookmarks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |bookmarks(com.example.tuvi.data.local.BookmarkItemEntity).
              | Expected:
              |""".trimMargin() + _infoBookmarks + """
              |
              | Found:
              |""".trimMargin() + _existingBookmarks)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "saved_charts",
        "browser_history", "bookmarks")
  }

  public override fun clearAllTables() {
    super.performClear(false, "saved_charts", "browser_history", "bookmarks")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SavedChartDao::class, SavedChartDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HistoryDao::class, HistoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BookmarkDao::class, BookmarkDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun savedChartDao(): SavedChartDao = _savedChartDao.value

  public override fun historyDao(): HistoryDao = _historyDao.value

  public override fun bookmarkDao(): BookmarkDao = _bookmarkDao.value
}
