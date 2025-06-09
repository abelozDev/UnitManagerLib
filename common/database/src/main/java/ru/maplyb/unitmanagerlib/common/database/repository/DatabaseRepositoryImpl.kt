package ru.maplyb.unitmanagerlib.common.database.repository

import androidx.room.Transaction
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
                values = values
            )
        } else null
    }
}