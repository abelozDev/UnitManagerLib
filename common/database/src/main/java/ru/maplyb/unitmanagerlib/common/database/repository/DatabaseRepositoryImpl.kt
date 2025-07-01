package ru.maplyb.unitmanagerlib.common.database.repository

import android.content.Context
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.maplyb.unitmanagerlib.common.database.UnitManagerDatabase
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.defaultUnitManagerTableHeaders
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.defaultUnitManagerValueTypes
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.findPositionInDefaultHeaders
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.headersSize
import ru.maplyb.unitmanagerlib.common.database.data_store.PreferencesDataSource
import ru.maplyb.unitmanagerlib.common.database.domain.DatabaseRepository
import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.common.database.domain.model.PositionDTO
import ru.maplyb.unitmanagerlib.common.database.domain.model.toDTO
import ru.maplyb.unitmanagerlib.common.database.entity.HeaderEntity
import ru.maplyb.unitmanagerlib.common.database.entity.HeadersWithValues
import ru.maplyb.unitmanagerlib.common.database.entity.ValueEntity
import ru.maplyb.unitmanagerlib.common.database.entity.ValuesTypeEntity
import ru.maplyb.unitmanagerlib.core.util.subListExclusiveToInclusive

internal class DatabaseRepositoryImpl(
    private val database: UnitManagerDatabase,
    private val preferencesDataSource: PreferencesDataSource
) : DatabaseRepository {

    override fun getAllTablesNames(): Flow<List<String>> {
        return database.headerDao().getAllFLow()
            .map { list ->
                list.map {
                    it.name
                }
            }
    }

    override suspend fun deleteTable(tableName: String) {
        database.headerDao().deleteHeaders(tableName)
    }

    override suspend fun createNew(name: String) {
        val modifiedName = if (!name.endsWith(".csv")) "$name.csv" else name
        val valueTypesList = buildList {
            defaultUnitManagerValueTypes.forEachIndexed { index, value ->
                add(
                    ValuesTypeEntity(
                        tableName = modifiedName,
                        name = value,
                        orderIndex = index
                    )
                )
            }
        }
        val entity = HeaderEntity(
            modifiedName,
            defaultUnitManagerTableHeaders
        )
        database.headerDao().insert(entity)
        database.valuesTypeDao().insertTypes(valueTypesList)
    }

    override suspend fun setLastTable(context: Context, tableName: String) {
        preferencesDataSource.setLastTable(context, tableName)
    }

    override suspend fun getLastTable(context: Context): String? {
        val lastTable = preferencesDataSource.getLastTable(context)
        val allTables = database.headerDao().getAll()
        return if (allTables.map { it.name }.contains(lastTable)) {
            lastTable
        } else {
            preferencesDataSource.removeLastTable(context)
            null
        }
    }

    override suspend fun removeLastTable(context: Context) {
        preferencesDataSource.removeLastTable(context)
    }

    @Transaction
    override suspend fun insertHeadersAndValues(
        tableName: String,
        headers: Map<String, List<String>>,
        values: Map<String, List<List<String>>>
    ): FileParsingResultDTO {
        val mutableHeaders = headers.toMutableMap()
        val positionItems = listOf("X", "Y", "Название")
        mutableHeaders["Позиция"] = positionItems
        database.headerDao().insert(
            HeaderEntity(
                name = tableName,
                value = mutableHeaders
            )
        )
        val types = buildList {
            values.keys.forEachIndexed { index, s ->
                add(
                    ValuesTypeEntity(
                        tableName = tableName,
                        name = s,
                        orderIndex = index
                    )
                )
            }
        }
        database.valuesTypeDao().insertTypes(types)
        val valueEntities = buildList {
            values.keys.forEach { type ->
                values[type]?.forEach { value ->
                    val newValue = List(headersSize) {
                        value.getOrElse(it) { "" }
                    }
                    add(
                        ValueEntity(
                            headersName = tableName,
                            type = type,
                            values = newValue
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
        val allByType = allValues.filter { it.type == type }
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
    private suspend fun deleteAndGet(
        tableName: String,
        deleteItems: List<ValueEntity>
    ): List<ValueEntity> {
        database.valueDao().deleteByIds(deleteItems.map { it.id })
        return database.valueDao().getAllByTableName(tableName)
    }

    @Transaction
    override suspend fun addNewItem(type: String, tableName: String) {
        val headersByTableName = database.headerDao().getByTableName(tableName)
        val allTypes = database.valuesTypeDao().getAllTypeNamesByTableName(tableName)
        val currentIndex = allTypes.indexOf(type)
        val typesBeforeCurrent = allTypes.subList(0, currentIndex)

        val typesAfterCurrent =
            allTypes.subListExclusiveToInclusive(currentIndex, allTypes.lastIndex)
        val allValues = database.valueDao().getAllByTableName(tableName).toMutableList()

        val valuesBeforeCurrentType = allValues.filter { typesBeforeCurrent.contains(it.type) }

        val valuesByType = allValues.filter { it.type == type }.map {
            it.values
        }

        val size = headersByTableName.value.map { it.value }
            .sumOf { it.size } + headersByTableName.value.size
        /*Новое значение*/
        val newItem = MutableList(size) { "" }
        newItem[0] = ((valuesByType.lastOrNull()?.get(0)?.toInt()
            ?: valuesBeforeCurrentType.size) + 1).toString()
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
            getHeadersWithValuesFlow(name).map {
                val values = it.values
                    .sortedBy { it.values[0].toInt() }
                    .groupBy {
                        it.type
                    }
                    .map {
                        it.key to it.value.map { it.values }
                    }
                    .toMap()
                FileParsingResultDTO(
                    headers = it.header.value,
                    values = values,
                    tableName = name,
                    valueTypes = it.valuesTypes.map { it.name }
                )
            }
        } else flow { emit(null) }
    }

    override suspend fun updateValues(
        tableName: String,
        type: String,
        rowIndex: Int,
        columnIndex: Int,
        newValue: String,
    ) {
        val allByType = database.valueDao().getAllByTypeAndTableName(type, tableName)
        val currentValue = allByType[columnIndex]
        val mutableValues = currentValue.values.toMutableList()
        mutableValues[rowIndex] = newValue
        val updated = currentValue.copy(
            values = mutableValues
        )
        database.valueDao().insertValue(updated)
    }

    override suspend fun setPosition(
        tableName: String,
        position: PositionDTO,
        type: String,
        rowIndex: Int
    ) {
        val allByType = database.valueDao().getAllByTypeAndTableName(type, tableName)
        val currentValue = allByType[rowIndex]
        val mutableValues = currentValue.values.toMutableList()
        val xIndex =
            findPositionInDefaultHeaders("X").firstOrNull() ?: error("header \"X\" not found")
        val yIndex =
            findPositionInDefaultHeaders("Y").firstOrNull() ?: error("header \"Y\" not found")
        val nameIndex = findPositionInDefaultHeaders("Название").firstOrNull()
            ?: error("header \"Название\" not found")
        mutableValues[xIndex] = position.x.toString()
        mutableValues[yIndex] = position.y.toString()
        mutableValues[nameIndex] = position.name
        val updated = currentValue.copy(
            values = mutableValues
        )
        database.valueDao().insertValue(updated)
    }

    override suspend fun insertPositions(positions: List<PositionDTO>) {
        database.positionDao().insertPositions(positions.map { it.toEntity() })
    }

    override fun positionsFlow(): Flow<List<PositionDTO>> {
        return database.positionDao().getPositionsFlow().map { list ->
            list.map {
                it.toDTO()
            }
        }
    }

    override suspend fun getTableInfo(): FileParsingResultDTO? {
        return if (database.databaseIsNotEmpty()) {
            /*Получаем хедер (пока там может быть только один)*/
            val headers = database.headerDao().getAll().first()
            val headersWithValues = getHeadersWithValues(headers.name)
            val valuesCategories = headersWithValues.values.map { it.type }.toSet()
            println("valuesCategories = $valuesCategories")
            val values = buildMap<String, List<List<String>>> {
                valuesCategories.forEach { category ->
                    put(
                        category,
                        headersWithValues.values.filter { it.type == category }
                            .map { it.values })
                }
            }
            FileParsingResultDTO(
                headers = headersWithValues.header.value,
                values = values,
                tableName = headersWithValues.header.name,
                valueTypes = headersWithValues.valuesTypes.map { it.name }
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

    fun getHeadersWithValuesFlow(name: String): Flow<HeadersWithValues> {
        return combine(
            database.headerDao().getByTableNameFlow(name),
            database.valueDao().getAllByTableNameFlow(name),
            database.valuesTypeDao().getAllByTableNameFlow(name)
        ) { header, value, valueType ->
            HeadersWithValues(
                header = header,
                values = value,
                valuesTypes = valueType
            )
        }
    }

    @Transaction
    suspend fun getHeadersWithValues(name: String): HeadersWithValues {
        val header = database.headerDao().getByTableName(name)
        val values = database.valueDao().getAllByTableName(name)
        val valuesTypes = database.valuesTypeDao().getAllByTableName(name)
        return HeadersWithValues(header, values, valuesTypes)
    }
}