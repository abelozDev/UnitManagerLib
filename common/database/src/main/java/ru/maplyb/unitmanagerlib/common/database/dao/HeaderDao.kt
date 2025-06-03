package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.maplyb.unitmanagerlib.common.database.entity.HeaderEntity

@Dao
interface HeaderDao {
    @Query("SELECT * FROM HeaderEntity")
    suspend fun getAll(): List<HeaderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(headers: List<HeaderEntity>)
}
