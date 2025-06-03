package ru.maplyb.unitmanagerlib.gui.api

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import ru.maplyb.unitmanagerlib.gui.impl.UnitManagerImpl
import java.io.File

interface UnitManager {

    fun init(activity: Activity)

    @Composable
    fun Show(uri: Uri?)

    companion object {
        fun create(): UnitManager {
            return UnitManagerImpl()
        }
    }
}