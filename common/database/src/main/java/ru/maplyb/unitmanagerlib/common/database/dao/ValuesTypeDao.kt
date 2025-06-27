package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.maplyb.unitmanagerlib.common.database.entity.ValuesTypeEntity

@Dao
interface ValuesTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<ValuesTypeEntity>)

    @Query("SELECT * FROM ValuesTypeEntity WHERE tableName = :tableName")
    suspend fun getAllByTableName(tableName: String): List<ValuesTypeEntity>

    @Query("DELETE FROM ValuesTypeEntity WHERE tableName = :tableName")
    suspend fun deleteAllByTableName(tableName: String)

    //@Query("SELECT DISTINCT type FROM ValueEntity")
    @Query("SELECT DISTINCT name FROM ValuesTypeEntity WHERE tableName = :tableName")
    suspend fun getAllTypeNamesByTableName(tableName: String): List<String>
}