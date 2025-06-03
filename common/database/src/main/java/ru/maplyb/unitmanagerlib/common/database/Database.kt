package ru.maplyb.unitmanagerlib.common.database

import android.content.Context
import androidx.room.Room

object Database {
    private var dbInstance: UnitManagerDatabase? = null

    fun provideDatabase(context: Context): UnitManagerDatabase {
        if (dbInstance == null) {
            synchronized(UnitManagerDatabase::class) {
                if (dbInstance == null) {
                    dbInstance = Room.databaseBuilder(
                        context.applicationContext,
                        UnitManagerDatabase::class.java,
                        "unit_manager_database.db"
                    ).build()
                }
            }
        }
        return dbInstance!!
    }
}