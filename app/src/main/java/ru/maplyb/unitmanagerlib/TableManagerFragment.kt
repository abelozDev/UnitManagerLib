package ru.maplyb.unitmanagerlib

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import ru.maplyb.unitmanagerlib.gui.api.ShowOnMapClickListener
import ru.maplyb.unitmanagerlib.gui.api.model.Position

class TableManagerFragment : Fragment(R.layout.table_manager_fragment) {

    private lateinit var composeView: ComposeView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView = view.findViewById(R.id.composeView)
        val unitManager = (activity as UnitManagerProvider).provideUnitManager()
        val list = listOf(
            Position(
                x = 1,
                y = 0,
                name = "test1"
            ),
            Position(
                x = 2,
                y = 0,
                name = "test2"
            ),
            Position(
                x = 3,
                y = 0,
                name = "test3"
            ),
            Position(
                x = 4,
                y = 0,
                name = "test4"
            ),
        )
        unitManager.updatePositions(list)
        unitManager.setShowOnMapClickListener(object : ShowOnMapClickListener {
            override fun onClick(position: Position) {
                Toast.makeText(context, position.toString(), Toast.LENGTH_LONG).show()
            }
        }
        )

        composeView.setContent {
            unitManager.TableHandler()
        }
    }
}