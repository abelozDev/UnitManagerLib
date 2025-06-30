package ru.maplyb.unitmanagerlib

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import ru.maplyb.unitmanagerlib.gui.api.model.Position

class TableManagerFragment : Fragment(R.layout.table_manager_fragment) {

    private lateinit var composeView: ComposeView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView = view.findViewById(R.id.composeView)
        val unitManager = (activity as UnitManagerProvider).provideUnitManager()
        val list = listOf(
            Position(
                x = 1.0,
                y = 0.0,
                name = "test1"
            ),
            Position(
                x = 2.0,
                y = 0.0,
                name = "test2"
            ),
            Position(
                x = 3.0,
                y = 0.0,
                name = "test3"
            ),
            Position(
                x = 4.0,
                y = 0.0,
                name = "test4"
            ),
        )
        unitManager.updatePositions(list)
        composeView.setContent {
            unitManager.TableHandler()
        }
    }
}