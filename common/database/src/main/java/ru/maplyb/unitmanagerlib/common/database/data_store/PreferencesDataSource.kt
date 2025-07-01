package ru.maplyb.unitmanagerlib.common.database.data_store

import android.content.Context

interface PreferencesDataSource {
    suspend fun setLastTable(context: Context, tableName: String)
    suspend fun getLastTable(context: Context): String?
    suspend fun removeLastTable(context: Context)
    companion object {
        fun create(): PreferencesDataSource {
            return DataStoreSource
        }
    }
}