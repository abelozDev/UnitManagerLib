package ru.maplyb.unitmanagerlib.common.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param name - имя таблицы
 * @param value - все значения хедера*/
@Entity
data class HeaderEntity(
    @PrimaryKey
    val name: String,
    val value: Map<String, List<String>>
)