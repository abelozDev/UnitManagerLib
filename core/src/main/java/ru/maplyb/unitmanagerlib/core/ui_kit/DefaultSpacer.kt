package ru.maplyb.unitmanagerlib.core.ui_kit

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.DefaultVerticalSpacer(height: Int = 16) {
    Spacer(Modifier.height(height.dp))
}

@Composable
fun RowScope.DefaultHorizontalSpacer(width: Int = 16) {
    Spacer(Modifier.width(16.dp))
}