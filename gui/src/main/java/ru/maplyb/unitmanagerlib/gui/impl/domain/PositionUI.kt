package ru.maplyb.unitmanagerlib.gui.impl.domain

import ru.maplyb.unitmanagerlib.common.database.domain.model.PositionDTO

data class PositionUI(
    val id: Int = 0,
    val x: Double,
    val y: Double,
    val name: String
)
