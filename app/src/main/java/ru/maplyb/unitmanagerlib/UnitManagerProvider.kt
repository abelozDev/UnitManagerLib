package ru.maplyb.unitmanagerlib

import ru.maplyb.unitmanagerlib.gui.api.UnitManager

interface UnitManagerProvider {

    fun provideUnitManager(): UnitManager
}