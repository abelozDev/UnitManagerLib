package ru.maplyb.unitmanagerlib.common.database.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.maplyb.unitmanagerlib.common.database.UnitManagerDatabase
import ru.maplyb.unitmanagerlib.common.database.domain.DatabaseRepository
import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.common.database.entity.HeaderEntity
import ru.maplyb.unitmanagerlib.common.database.entity.ValueEntity

class DatabaseRepositoryImpl(
    private val database: UnitManagerDatabase
): DatabaseRepository {

    @Transaction
    override suspend fun insertHeadersAndValues(
        tableName: String,
        headers: Map<String, List<String>>,
        values: Map<String, List<List<String>>>
    ): FileParsingResultDTO {
        database.headerDao().insert(
            HeaderEntity(
                name = tableName,
                value = headers
            )
        )
        val valueEntities = buildList<ValueEntity> {
            values.keys.forEach { type ->
                values[type]?.forEach {
                    add(
                        ValueEntity(
                            headersName = tableName,
                            type = type,
                            values = it
                        )
                    )
                }
            }
        }
        database.valueDao().insertValues(valueEntities)
        return getTableInfo()!!
    }

    @Transaction
    override suspend fun addNewItem(type: String, tableName: String) {
        val headersByTableName = database.headerDao().getByTableName(tableName)
        val allTypes = database.valueDao().getAllUniqueTypes()
        val typesAfterCurrent = allTypes.subList(allTypes.indexOf(type)+1, allTypes.lastIndex)
        val allValues = database.valueDao().getAllByTableName(tableName).toMutableList()
        val valuesByType = allValues.filter { it.type == type }.map {
            it.values
        }
        val size = headersByTableName.value.map { it.value }.sumOf { it.size } + headersByTableName.value.size
        /*Новое значение*/
        val newItem = MutableList(size) { "" }
        newItem[0] = ((valuesByType.lastOrNull()?.get(0)?.toInt() ?: 0) + 1).toString()
        newItem[1] = ((valuesByType.lastOrNull()?.get(1)?.toInt() ?: 0) + 1).toString()
        allValues.add(
            ValueEntity(
                headersName = tableName,
                type = type,
                values = newItem
            )
        )
        /*Изменение порядкового номера остальных значений*/
        val updatedValues = allValues.map {
            if (it.type in typesAfterCurrent) {
                val newValue = it.values.toMutableList()
                newValue[0] = ((newValue[0].toIntOrNull() ?: -1) + 1).toString()
                it.copy(values = newValue)
            } else it
        }
        println("updated values: $updatedValues")
        database.valueDao().insertValues(updatedValues)
    }

    override suspend fun getTableInfoFlow(name: String): Flow<FileParsingResultDTO?> {
        return if (database.databaseIsNotEmpty()) {
            database.headerDao().getHeaderWithValuesFlow(name).map {
                val values = it?.values?.groupBy {
                    it.type
                }?.map {
                    it.key to it.value.map { it.values }
                }?.toMap()
                FileParsingResultDTO(
                    headers = it?.header?.value!!,
                    values = values!!,
                    tableName = name
                )
            }
        } else flow { emit(null) }
    }

    override suspend fun getTableInfo(): FileParsingResultDTO? {
        return if (database.databaseIsNotEmpty()) {
            /*Получаем хедер (пока там может быть только один)*/
            val headers = database.headerDao().getAll().first()
            val headersWithValues = database.headerDao().getHeaderWithValues(headers.name)
            val valuesCategories = headersWithValues?.values?.map { it.type }?.toSet()
            println("valuesCategories = $valuesCategories")
            val values = buildMap<String, List<List<String>>> {
                valuesCategories?.forEach { category ->
                    put(
                        category,
                        headersWithValues.values.filter { it.type == category }
                            .map { it.values })
                }
            }
            FileParsingResultDTO(
                headers = headersWithValues?.header?.value!!,
                values = values,
                tableName = headersWithValues.header.name
            )
        } else null
    }
}