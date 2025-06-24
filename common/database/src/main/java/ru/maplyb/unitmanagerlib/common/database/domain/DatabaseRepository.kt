package ru.maplyb.unitmanagerlib.common.database.domain

import kotlinx.coroutines.flow.Flow
import ru.maplyb.unitmanagerlib.common.database.UnitManagerDatabase
import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.common.database.repository.DatabaseRepositoryImpl

interface DatabaseRepository {
    suspend fun insertHeadersAndValues(
        tableName: String,
        headers: Map<String, List<String>>,
        values: Map<String, List<List<String>>>
    ): FileParsingResultDTO
    suspend fun addNewItem(type: String, tableName: String)
    suspend fun deleteItems(tableName: String, items: List<List<String>>)
    suspend fun moveItems(type: String, tableName: String, items: List<List<String>>)
    suspend fun getTableInfo(): FileParsingResultDTO?
    suspend fun getTableInfoFlow(name: String): Flow<FileParsingResultDTO?>
    suspend fun updateValues(type: String, rowIndex: Int, columnIndex: Int, newValue: String)
    fun getAllTablesNames(): Flow<List<String>>
    companion object {
        fun create(database: UnitManagerDatabase): DatabaseRepository {
            return DatabaseRepositoryImpl(database)
        }
    }
}