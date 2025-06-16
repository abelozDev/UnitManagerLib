package ru.maplyb.unitmanagerlib.gui.impl.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema

@Composable
internal fun MoveDialog(
    title: String,
    message: String,
    items: List<String>,
    onDismissRequest: () -> Unit,
    select: (String) -> Unit
) {
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
                    text = title,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    color = PrintMapColorSchema.colors.textColor,
                    text = message
                )
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(items.size) {
                        val item = items[it]
                        Text(
                            color = PrintMapColorSchema.colors.textColor,
                            modifier = Modifier
                                .clickable {
                                    select(item)
                                }
                                .padding(8.dp),
                            text = item
                        )
                    }
                }
            }
        }
    )
}
@Preview
@Composable
private fun PreviewMoveDialog() {
    MoveDialog(
        title = "Перемещение элементов",
        message = "Выберите, куда переместить элементы:",
        items = listOf("Петя", "Вася", "Женя"),
        onDismissRequest = {},
        select = {}
    )
}