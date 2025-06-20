package ru.maplyb.unitmanagerlib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.maplyb.unitmanagerlib.gui.api.UnitManager


class MainActivity : AppCompatActivity(R.layout.activity_main), UnitManagerProvider {

    private val unitManager by lazy {
        UnitManager.create().apply {
            init(this@MainActivity)
        }
    }

    override fun provideUnitManager(): UnitManager {
        return unitManager
    }

}

