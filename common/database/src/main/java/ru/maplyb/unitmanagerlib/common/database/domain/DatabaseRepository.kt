package ru.maplyb.unitmanagerlib.common.database.domain

import ru.maplyb.unitmanagerlib.common.database.UnitManagerDatabase
import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.common.database.repository.DatabaseRepositoryImpl

interface DatabaseRepository {
    suspend fun insertHeadersAndValues(
        tableName: String,
        headers: Map<String, List<String>>,
        values: Map<String, List<List<String>>>
    ): FileParsingResultDTO
    suspend fun getTableInfo(): FileParsingResultDTO?
    companion object {
        fun create(database: UnitManagerDatabase): DatabaseRepository {
            return DatabaseRepositoryImpl(database)
        }
    }
}