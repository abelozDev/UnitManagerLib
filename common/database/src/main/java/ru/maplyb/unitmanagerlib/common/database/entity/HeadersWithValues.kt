package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HeadersWithValues(
    @Embedded val header: HeaderEntity,

    @Relation(
        parentColumn = "name",
        entityColumn = "headersName"
    )
    val values: List<ValueEntity>
)