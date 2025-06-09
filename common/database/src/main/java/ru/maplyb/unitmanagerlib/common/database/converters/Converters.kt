package ru.maplyb.unitmanagerlib.common.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json

class Converters {

    private val gson = Gson()
    @TypeConverter
    fun fromStringToMap(value: String): Map<String, List<String>> {
        val mapType = object : TypeToken<Map<String, List<String>>>() {}.type
        return gson.fromJson(value, mapType)
    }
    @TypeConverter
    fun fromMapToString(value: Map<String, List<String>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun fromListToString(value: List<String>): String {
        return gson.toJson(value)
    }
    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        val mapType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, mapType)
    }
}