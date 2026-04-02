package com.example.tuvi.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class SavedChartDao_Impl(
  __db: RoomDatabase,
) : SavedChartDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSavedChartEntity: EntityInsertAdapter<SavedChartEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSavedChartEntity = object : EntityInsertAdapter<SavedChartEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `saved_charts` (`id`,`ten`,`ngay_sinh`,`gioi_tinh`,`nhom`,`ngay_luu`,`input_json`,`chart_json`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SavedChartEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.ten)
        statement.bindText(3, entity.ngaySinh)
        statement.bindText(4, entity.gioiTinh)
        statement.bindText(5, entity.nhom)
        statement.bindLong(6, entity.ngayLuu)
        statement.bindText(7, entity.inputJson)
        statement.bindText(8, entity.chartJson)
      }
    }
  }

  public override suspend fun insert(entity: SavedChartEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfSavedChartEntity.insertAndReturnId(_connection, entity)
    _result
  }

  public override fun getAll(): Flow<List<SavedChartEntity>> {
    val _sql: String = "SELECT * FROM saved_charts ORDER BY ngay_luu DESC"
    return createFlow(__db, false, arrayOf("saved_charts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTen: Int = getColumnIndexOrThrow(_stmt, "ten")
        val _columnIndexOfNgaySinh: Int = getColumnIndexOrThrow(_stmt, "ngay_sinh")
        val _columnIndexOfGioiTinh: Int = getColumnIndexOrThrow(_stmt, "gioi_tinh")
        val _columnIndexOfNhom: Int = getColumnIndexOrThrow(_stmt, "nhom")
        val _columnIndexOfNgayLuu: Int = getColumnIndexOrThrow(_stmt, "ngay_luu")
        val _columnIndexOfInputJson: Int = getColumnIndexOrThrow(_stmt, "input_json")
        val _columnIndexOfChartJson: Int = getColumnIndexOrThrow(_stmt, "chart_json")
        val _result: MutableList<SavedChartEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedChartEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTen: String
          _tmpTen = _stmt.getText(_columnIndexOfTen)
          val _tmpNgaySinh: String
          _tmpNgaySinh = _stmt.getText(_columnIndexOfNgaySinh)
          val _tmpGioiTinh: String
          _tmpGioiTinh = _stmt.getText(_columnIndexOfGioiTinh)
          val _tmpNhom: String
          _tmpNhom = _stmt.getText(_columnIndexOfNhom)
          val _tmpNgayLuu: Long
          _tmpNgayLuu = _stmt.getLong(_columnIndexOfNgayLuu)
          val _tmpInputJson: String
          _tmpInputJson = _stmt.getText(_columnIndexOfInputJson)
          val _tmpChartJson: String
          _tmpChartJson = _stmt.getText(_columnIndexOfChartJson)
          _item =
              SavedChartEntity(_tmpId,_tmpTen,_tmpNgaySinh,_tmpGioiTinh,_tmpNhom,_tmpNgayLuu,_tmpInputJson,_tmpChartJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun search(query: String): Flow<List<SavedChartEntity>> {
    val _sql: String =
        "SELECT * FROM saved_charts WHERE ten LIKE '%' || ? || '%' ORDER BY ngay_luu DESC"
    return createFlow(__db, false, arrayOf("saved_charts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTen: Int = getColumnIndexOrThrow(_stmt, "ten")
        val _columnIndexOfNgaySinh: Int = getColumnIndexOrThrow(_stmt, "ngay_sinh")
        val _columnIndexOfGioiTinh: Int = getColumnIndexOrThrow(_stmt, "gioi_tinh")
        val _columnIndexOfNhom: Int = getColumnIndexOrThrow(_stmt, "nhom")
        val _columnIndexOfNgayLuu: Int = getColumnIndexOrThrow(_stmt, "ngay_luu")
        val _columnIndexOfInputJson: Int = getColumnIndexOrThrow(_stmt, "input_json")
        val _columnIndexOfChartJson: Int = getColumnIndexOrThrow(_stmt, "chart_json")
        val _result: MutableList<SavedChartEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedChartEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTen: String
          _tmpTen = _stmt.getText(_columnIndexOfTen)
          val _tmpNgaySinh: String
          _tmpNgaySinh = _stmt.getText(_columnIndexOfNgaySinh)
          val _tmpGioiTinh: String
          _tmpGioiTinh = _stmt.getText(_columnIndexOfGioiTinh)
          val _tmpNhom: String
          _tmpNhom = _stmt.getText(_columnIndexOfNhom)
          val _tmpNgayLuu: Long
          _tmpNgayLuu = _stmt.getLong(_columnIndexOfNgayLuu)
          val _tmpInputJson: String
          _tmpInputJson = _stmt.getText(_columnIndexOfInputJson)
          val _tmpChartJson: String
          _tmpChartJson = _stmt.getText(_columnIndexOfChartJson)
          _item =
              SavedChartEntity(_tmpId,_tmpTen,_tmpNgaySinh,_tmpGioiTinh,_tmpNhom,_tmpNgayLuu,_tmpInputJson,_tmpChartJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByGroup(nhom: String): Flow<List<SavedChartEntity>> {
    val _sql: String = "SELECT * FROM saved_charts WHERE nhom = ? ORDER BY ngay_luu DESC"
    return createFlow(__db, false, arrayOf("saved_charts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, nhom)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTen: Int = getColumnIndexOrThrow(_stmt, "ten")
        val _columnIndexOfNgaySinh: Int = getColumnIndexOrThrow(_stmt, "ngay_sinh")
        val _columnIndexOfGioiTinh: Int = getColumnIndexOrThrow(_stmt, "gioi_tinh")
        val _columnIndexOfNhom: Int = getColumnIndexOrThrow(_stmt, "nhom")
        val _columnIndexOfNgayLuu: Int = getColumnIndexOrThrow(_stmt, "ngay_luu")
        val _columnIndexOfInputJson: Int = getColumnIndexOrThrow(_stmt, "input_json")
        val _columnIndexOfChartJson: Int = getColumnIndexOrThrow(_stmt, "chart_json")
        val _result: MutableList<SavedChartEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedChartEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTen: String
          _tmpTen = _stmt.getText(_columnIndexOfTen)
          val _tmpNgaySinh: String
          _tmpNgaySinh = _stmt.getText(_columnIndexOfNgaySinh)
          val _tmpGioiTinh: String
          _tmpGioiTinh = _stmt.getText(_columnIndexOfGioiTinh)
          val _tmpNhom: String
          _tmpNhom = _stmt.getText(_columnIndexOfNhom)
          val _tmpNgayLuu: Long
          _tmpNgayLuu = _stmt.getLong(_columnIndexOfNgayLuu)
          val _tmpInputJson: String
          _tmpInputJson = _stmt.getText(_columnIndexOfInputJson)
          val _tmpChartJson: String
          _tmpChartJson = _stmt.getText(_columnIndexOfChartJson)
          _item =
              SavedChartEntity(_tmpId,_tmpTen,_tmpNgaySinh,_tmpGioiTinh,_tmpNhom,_tmpNgayLuu,_tmpInputJson,_tmpChartJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllGroups(): Flow<List<String>> {
    val _sql: String = "SELECT DISTINCT nhom FROM saved_charts ORDER BY nhom ASC"
    return createFlow(__db, false, arrayOf("saved_charts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): SavedChartEntity? {
    val _sql: String = "SELECT * FROM saved_charts WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTen: Int = getColumnIndexOrThrow(_stmt, "ten")
        val _columnIndexOfNgaySinh: Int = getColumnIndexOrThrow(_stmt, "ngay_sinh")
        val _columnIndexOfGioiTinh: Int = getColumnIndexOrThrow(_stmt, "gioi_tinh")
        val _columnIndexOfNhom: Int = getColumnIndexOrThrow(_stmt, "nhom")
        val _columnIndexOfNgayLuu: Int = getColumnIndexOrThrow(_stmt, "ngay_luu")
        val _columnIndexOfInputJson: Int = getColumnIndexOrThrow(_stmt, "input_json")
        val _columnIndexOfChartJson: Int = getColumnIndexOrThrow(_stmt, "chart_json")
        val _result: SavedChartEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTen: String
          _tmpTen = _stmt.getText(_columnIndexOfTen)
          val _tmpNgaySinh: String
          _tmpNgaySinh = _stmt.getText(_columnIndexOfNgaySinh)
          val _tmpGioiTinh: String
          _tmpGioiTinh = _stmt.getText(_columnIndexOfGioiTinh)
          val _tmpNhom: String
          _tmpNhom = _stmt.getText(_columnIndexOfNhom)
          val _tmpNgayLuu: Long
          _tmpNgayLuu = _stmt.getLong(_columnIndexOfNgayLuu)
          val _tmpInputJson: String
          _tmpInputJson = _stmt.getText(_columnIndexOfInputJson)
          val _tmpChartJson: String
          _tmpChartJson = _stmt.getText(_columnIndexOfChartJson)
          _result =
              SavedChartEntity(_tmpId,_tmpTen,_tmpNgaySinh,_tmpGioiTinh,_tmpNhom,_tmpNgayLuu,_tmpInputJson,_tmpChartJson)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM saved_charts WHERE id = ?"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
