package ru.maplyb.unitmanagerlib.gui.impl

import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.maplyb.unitmanagerlib.core.util.types.RowIndex
import ru.maplyb.unitmanagerlib.gui.R
import ru.maplyb.unitmanagerlib.gui.impl.components.dialogs.ConfirmDialog
import ru.maplyb.unitmanagerlib.gui.impl.components.dialogs.MoveDialog
import ru.maplyb.unitmanagerlib.gui.impl.table.Table
import ru.maplyb.unitmanagerlib.parser.impl.convertToCsv

@Composable
fun MainScreen(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    share: (List<String>) -> Unit,
    addItem: (type: String) -> Unit,
) {
    NavigationTabExample(headersData, values, share, addItem)
}

@Composable
fun NavigationTabExample(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    share: (List<String>) -> Unit,
    addItem: (type: String) -> Unit,
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
    var selectMode by remember {
        mutableStateOf(false)
    }
    var selectedMap by remember {
        mutableStateOf(mapOf<String, List<RowIndex>>())
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    var showMoveDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(modifier = modifier) { contentPadding ->
        Column {
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    content = {
                        Icon(
                            Icons.Default.Share, null
                        )
                    },
                    onClick = {
                        val list = convertToCsv(headersData, values)
                        share(list)
                    }
                )
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (selectMode) Color.Green else Color.Unspecified
                        )
                    },
                    onClick = {
                        selectMode = !selectMode
                    }
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectMode,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Row {
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showDeleteDialog = true
                            }
                        )
                        IconButton(
                            content = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_move),
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showMoveDialog = true
                            }
                        )
                    }
                }
            }
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
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                destinations = destinations,
                headers = headersData,
                valuesMutable = values,
                selectMode = selectMode,
                selectedMap = selectedMap,
                updateSelectedMap = {
                    selectedMap = it
                },
                updateValues = {
                    //todo
//                    valuesMutable = it
                },
                addItem = addItem
            )
        }
    }
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Удаление",
            message = "Вы уверены, что хотите удалить элементы из списка?",
            onConfirm = {
                //todo
//                valuesMutable = deleteItems(valuesMutable, selectedMap)
                selectMode = false
                showDeleteDialog = false
            },
            onDismissRequest = {
                showDeleteDialog = false
            }
        )
    }
    if (showMoveDialog) {
        MoveDialog(
            title = "Перемещение элементов",
            message = "Выберите, куда переместить элементы:",
            items = destinations,
            onDismissRequest = {
                showMoveDialog = false
            },
            select = { header ->
                //todo
                val newValues = deleteItems(values, selectedMap).toMutableMap()
                val listToAdd = buildList {
                    selectedMap.entries.forEach { (key, value) ->
                        values[key]?.forEachIndexed { index, list ->
                            if (value.contains(index)) add(list)
                        }
                    }
                }
                newValues[header] = newValues[header]?.plus(listToAdd) ?: emptyList()
//                valuesMutable = newValues
                showMoveDialog = false
                selectMode = false
            }
        )
    }
}

private fun deleteItems(
    valuesMutable: Map<String, List<List<String>>>,
    selectedMap: Map<String, List<RowIndex>>
): Map<String, List<List<String>>> {
    val newValues = valuesMutable.toMutableMap()
    selectedMap.forEach { (key, value) ->
        newValues[key].takeIf { newValues.containsKey(key)}?.let {
            newValues[key] = it.filterIndexed { index, strings ->
                !value.contains(index)
            }
        }
    }
    return newValues
}

private fun addItem(
    valuesMutable: Map<String, List<List<String>>>,
    headers: Map<String, List<String>>,
    currentDestination: String,
    destinations: List<String>,
    currentValues: List<List<String>>?
): Map<String, List<List<String>>> {
    val mutableMap = valuesMutable.toMutableMap()
    val mutableDestinationMap = mutableMap[currentDestination]?.toMutableList()
    val size = headers.values.sumOf { it.size } + headers.size
    /*Размер списка для нового элемента*/
    val newItem = MutableList(size) { "" }
    /*Порядковый номер общий и в подразделении*/
    newItem[0] = ((currentValues?.lastOrNull()?.get(0)?.toInt() ?: 0) + 1).toString()
    newItem[1] = ((currentValues?.lastOrNull()?.get(1)?.toInt() ?: 0) + 1).toString()
    mutableDestinationMap?.add(newItem)
    /*Сдвиг общих порядковых номеров у следующих подразделений*/
    for (i in destinations.indexOf(currentDestination)..destinations.lastIndex) {
        val current = destinations[i]
        mutableMap[current] = mutableMap[current]!!.map {
            val mutable = it.toMutableList()
            mutable[0] = ((it[0].toIntOrNull() ?: -1) + 1).toString()
            mutable
        }
    }
    mutableMap[currentDestination] = mutableDestinationMap!!
    return mutableMap
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    destinations: List<String>,
    headers: Map<String, List<String>>,
    selectMode: Boolean,
    selectedMap: Map<String, List<RowIndex>>,
    valuesMutable: Map<String, List<List<String>>>,
    updateValues: (Map<String, List<List<String>>>) -> Unit,
    updateSelectedMap: (Map<String, List<RowIndex>>) -> Unit,
    addItem: (type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(selectMode) {
        if (!selectMode) {
            updateSelectedMap(emptyMap())
        }
    }
    NavHost(
        navController,
        startDestination = startDestination
    ) {
        destinations.forEach { destination ->
            composable(destination) {
                val currentValues by remember(valuesMutable, destination) {
                    mutableStateOf(valuesMutable[destination])
                }
                Column {
                    Table(
                        headers,
                        currentValues!!,
                        selectMode,
                        selectItem = {
                            val mutableSelectedMap = selectedMap.toMutableMap()
                            if (mutableSelectedMap[destination]?.contains(it) == true) {
                                /*Если есть в списке - удаляем*/
                                mutableSelectedMap[destination] = selectedMap[destination]?.minus(it) ?: emptyList()
                            } else {
                                /*Если нет - добавляем*/
                                mutableSelectedMap[destination] = selectedMap[destination]?.plus(it) ?: listOf(it)
                            }
                            updateSelectedMap(mutableSelectedMap)
                        },
                        selectedValues = selectedMap[destination] ?: emptyList()
                    ) {
                        /*Обновление значения*/
                        val mutableMap = valuesMutable.toMutableMap()
                        mutableMap[destination] = it
                        updateValues(mutableMap)
                    }
                    Spacer(Modifier.height(16.dp))
                    IconButton(
                        onClick = {
                            addItem(destination)
                            /*val newList = addItem(
                                valuesMutable = valuesMutable,
                                headers = headers,
                                currentValues = currentValues,
                                currentDestination = destination,
                                destinations = destinations
                            )
                            updateValues(newList)*/
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 16.dp)
                            .size(36.dp),
                        content = {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add"
                            )
                        }
                    )
                }

            }
        }
    }
}



@Preview(widthDp = 2000, showBackground = true)
@Composable
private fun HeadersPreview() {
    val headersData = mapOf(
        "№п/п" to emptyList<String>(),
        "№" to emptyList<String>(),
        "Позывной" to emptyList<String>(),
        "№ жетона" to emptyList<String>(),
        "Должность" to emptyList<String>(),
        "Группа" to emptyList<String>(),
        "Вооружение" to listOf("тип", "№", "тип", "№"),
        "Средства связи" to listOf("рст", "телефон"),
        "Группа крови" to emptyList<String>(),
        "Позиция" to emptyList<String>(),
    )
    val values: List<List<String>> = listOf(
        listOf(
            "1",
            "1",
            "Ленон",
            "В-77777123123123123123",
            "КВ",
            "Управление",
            "12345678901234567890",
            "7302118",
            "ПМ",
            "АА-1234",
            "771526480",
            "A256418JBK267",
            "О(I)-",
            "Фазенда"
        ),
        listOf(
            "1",
            "1",
            "Ленон",
            "В-77777",
            "КВ",
            "Управление",
            "АК",
            "7302118",
            "ПМ",
            "АА-1234",
            "771526480",
            "A256418JBK267",
            "О(I)-",
            "Фазенда"
        ),
        listOf(
            "1",
            "1",
            "Ленон",
            "В-77777",
            "КВ",
            "Управление",
            "АК",
            "7302118",
            "ПМ",
            "АА-1234",
            "771526480",
            "A256418JBK267",
            "О(I)-",
            "Фазенда"
        )
    )
/*
    Table(
        headersData = headersData,
        values = values,
        false,
        {}
    )*/
}