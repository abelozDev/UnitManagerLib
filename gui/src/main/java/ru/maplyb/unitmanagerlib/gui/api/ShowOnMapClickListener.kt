package ru.maplyb.unitmanagerlib.gui.api

import ru.maplyb.unitmanagerlib.gui.api.model.Position

interface ShowOnMapClickListener {
    fun onClick(position: Position)
}