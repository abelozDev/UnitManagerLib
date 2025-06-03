package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val rows: List<List<String>>
)