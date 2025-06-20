package ru.maplyb.unitmanagerlib

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

class TableManagerFragment: Fragment(R.layout.table_manager_fragment) {

    private lateinit var composeView: ComposeView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView = view.findViewById(R.id.composeView)
        val unitManager = (activity as UnitManagerProvider).provideUnitManager()
        composeView.setContent {
            unitManager.TableHandler()
        }
    }
}