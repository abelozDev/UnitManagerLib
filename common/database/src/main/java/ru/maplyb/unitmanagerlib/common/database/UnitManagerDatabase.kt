package ru.maplyb.unitmanagerlib.common.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.maplyb.unitmanagerlib.common.database.converters.Converters
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao
import ru.maplyb.unitmanagerlib.common.database.dao.ValuesDao
import ru.maplyb.unitmanagerlib.common.database.dao.ValuesTypeDao
import ru.maplyb.unitmanagerlib.common.database.entity.HeaderEntity
import ru.maplyb.unitmanagerlib.common.database.entity.ValueEntity
import ru.maplyb.unitmanagerlib.common.database.entity.ValuesTypeEntity

@Database(
    entities = [
        HeaderEntity::class,
        ValueEntity::class,
        ValuesTypeEntity::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class UnitManagerDatabase : RoomDatabase() {
    abstract fun headerDao(): HeaderDao
    abstract fun valueDao(): ValuesDao
    abstract fun valuesTypeDao(): ValuesTypeDao
    suspend fun databaseIsNotEmpty(): Boolean {
        return headerDao().getAll().isNotEmpty()
    }

}