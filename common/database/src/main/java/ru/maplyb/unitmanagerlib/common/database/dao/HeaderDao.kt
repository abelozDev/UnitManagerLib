package ru.maplyb.unitmanagerlib.common.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.maplyb.unitmanagerlib.common.database.entity.HeaderEntity
import ru.maplyb.unitmanagerlib.common.database.entity.HeadersWithValues

@Dao
interface HeaderDao {
    @Query("SELECT * FROM HeaderEntity")
    fun getAllFLow(): Flow<List<HeaderEntity>>

    @Query("SELECT * FROM HeaderEntity")
    suspend fun getAll(): List<HeaderEntity>

    @Query("SELECT * FROM HeaderEntity WHERE name = :name LIMIT 1")
    suspend fun getByTableName(name: String): HeaderEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(headers: HeaderEntity)

    @Query("DELETE FROM HeaderEntity WHERE name = :name")
    suspend fun deleteHeaders(name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(headers: List<HeaderEntity>)

    @Transaction
    @Query("SELECT * FROM HeaderEntity WHERE name = :name")
    suspend fun getHeaderWithValues(name: String): HeadersWithValues?

    @Query("SELECT * FROM HeaderEntity WHERE name = :name")
    fun getHeaderWithValuesFlow(name: String): Flow<HeadersWithValues?>

    companion object {
        val defaultUnitManagerTableHeaders:Map<String, List<String>> = mapOf(
            "№п/п" to emptyList(),
            "№" to emptyList(),
            "Позывной" to emptyList(),
            "№ жетона" to emptyList(),
            "Должность" to emptyList(),
            "Группа" to emptyList(),
            "Вооружение" to listOf("тип","№","тип","№"),
            "Средства связи" to listOf("рст","телефон"),
            "Группа крови" to emptyList(),
            "Позиция" to listOf("X", "Y", "Название"),
        )
        val headersSize = defaultUnitManagerTableHeaders.flatMap {
            if(it.value.isNotEmpty()) it.value
            else listOf(it.key)
        }.size
        val defaultUnitManagerValueTypes = listOf(
            "Управление взводом",
            "1 группа штурмовиков",
            "Артиллерия",
            "Обеспечение"
        )

        fun findPositionInDefaultHeaders(value: String): List<Int> {
            val positions = mutableListOf<Int>()
            var currentPosition = -1
            defaultUnitManagerTableHeaders.forEach { (key, values) ->
                if (values.isEmpty()) currentPosition++
                if (key == value) {
                    positions.add(currentPosition)
                }
                values.forEach { s ->
                    currentPosition++
                    if (s == value) {
                        positions.add(currentPosition)
                    }
                }
            }
            return positions
        }
    }
}
