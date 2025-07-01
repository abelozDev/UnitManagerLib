package ru.maplyb.unitmanagerlib.common.database.data_store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal object DataStoreSource: PreferencesDataSource {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unit_manager_prefs")
    private val LAST_TABLE = stringPreferencesKey("UNIT_MANAGER_LAST_TABLE")

    override suspend fun setLastTable(context: Context, tableName: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_TABLE] = tableName
        }
    }

    override suspend fun removeLastTable(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(LAST_TABLE)
        }
    }

    override suspend fun getLastTable(context: Context): String? {
        return context.dataStore.data.map { prefs ->
            prefs[LAST_TABLE]
        }.first()
    }
}