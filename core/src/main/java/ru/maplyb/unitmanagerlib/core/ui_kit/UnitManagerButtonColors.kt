package ru.maplyb.unitmanagerlib.core.ui_kit

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable

@Composable
fun UnitManagerButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = PrintMapColorSchema.colors.buttonBackgroundColor,
        contentColor = PrintMapColorSchema.colors.textColor
    )
}