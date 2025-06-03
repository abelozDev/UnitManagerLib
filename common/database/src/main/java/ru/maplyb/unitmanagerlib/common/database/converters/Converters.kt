package ru.maplyb.unitmanagerlib.common.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromListOfList(value: List<List<String>>): String = Json.encodeToString(value)

    @TypeConverter
    fun toListOfList(value: String): List<List<String>> = Json.decodeFromString(value)
}