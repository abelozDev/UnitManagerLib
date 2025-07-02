package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity

@Entity(primaryKeys = ["x", "y", "name"])
data class PositionEntity(
    val x: Int,
    val y: Int,
    val name: String
)
