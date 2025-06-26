package ru.maplyb.unitmanagerlib.gui.impl.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema
import ru.maplyb.unitmanagerlib.core.ui_kit.UnitManagerButtonColors
import ru.maplyb.unitmanagerlib.gui.impl.domain.EditDialogState

@Composable
internal fun CreateTableDialog(
    onDismissRequest: () -> Unit = {},
    confirm: (name: String) -> Unit = {}
) {
    var text by remember {
        mutableStateOf("")
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrintMapColorSchema.colors.backgroundColor)
                    .padding(16.dp)
            ) {
                Text(
                    color = PrintMapColorSchema.colors.textColor,
                    text = "Задайте имя таблицы"
                )
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    }
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    colors = UnitManagerButtonColors(),
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    enabled = text.isNotEmpty(),
                    content = {
                        Text(
                            color = PrintMapColorSchema.colors.textColor,
                            text = "Применить",
                        )
                    },
                    onClick = {
                        confirm(text)
                    }
                )
            }
        }
    )
}

@Composable
@Preview
private fun PreviewCreateTableDialog() {
    CreateTableDialog()
}