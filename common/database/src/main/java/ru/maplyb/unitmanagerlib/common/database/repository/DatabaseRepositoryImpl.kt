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
) : DatabaseRepository {

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

    /*изменение индекса*/
    @Transaction
    override suspend fun moveItems(type: String, tableName: String, items: List<List<String>>) {
        /*Все значения в таблице*/
        val allValues = database.valueDao().getAllByTableName(tableName).toMutableList()
        val allByType = database.valueDao().getAllByType(type)
        items.forEachIndexed { itemIndex, strings ->
            val index = allValues.map { it.values }.indexOf(strings)
            val mutableStrings = strings.toMutableList()
            mutableStrings[0] = ((allByType.maxBy { it.values[0] }.values[0].toIntOrNull()
                ?: -1) + itemIndex + 1).toString()
            if (index != -1) {
                allValues[index] = ValueEntity(
                    id = allValues[index].id,
                    headersName = tableName,
                    type = type,
                    values = mutableStrings
                )
            }
        }
        val sorted = sortValues(allValues)
        database.valueDao().insertValues(sorted)
    }


    @Transaction
    override suspend fun deleteItems(tableName: String, items: List<List<String>>) {
        val allValues = database.valueDao().getAllByTableName(tableName).toMutableList()
        val filtered = allValues.filter {
            items.contains(it.values)
        }
        val newValues = deleteAndGet(tableName, filtered)
        val sorted = sortValues(newValues)
        database.valueDao().insertValues(sorted)
    }

    @Transaction
    private suspend fun deleteAndGet(tableName: String, deleteItems: List<ValueEntity>): List<ValueEntity> {
        database.valueDao().deleteByIds(deleteItems.map { it.id })
        return database.valueDao().getAllByTableName(tableName)
    }

    @Transaction
    override suspend fun addNewItem(type: String, tableName: String) {
        val headersByTableName = database.headerDao().getByTableName(tableName)
        val allTypes = database.valueDao().getAllUniqueTypes()
        val typesAfterCurrent = allTypes.subList(allTypes.indexOf(type) + 1, allTypes.lastIndex)
        val allValues = database.valueDao().getAllByTableName(tableName).toMutableList()
        val valuesByType = allValues.filter { it.type == type }.map {
            it.values
        }
        val size = headersByTableName.value.map { it.value }
            .sumOf { it.size } + headersByTableName.value.size
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
                val values = it?.values
                    ?.sortedBy { it.values[0].toInt() }
                    ?.groupBy {
                        it.type
                    }
                    ?.map {
                        it.key to it.value.map { it.values }
                    }
                    ?.toMap()
                FileParsingResultDTO(
                    headers = it?.header?.value!!,
                    values = values!!,
                    tableName = name
                )
            }
        } else flow { emit(null) }
    }

    override suspend fun updateValues(
        type: String,
        rowIndex: Int,
        columnIndex: Int,
        newValue: String,
    ) {
        val allByType = database.valueDao().getAllByType(type)
        val currentValue = allByType[columnIndex]
        val mutableValues = currentValue.values.toMutableList()
        mutableValues[rowIndex] = newValue
        val updated = currentValue.copy(
            values = mutableValues
        )
        database.valueDao().insertValue(updated)
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

    private suspend fun sortValues(values: List<ValueEntity>): List<ValueEntity> {
        val allUniqueTypes = database.valueDao().getAllUniqueTypes()
        val typesMap = allUniqueTypes.withIndex().associate { it.value to it.index }
        val changedLocalNumber = values
            .groupBy { it.type }
            .map { (_, value) ->
                value.mapIndexed { index, valueEntity ->
                    val mutableValues = valueEntity.values.toMutableList()
                    mutableValues[1] = (index + 1).toString()
                    valueEntity.copy(
                        values = mutableValues
                    )
                }
            }
            .flatten()
        return changedLocalNumber
            .sortedBy { typesMap[it.type] }
            .mapIndexed { index, valueEntity ->
                val modifiedValue = valueEntity.values.toMutableList()
                /*Обновлять индекс 1*/
                modifiedValue[0] = (index + 1).toString()
                valueEntity.copy(
                    values = modifiedValue
                )
            }

    }
}