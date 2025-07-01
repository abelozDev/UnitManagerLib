package ru.maplyb.unitmanagerlib.common.database.domain

import android.content.Context
import kotlinx.coroutines.flow.Flow
import ru.maplyb.unitmanagerlib.common.database.UnitManagerDatabase
import ru.maplyb.unitmanagerlib.common.database.data_store.PreferencesDataSource
import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.common.database.domain.model.PositionDTO
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
    suspend fun updateValues(
        tableName: String,
        type: String,
        rowIndex: Int,
        columnIndex: Int,
        newValue: String,
    )

    suspend fun setPosition(
        tableName: String,
        position: PositionDTO,
        type: String,
        rowIndex: Int,
    )

    @Deprecated("use local positions")
    suspend fun insertPositions(positions: List<PositionDTO>)

    @Deprecated("use local positions")
    fun positionsFlow(): Flow<List<PositionDTO>>
    fun getAllTablesNames(): Flow<List<String>>
    suspend fun deleteTable(tableName: String)
    suspend fun createNew(name: String)
    suspend fun setLastTable(context: Context, tableName: String)
    suspend fun getLastTable(context: Context): String?
    suspend fun removeLastTable(context: Context)
    companion object {
        fun create(
            database: UnitManagerDatabase,
            preferencesDataSource: PreferencesDataSource
        ): DatabaseRepository {
            return DatabaseRepositoryImpl(database, preferencesDataSource)
        }
    }
}