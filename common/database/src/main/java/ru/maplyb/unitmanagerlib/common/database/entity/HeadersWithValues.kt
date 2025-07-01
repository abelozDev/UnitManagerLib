package ru.maplyb.unitmanagerlib.common.database.entity


data class HeadersWithValues(
    val header: HeaderEntity,
    val values: List<ValueEntity>,
    val valuesTypes: List<ValuesTypeEntity>
)