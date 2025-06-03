package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HeaderEntity(
    @PrimaryKey val key: String,
    val values: List<String>
)