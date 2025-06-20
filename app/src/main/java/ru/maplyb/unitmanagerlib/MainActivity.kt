package ru.maplyb.unitmanagerlib

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import ru.maplyb.unitmanagerlib.gui.api.UnitManager


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unitManager = UnitManager.create()
        unitManager.init(this)
        setContent {
            unitManager.TableHandler()
        }
    }

}

