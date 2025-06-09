package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = HeaderEntity::class,
            parentColumns = ["name"],
            childColumns = ["headersName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ValueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val headersName: String,
    val type: String,
    val values: List<String>
)