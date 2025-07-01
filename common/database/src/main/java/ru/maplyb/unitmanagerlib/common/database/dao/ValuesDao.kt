package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.maplyb.unitmanagerlib.common.database.entity.ValueEntity

@Dao
interface ValuesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValues(values: List<ValueEntity>)

    @Delete
    suspend fun deleteValue(value: ValueEntity)

    @Query("DELETE FROM ValueEntity WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM ValueEntity WHERE headersName = :name")
    suspend fun getAllByTableName(name: String): List<ValueEntity>

    @Query("SELECT * FROM ValueEntity WHERE headersName = :name")
    fun getAllByTableNameFlow(name: String): Flow<List<ValueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValue(value: ValueEntity)

    @Query("SELECT * FROM ValueEntity")
    suspend fun getAll(): List<ValueEntity>

    @Query("SELECT * FROM ValueEntity WHERE type = :type")
    suspend fun getAllByType(type: String): List<ValueEntity>

    @Query("SELECT * FROM ValueEntity WHERE type = :type AND headersName = :tableName")
    suspend fun getAllByTypeAndTableName(type: String, tableName: String): List<ValueEntity>

    @Query("SELECT DISTINCT type FROM ValueEntity")
    suspend fun getAllUniqueTypes(): List<String>
}
