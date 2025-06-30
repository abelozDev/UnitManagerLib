package ru.maplyb.unitmanagerlib.common.database.domain.model

import ru.maplyb.unitmanagerlib.common.database.entity.PositionEntity

data class PositionDTO(
    val id: Int = 0,
    val x: Double,
    val y: Double,
    val name: String
) {
    fun toEntity(): PositionEntity = PositionEntity(id = id, x = x, y = y, name = name)
}

fun PositionEntity.toDTO(): PositionDTO = PositionDTO(id = id, x = x, y = y, name = name)
