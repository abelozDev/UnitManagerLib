package ru.maplyb.unitmanagerlib.gui.impl.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.headerNoClickingPossible
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema
import ru.maplyb.unitmanagerlib.core.ui_kit.TableTextStyle
import ru.maplyb.unitmanagerlib.core.util.getValuesByIndex
import ru.maplyb.unitmanagerlib.core.util.types.RowIndex
import ru.maplyb.unitmanagerlib.gui.impl.components.dialogs.EditDialog
import ru.maplyb.unitmanagerlib.gui.impl.domain.EditDialogState

@Composable
internal fun Table(
    headersData: Map<String, List<String>>,
    values: List<List<String>>,
    selectMode: Boolean = false,
    selectItem: (RowIndex) -> Unit = {},
    selectedValues: List<RowIndex> = emptyList(),
    updateValues: (
        rowIndex: Int,
        columnIndex: Int,
        newValue: String
    ) -> Unit = {_,_,_ -> },
    showOnMap: (columnIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()
    var editDialogState by remember {
        mutableStateOf(EditDialogState.default)
    }
    Row(
        modifier = modifier
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        var currentValuesIndex = 0
        headersData.forEach { (mainHeader, subHeaders) ->
            val maxTextSizeByAllSubheaders = mutableListOf<String>()
            for (i in currentValuesIndex..currentValuesIndex + subHeaders.lastIndex + 1) {
                maxTextSizeByAllSubheaders.add(values.getValuesByIndex(i).maxByOrNull { it.length }
                    ?: "")
            }
            val maxText =
                listOf(maxTextSizeByAllSubheaders.joinToString(), mainHeader).maxBy { it.length }
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(maxText),
                style = TableTextStyle(),
                constraints = Constraints()
            )
            val maxWidth = with(LocalDensity.current) {
                textLayoutResult.size.width.toDp()
            }
            Column(
                modifier = Modifier
                    .width(maxWidth + (32 * (subHeaders.size + 1)).dp)
                    .border(1.dp, PrintMapColorSchema.colors.textColor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*Основной хедер*/
                Text(
                    color = PrintMapColorSchema.colors.textColor,
                    text = mainHeader,
                    style = TableTextStyle(),
                    modifier = Modifier
                        .padding(4.dp),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                if (subHeaders.isNotEmpty()) {
                    val columnWidths = remember(values) {
                        subHeaders.mapIndexed { index, sub ->
                            val valuesAtIndex = values.getValuesByIndex(currentValuesIndex + index)
                            val allText = listOf(sub, *valuesAtIndex.toTypedArray())
                            val _maxText = allText.maxByOrNull { it.length } ?: ""
                            val measured = textMeasurer.measure(AnnotatedString(_maxText))
                            println("max text: $_maxText, width: ${measured.size.width}")
                            maxOf(measured.size.width, 70)
                        }
                    }
                    val totalWidth = columnWidths.sum()
                    Row {
                        subHeaders.forEachIndexed { index, sub ->
                            val valuesAtIndex = values.getValuesByIndex(currentValuesIndex)
                            val weight = columnWidths[index].toFloat() / totalWidth.toFloat()

                            Column(
                                modifier = Modifier.weight(weight)
                            ) {
                                /*Дочерний хедер*/
                                Text(
                                    color = PrintMapColorSchema.colors.textColor,
                                    text = sub,
                                    maxLines = 1,
                                    style = TableTextStyle(),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .fillMaxWidth()
                                        .border(1.dp, PrintMapColorSchema.colors.textColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                                /*Значения*/
                                valuesAtIndex.forEachIndexed { valuesIndex, value ->
                                    val thisCurrentValueIndex by remember {
                                        mutableStateOf(currentValuesIndex)
                                    }
                                    Text(
                                        color = PrintMapColorSchema.colors.textColor,
                                        text = value,
                                        style = TableTextStyle(),
                                        maxLines = 1,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onLongClick = {
                                                    if (headerNoClickingPossible.contains(mainHeader)) {
                                                        return@combinedClickable
                                                    }
                                                    editDialogState = EditDialogState(
                                                        name = value,
                                                        visibility = true,
                                                        dismiss = {
                                                            editDialogState =
                                                                editDialogState.copy(visibility = false)
                                                        },
                                                        confirm = {
                                                            updateValues(
                                                                thisCurrentValueIndex,
                                                                valuesIndex,
                                                                it
                                                            )
                                                            editDialogState =
                                                                editDialogState.copy(visibility = false)
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    if (selectMode) {
                                                        selectItem(valuesIndex)
                                                    } else {
                                                        showOnMap(valuesIndex)
                                                    }
                                                }
                                            )
                                            .wrapContentHeight()
                                            .fillMaxWidth()
                                            .then(
                                                if (selectedValues.contains(valuesIndex)) {
                                                    Modifier.background(Color.Green)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .border(1.dp, PrintMapColorSchema.colors.textColor)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            currentValuesIndex++
                        }
                    }
                } else {
                    Spacer(Modifier.height(24.dp))
                    Column {
                        /*Значения*/
                        values.getValuesByIndex(currentValuesIndex).forEachIndexed { index, value ->
                            val thisCurrentValueIndex by remember {
                                mutableIntStateOf(currentValuesIndex)
                            }
                            Text(
                                color = PrintMapColorSchema.colors.textColor,
                                text = value,
                                maxLines = 1,
                                style = TableTextStyle(),
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            if (headerNoClickingPossible.contains(mainHeader)) {
                                                return@combinedClickable
                                            }
                                            editDialogState = EditDialogState(
                                                name = value,
                                                visibility = true,
                                                dismiss = {
                                                    editDialogState =
                                                        editDialogState.copy(visibility = false)
                                                },
                                                confirm = {
                                                    updateValues(
                                                        thisCurrentValueIndex,
                                                        index,
                                                        it
                                                    )
                                                    editDialogState =
                                                        editDialogState.copy(visibility = false)
                                                }
                                            )
                                        },
                                        onClick = {
                                            if (selectMode) {
                                                selectItem(index)
                                            } else {
                                                showOnMap(index)
                                            }
                                        }
                                    )
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .then(
                                        if (selectedValues.contains(index)) {
                                            Modifier.background(Color.Green)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .border(1.dp, PrintMapColorSchema.colors.textColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                    currentValuesIndex++
                }
            }
        }
    }
    if (editDialogState.visibility) {
        EditDialog(editDialogState)
    }
}