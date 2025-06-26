package ru.maplyb.unitmanagerlib.core.ui_kit

import android.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//containerColor: Color = FloatingActionButtonDefaults.containerColor,
//contentColor: Color = contentColorFor(containerColor),
@Composable
fun UnitManagerFab(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        content = content,
        containerColor = PrintMapColorSchema.colors.buttonBackgroundColor,
        contentColor = PrintMapColorSchema.colors.textColor
    )
}
