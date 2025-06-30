package ru.maplyb.unitmanagerlib.gui.impl.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.maplyb.unitmanagerlib.core.ui_kit.DefaultVerticalSpacer
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema
import ru.maplyb.unitmanagerlib.gui.impl.domain.PositionUI

@Composable
internal fun SelectPositionDialog(
    positions: List<PositionUI>,
    onDismissRequest: () -> Unit = {},
    select: (PositionUI) -> Unit = {}
) {
    var input by rememberSaveable {
        mutableStateOf("")
    }
    val filteredPositions by rememberSaveable(input, positions) {
        mutableStateOf(
            filterPositions(positions, input)
        )
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
                    text = "Выберите позицию"
                )
                DefaultVerticalSpacer()
                TextField(
                    value = input,
                    onValueChange = {
                        input = it
                    }
                )
                DefaultVerticalSpacer()
                LazyColumn {
                    items(filteredPositions.size) {
                        val item = filteredPositions[it]
                        PositionItem(item) {
                            select(item)
                        }
                    }
                }
            }
        }
    )
}

private fun filterPositions(positions: List<PositionUI>, input: String): List<PositionUI> {
    val query = input.trim()
    return positions.filter { it.name.contains(query, ignoreCase = true) }
}

@Composable
private fun PositionItem(position: PositionUI, onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(vertical = 4.dp),
        fontSize = 16.sp,
        text = "${position.name}(${position.x}, ${position.y})"
    )
}

@Preview
@Composable
private fun PreviewSelectPositionDialog() {
    val positionsList = List(10) {
        PositionUI(
            x = 0.0,
            y = 0.0,
            name = "test"
        )
    }
    SelectPositionDialog(
        positionsList
    )
}