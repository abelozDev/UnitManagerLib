package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PositionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val x: Double,
    val y: Double,
    val name: String
)
