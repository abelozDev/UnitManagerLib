package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.maplyb.unitmanagerlib.common.database.entity.ValuesTypeEntity

@Dao
interface ValuesTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<ValuesTypeEntity>)

    @Query("SELECT * FROM ValuesTypeEntity WHERE tableName = :tableName ORDER BY orderIndex")
    suspend fun getAllByTableName(tableName: String): List<ValuesTypeEntity>

    @Query("SELECT * FROM ValuesTypeEntity WHERE tableName = :tableName ORDER BY orderIndex")
    fun getAllByTableNameFlow(tableName: String): Flow<List<ValuesTypeEntity>>

    @Query("DELETE FROM ValuesTypeEntity WHERE tableName = :tableName")
    suspend fun deleteAllByTableName(tableName: String)

    //@Query("SELECT DISTINCT type FROM ValueEntity")
    @Query("SELECT DISTINCT name FROM ValuesTypeEntity WHERE tableName = :tableName ORDER BY orderIndex")
    suspend fun getAllTypeNamesByTableName(tableName: String): List<String>
}