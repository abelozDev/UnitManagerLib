package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.maplyb.unitmanagerlib.common.database.entity.ValueEntity

@Dao
interface ValuesDao {
    @Query("SELECT * FROM ValueEntity WHERE 'key' = :key")
    suspend fun getByKey(key: String): List<ValueEntity>

    @Query("SELECT * FROM ValueEntity")
    suspend fun getAll(): List<ValueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<ValueEntity>)
}
