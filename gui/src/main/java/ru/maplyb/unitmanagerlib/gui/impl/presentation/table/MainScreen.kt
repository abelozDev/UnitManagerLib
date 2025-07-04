package ru.maplyb.unitmanagerlib.gui.impl.presentation.table

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.defaultUnitManagerTableHeaders
import ru.maplyb.unitmanagerlib.core.ui_kit.LocalColorScheme
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema
import ru.maplyb.unitmanagerlib.core.ui_kit.darkColorSchema
import ru.maplyb.unitmanagerlib.core.ui_kit.lightColorSchema
import ru.maplyb.unitmanagerlib.core.util.types.RowIndex
import ru.maplyb.unitmanagerlib.gui.R
import ru.maplyb.unitmanagerlib.gui.impl.components.dialogs.ConfirmDialog
import ru.maplyb.unitmanagerlib.gui.impl.components.dialogs.MoveDialog
import ru.maplyb.unitmanagerlib.gui.impl.components.dialogs.SelectPositionDialog
import ru.maplyb.unitmanagerlib.gui.impl.images.pinned
import ru.maplyb.unitmanagerlib.gui.impl.table.Table
import ru.maplyb.unitmanagerlib.parser.impl.convertToCsv

@Composable
internal fun MainScreen(
    uiState: MainScreenUIState,
    share: (List<String>) -> Unit,
    addItem: (type: String) -> Unit,
    moveItem: (header: String, items: List<List<String>>) -> Unit,
    deleteItems: (List<List<String>>) -> Unit,
    onAction: (MainScreenAction) -> Unit,
    showOnMap: (String, Int) -> Unit,
    back: () -> Unit,
) {

    NavigationTabExample(
        uiState = uiState,
        state = uiState.state,
        share = share,
        moveItem = moveItem,
        addItem = addItem,
        deleteItems = deleteItems,
        onAction = onAction,
        showOnMap = showOnMap,
        back = back
    )
}

@Composable
internal fun NavigationTabExample(
    uiState: MainScreenUIState,
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit,
    share: (List<String>) -> Unit,
    moveItem: (header: String, items: List<List<String>>) -> Unit,
    addItem: (type: String) -> Unit,
    deleteItems: (List<List<String>>) -> Unit,
    showOnMap: (String, Int) -> Unit,
    back: () -> Unit,
    modifier: Modifier = Modifier
) {
    require(uiState.fileInfo != null) {
        "file info must not be null"
    }
    val navController = rememberNavController()
    val startDestination = uiState.fileInfo.valueTypes.first()
    var selectedDestination by rememberSaveable {
        mutableIntStateOf(
            uiState.fileInfo.valueTypes.indexOf(
                startDestination
            )
        )
    }

    Scaffold(modifier = modifier.background(PrintMapColorSchema.colors.backgroundColor)) { contentPadding ->
        Column(
            modifier = Modifier.background(PrintMapColorSchema.colors.backgroundColor)
        ) {
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = PrintMapColorSchema.colors.textColor
                        )
                    },
                    onClick = {
                        back()
                    }
                )
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = PrintMapColorSchema.colors.textColor
                        )
                    },
                    onClick = {
                        val list = convertToCsv(uiState.fileInfo.headers, uiState.fileInfo.values)
                        share(list)
                    }
                )
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (state is MainScreenState.Select) Color.Green else PrintMapColorSchema.colors.textColor
                        )
                    },
                    onClick = {
                        if (state is MainScreenState.Select) {
                            onAction(MainScreenAction.UpdateState(MainScreenState.Initial))
                        } else {
                            onAction(MainScreenAction.UpdateState(MainScreenState.Select.Initial()))
                        }
                    }
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = state is MainScreenState.Select,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Row {
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = PrintMapColorSchema.colors.textColor
                                )
                            },
                            onClick = {
                                onAction(MainScreenAction.UpdateState(MainScreenState.Select.DeleteDialog()))
                            }
                        )
                        IconButton(
                            content = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_move),
                                    contentDescription = null,
                                    tint = PrintMapColorSchema.colors.textColor
                                )
                            },
                            onClick = {
                                onAction(MainScreenAction.UpdateState(MainScreenState.Select.MoveDialog()))
                            }
                        )
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = pinned(PrintMapColorSchema.colors.textColor),
                                    modifier = Modifier.size(24.dp),
                                    contentDescription = null,
                                    tint = PrintMapColorSchema.colors.textColor
                                )
                            },
                            onClick = {
                                onAction(MainScreenAction.UpdateState(MainScreenState.Select.SelectPosition()))
                            }
                        )
                    }
                }
            }
            ScrollableTabRow(
                selectedTabIndex = selectedDestination,
                modifier = Modifier
                    .background(PrintMapColorSchema.colors.backgroundColor)
                    .padding(contentPadding),
                indicator =
                    @Composable { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDestination]),
                            color = PrintMapColorSchema.colors.primary
                        )
                    },
                containerColor = PrintMapColorSchema.colors.backgroundColor,
                edgePadding = 16.dp
            ) {
                uiState.fileInfo.valueTypes.forEachIndexed { index, destination ->
                    Tab(
                        selected = selectedDestination == index,
                        modifier = Modifier.background(PrintMapColorSchema.colors.backgroundColor),
                        onClick = {
                            navController.navigate(route = destination) {
                                popUpTo(0)
                            }
                            selectedDestination = index
                        },
                        text = {
                            Text(
                                color = PrintMapColorSchema.colors.textColor,
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
                destinations = uiState.fileInfo.valueTypes,
                headers = uiState.fileInfo.headers,
                valuesMutable = uiState.fileInfo.values,
                selectMode = state is MainScreenState.Select,
                selectedMap = uiState.selectedMap,
                updateSelectedMap = {
                    onAction(MainScreenAction.SelectItem(it))
                },
                addItem = addItem,
                updateValues = { type, rowindex, ColumntIndex, newValue ->
                    onAction(
                        MainScreenAction.UpdateValues(
                            type,
                            rowindex,
                            ColumntIndex,
                            newValue
                        )
                    )
                },
                showOnMap = showOnMap
            )
        }
    }
    when (state) {
        is MainScreenState.Select.DeleteDialog -> {
            ConfirmDialog(
                title = "Удаление",
                message = "Вы уверены, что хотите удалить элементы из списка?",
                onConfirm = {
                    val listToDelete =
                        getSelectedItems(uiState.selectedMap, uiState.fileInfo.values)
                    deleteItems(listToDelete)
                },
                onDismissRequest = {
                    onAction(MainScreenAction.UpdateState(MainScreenState.Select.Initial()))
                }
            )
        }

        is MainScreenState.Select.MoveDialog -> {
            MoveDialog(
                title = "Перемещение элементов",
                message = "Выберите, куда переместить элементы:",
                items = uiState.fileInfo.valueTypes,
                onDismissRequest = {
                    onAction(MainScreenAction.UpdateState(MainScreenState.Select.Initial()))
                },
                select = { header ->
                    val listToAdd = getSelectedItems(uiState.selectedMap, uiState.fileInfo.values)
                    moveItem(header, listToAdd)
                }
            )
        }

        is MainScreenState.Select.SelectPosition -> {
            SelectPositionDialog(
                uiState.positions,
                onDismissRequest = {
                    onAction(MainScreenAction.UpdateState(MainScreenState.Select.Initial()))
                },
                select = { position ->
                    onAction(
                        MainScreenAction.UpdatePosition(position)
                    )
                }
            )
        }

        else -> Unit
    }

}

private fun getSelectedItems(
    selectedMap: Map<String, List<RowIndex>>,
    values: Map<String, List<List<String>>>
): List<List<String>> {
    return buildList {
        selectedMap.entries.forEach { (key, value) ->
            values[key]?.forEachIndexed { index, list ->
                if (value.contains(index)) add(list)
            }
        }
    }
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
    updateValues: (
        type: String,
        rowIndex: Int,
        columnIndex: Int,
        newValue: String
    ) -> Unit,
    updateSelectedMap: (Pair<String, RowIndex>) -> Unit,
    addItem: (type: String) -> Unit,
    showOnMap: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        destinations.forEach { destination ->
            composable(destination) {
                val currentValues by remember(valuesMutable, destination) {
                    mutableStateOf(valuesMutable[destination] ?: emptyList())
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(PrintMapColorSchema.colors.backgroundColor)
                ) {
                    Table(
                        headersData = headers,
                        values = currentValues,
                        selectMode = selectMode,
                        selectItem = {
                            updateSelectedMap(Pair(destination, it))
                        },
                        selectedValues = selectedMap[destination] ?: emptyList(),
                        updateValues = { rowIndex, columnIndex, newValue ->
                            updateValues(
                                destination,
                                rowIndex,
                                columnIndex,
                                newValue
                            )
                        },
                        showOnMap = {
                            showOnMap(destination, it)
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    IconButton(
                        onClick = {
                            addItem(destination)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 24.dp)
                            .size(36.dp),
                        content = {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add",
                                tint = PrintMapColorSchema.colors.textColor
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
        "№п/п" to emptyList(),
        "№" to emptyList(),
        "Позывной" to emptyList(),
        "№ жетона" to emptyList(),
        "Должность" to emptyList(),
        "Группа" to emptyList(),
        "Вооружение" to listOf("тип", "№", "тип", "№"),
        "Средства связи" to listOf("рст", "телефон"),
        "Группа крови" to emptyList(),
        "Позиция" to emptyList(),
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
        Table(
            headersData = defaultUnitManagerTableHeaders,
            values = values,
            false,
            {}
        )
}