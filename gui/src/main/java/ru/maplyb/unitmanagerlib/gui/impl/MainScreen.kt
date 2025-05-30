package ru.maplyb.unitmanagerlib.gui.impl

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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.maplyb.unitmanagerlib.parser.impl.convertToCsv

@Composable
fun MainScreen(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    share: (List<String>) -> Unit
) {
    NavigationTabExample(headersData, values, share)
}

@Composable
fun NavigationTabExample(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    share: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val destinations = values.keys.toList()
    val startDestination = destinations.first()
    var selectedDestination by rememberSaveable {
        mutableIntStateOf(
            destinations.indexOf(
                startDestination
            )
        )
    }
    Scaffold(modifier = modifier) { contentPadding ->
        Column {
            Button(
                content = {
                    Text(
                        text = "Импорт"
                    )
                },
                onClick = {
                    val list = convertToCsv(headersData, values)
                    share(list)
                }
            )
            ScrollableTabRow(
                selectedTabIndex = selectedDestination,
                modifier = Modifier
                    .padding(contentPadding),
                edgePadding = 16.dp
            ) {
                destinations.forEachIndexed { index, destination ->
                    Tab(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination)
                            selectedDestination = index
                        },
                        text = {
                            Text(
                                text = destination,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
            AppNavHost(navController, startDestination, destinations, headersData, values)
        }
    }
}
//val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//intent.addFlags(
//    Intent.FLAG_GRANT_READ_URI_PERMISSION or
//    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
//    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//)
//startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER)

@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    destinations: List<String>,
    headers: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    modifier: Modifier = Modifier
) {
    var valuesMutable by remember {
        mutableStateOf(values)
    }
    NavHost(
        navController,
        startDestination = startDestination
    ) {
        destinations.forEach { destination ->
            composable(destination) {
                val currentValues = valuesMutable[destination]
                Headers(headers, currentValues!!) {
                    val mutableMap = values.toMutableMap()
                    mutableMap[destination] = it
                    valuesMutable = mutableMap
                }
            }
        }
    }
}

@Preview(widthDp = 2000, showBackground = true)
@Composable
private fun HeadersPreview() {
    val headersData = mapOf("№п/п" to emptyList<String>(), "№" to emptyList<String>(), "Позывной" to emptyList<String>(), "№ жетона" to emptyList<String>(), "Должность" to emptyList<String>(), "Группа" to emptyList<String>(), "Вооружение" to listOf("тип", "№", "тип", "№"), "Средства связи" to listOf("рст", "телефон"), "Группа крови" to emptyList<String>(), "Позиция" to emptyList<String>(),)
    val values: List<List<String>> = listOf(
        listOf("1", "1", "Ленон", "В-77777123123123123123", "КВ", "Управление", "12345678901234567890", "7302118", "ПМ", "АА-1234", "771526480", "A256418JBK267", "О(I)-", "Фазенда"),
        listOf("1", "1", "Ленон", "В-77777", "КВ", "Управление", "АК", "7302118", "ПМ", "АА-1234", "771526480", "A256418JBK267", "О(I)-", "Фазенда"),
        listOf("1", "1", "Ленон", "В-77777", "КВ", "Управление", "АК", "7302118", "ПМ", "АА-1234", "771526480", "A256418JBK267", "О(I)-", "Фазенда")
    )

    Headers(
        headersData = headersData,
        values = values,
        {}
    )
}

data class EditDialogState(
    val name: String = "",
    val visibility: Boolean = false,
    val confirm: (String) -> Unit,
    val dismiss: () -> Unit
) {
    companion object {
        val default = EditDialogState(
            "", false, {}, {}
        )
    }
}
@Composable
private fun Headers(
    headersData: Map<String, List<String>>,
    values: List<List<String>>,
    updateValues: (List<List<String>>) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    var currentValuesIndex by remember { mutableIntStateOf(0) }
    val textMeasurer = rememberTextMeasurer()
    var editDialogState by remember {
        mutableStateOf(EditDialogState.default)
    }
    Row(
        modifier = Modifier
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .fillMaxWidth()
    ) {
        currentValuesIndex = 0
        headersData.forEach { (mainHeader, subHeaders) ->
            val maxTextSizeByAllSubheaders = mutableListOf<String>()
            for (i in currentValuesIndex..currentValuesIndex + subHeaders.lastIndex + 1) {
                maxTextSizeByAllSubheaders.add(values.getValuesByIndex(i).maxByOrNull { it.length }
                    ?: "")
            }
            val maxText =
                listOf(maxTextSizeByAllSubheaders.joinToString(), mainHeader).maxBy { it.length }
            println("maxTextSizeByAllSubheaders = ${maxTextSizeByAllSubheaders.joinToString()}")
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(maxText),
                style = textStyle,
                constraints = Constraints() // без ограничений
            )
            val maxWidth = with(LocalDensity.current) {
                textLayoutResult.size.width.toDp()
            }
            Column(
                modifier = Modifier
                    .width(maxWidth + (32 * (subHeaders.size + 1)).dp)
                    .border(1.dp, Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*Основной хедер*/
                Text(
                    text = mainHeader,
                    style = textStyle,
                    modifier = Modifier
                        .padding(4.dp),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                if (subHeaders.isNotEmpty()) {
                    val columnWidths = remember {
                        subHeaders.mapIndexed { index, sub ->
                            val valuesAtIndex = values.getValuesByIndex(currentValuesIndex + index)
                            val allText = listOf(sub, *valuesAtIndex.toTypedArray())
                            val maxText = allText.maxByOrNull { it.length } ?: ""
                            val measured = textMeasurer.measure(AnnotatedString(maxText))
                            measured.size.width
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
                                    text = sub,
                                    maxLines = 1,
                                    style = textStyle,
                                    modifier = Modifier
                                        .height(24.dp)
                                        .fillMaxWidth()
                                        .border(1.dp, Color.Black)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                                /*Значения*/
                                valuesAtIndex.forEachIndexed { valuesIndex, value ->
                                    val thisCurrentValueIndex by remember {
                                        mutableStateOf(currentValuesIndex)
                                    }
                                    Text(
                                        text = value,
                                        style = textStyle,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onLongClick = {
                                                    editDialogState = EditDialogState(
                                                        name = value,
                                                        visibility = true,
                                                        dismiss = {
                                                            editDialogState = editDialogState.copy(visibility = false)
                                                        },
                                                        confirm = {
                                                            val mutableList = values[valuesIndex].toMutableList()
                                                            mutableList[thisCurrentValueIndex] = it
                                                            val mutableValuesList = values.toMutableList()
                                                            mutableValuesList[valuesIndex] = mutableList
                                                            updateValues(mutableValuesList)
                                                            editDialogState = editDialogState.copy(visibility = false)
                                                        }
                                                    )
                                                },
                                                onClick = {}
                                            )
                                            .wrapContentHeight()
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Black)
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
                                mutableStateOf(currentValuesIndex)
                            }
                            Text(
                                text = value,
                                maxLines = 1,
                                style = textStyle,
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            editDialogState = EditDialogState(
                                                name = value,
                                                visibility = true,
                                                dismiss = {
                                                    editDialogState = editDialogState.copy(visibility = false)
                                                },
                                                confirm = {
                                                    val mutableList = values[index].toMutableList()
                                                    mutableList[thisCurrentValueIndex] = it
                                                    val mutableValuesList = values.toMutableList()
                                                    mutableValuesList[index] = mutableList
                                                    updateValues(mutableValuesList)
                                                    editDialogState = editDialogState.copy(visibility = false)
                                                }
                                            )
                                        },
                                        onClick = {}
                                    )
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Black)
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

private fun List<List<String>>.getValuesByIndex(index: Int): List<String> {
    return map { it.getOrNull(index) ?: "" }
}

private val textStyle = TextStyle(
    fontSize = 16.sp,
    textAlign = TextAlign.Center,
)