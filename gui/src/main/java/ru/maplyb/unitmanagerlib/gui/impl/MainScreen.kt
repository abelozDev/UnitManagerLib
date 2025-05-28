package ru.maplyb.unitmanagerlib.gui.impl

import androidx.collection.mutableIntListOf
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(headersData: Map<String, List<String>>, values: Map<String, List<List<String>>>) {
    NavigationTabExample(headersData, values)
}

@Composable
fun NavigationTabExample(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    destinations: List<String>,
    headers: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination
    ) {
        destinations.forEach { destination ->
            composable(destination) {
                when (destination) {
                    else -> {
                        Headers(headers, values)
                    }
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
    val values: Map<String, List<List<String>>> = mapOf(
        "Управление взвода" to listOf(
            listOf("1", "1", "Ленон", "В-77777", "КВ", "Управление", "АК", "7302118", "ПМ", "АА-1234", "771526480", "A256418JBK267", "О(I)-", "Фазенда"),
            listOf("1", "1", "Ленон", "В-77777", "КВ", "Управление", "АК", "7302118", "ПМ", "АА-1234", "771526480", "A256418JBK267", "О(I)-", "Фазенда"),
            listOf("1", "1", "Ленон", "В-77777", "КВ", "Управление", "АК", "7302118", "ПМ", "АА-1234", "771526480", "A256418JBK267", "О(I)-", "Фазенда")
        )
    )
    Headers(
        headersData = headersData,
        values = values
        )
}

@Composable
private fun Headers(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>
) {
    val scrollState = rememberScrollState()
    var rowWidth by remember { mutableStateOf(0) }
    var currentHeaderIndex by remember { mutableIntStateOf(0) }
    val currentValues = values["Управление взвода"]
    var currentValuesIndex by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth()
    ) {
        headersData.forEach { (mainHeader, subHeaders) ->
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .border(1.dp, Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mainHeader,
                    modifier = Modifier
                        .padding(4.dp)
                        ,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                if (subHeaders.isNotEmpty()) {
                    Row(
                        Modifier.onGloballyPositioned {
                            rowWidth = it.size.width
                        }
                    ) {
                        subHeaders.forEachIndexed { index, sub ->
                            Column {
                                Text(
                                    text = sub,
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .weight(1f)
                                        .border(1.dp, Color.Black)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                                currentValues?.getValuesByIndex(currentValuesIndex)?.forEach {
                                    Text(
                                        text = it,
                                        modifier = Modifier
                                            .border(1.dp, Color.Black)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            currentValuesIndex++
                        }
                    }
                } else {
                    Column {
                        currentValues?.getValuesByIndex(currentValuesIndex)?.forEach {
                            Text(
                                text = it,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Black)
                                    .padding(horizontal = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    currentValuesIndex++
                }
            }
            currentHeaderIndex++
        }
    }
}

fun List<List<String>>.getValuesByIndex(index: Int): List<String> {
    return map { it.getOrNull(index) ?: "" }
}