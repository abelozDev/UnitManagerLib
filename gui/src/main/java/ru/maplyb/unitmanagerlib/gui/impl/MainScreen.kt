package ru.maplyb.unitmanagerlib.gui.impl

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.maplyb.unitmanagerlib.core.util.types.RowIndex
import ru.maplyb.unitmanagerlib.gui.impl.table.Table
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
    var selectMode by remember {
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
                navController,
                startDestination,
                destinations,
                headersData,
                values,
                selectMode
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    destinations: List<String>,
    headers: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    selectMode: Boolean,
    modifier: Modifier = Modifier
) {
    var valuesMutable by remember {
        mutableStateOf(values)
    }
    var selectedMap by remember {
        mutableStateOf(mapOf<String, List<RowIndex>>())
    }
    LaunchedEffect(selectMode) {
        if (!selectMode) {
            selectedMap = emptyMap()
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
                            selectedMap = mutableSelectedMap
                        },
                        selectedValues = selectedMap[destination] ?: emptyList()
                    ) {
                        /*Обновление значения*/
                        val mutableMap = valuesMutable.toMutableMap()
                        mutableMap[destination] = it
                        valuesMutable = mutableMap
                    }
                    Spacer(Modifier.height(16.dp))
                    IconButton(
                        onClick = {
                            val mutableMap = valuesMutable.toMutableMap()
                            val mutableDestinationMap = mutableMap[destination]?.toMutableList()
                            val size = headers.values.sumOf { it.size } + headers.size
                            /*Размер списка для нового элемента*/
                            val newItem = MutableList(size) { "" }
                            /*Порядковый номер общий и в подразделении*/
                            newItem[0] =
                                ((currentValues?.last()?.get(0)?.toInt() ?: 0) + 1).toString()
                            newItem[1] =
                                ((currentValues?.last()?.get(1)?.toInt() ?: 0) + 1).toString()
                            mutableDestinationMap?.add(newItem)
                            /*Сдвиг общих порядковых номеров у следующих подразделений*/
                            for (i in destinations.indexOf(destination)..destinations.lastIndex) {
                                val current = destinations[i]
                                mutableMap[current] = mutableMap[current]!!.map {
                                    val mutable = it.toMutableList()
                                    mutable[0] = ((it[0].toIntOrNull() ?: -1) + 1).toString()
                                    mutable
                                }
                            }
                            mutableMap[destination] = mutableDestinationMap!!
                            valuesMutable = mutableMap
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