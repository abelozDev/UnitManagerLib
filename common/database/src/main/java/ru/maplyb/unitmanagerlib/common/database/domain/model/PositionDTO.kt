package ru.maplyb.unitmanagerlib.common.database.domain.model

import ru.maplyb.unitmanagerlib.common.database.entity.PositionEntity

data class PositionDTO(
    val x: Double,
    val y: Double,
    val name: String
) {
    fun toEntity(): PositionEntity = PositionEntity(x = x, y = y, name = name)
}

fun PositionEntity.toDTO(): PositionDTO = PositionDTO(x = x, y = y, name = name)
