package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = HeaderEntity::class,
            parentColumns = ["name"],
            childColumns = ["tableName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["tableName", "name"]
)
data class ValuesTypeEntity(
    val tableName: String,
    val name: String
)
