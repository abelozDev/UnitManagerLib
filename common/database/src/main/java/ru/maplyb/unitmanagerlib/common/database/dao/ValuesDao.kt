package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.maplyb.unitmanagerlib.common.database.entity.HeadersWithValues
import ru.maplyb.unitmanagerlib.common.database.entity.ValueEntity

@Dao
interface ValuesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValues(values: List<ValueEntity>)
}
