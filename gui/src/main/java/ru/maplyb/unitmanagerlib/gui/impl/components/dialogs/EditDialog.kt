package ru.maplyb.unitmanagerlib.gui.impl.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema
import ru.maplyb.unitmanagerlib.gui.impl.domain.EditDialogState

@Composable
internal fun EditDialog(
    editDialogState: EditDialogState
) {
    var text by remember {
        mutableStateOf(editDialogState.name)
    }
    Dialog(
        onDismissRequest = editDialogState.dismiss,
        content = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrintMapColorSchema.colors.backgroundColor)
                    .padding(16.dp)
            ) {
                Text(
                    color = PrintMapColorSchema.colors.textColor,
                    text = "Изменение ${editDialogState.name}"
                )
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    }
                )
                Button(
                    content = {
                        Text(
                            color = PrintMapColorSchema.colors.textColor,
                            text = "Применить",
                        )
                    },
                    onClick = {
                        editDialogState.confirm(text)
                    }
                )
            }
        }
    )
}