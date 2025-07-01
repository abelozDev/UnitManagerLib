package ru.maplyb.unitmanagerlib.gui.impl.presentation.table

import android.content.Context
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.maplyb.unitmanagerlib.common.database.Database
import ru.maplyb.unitmanagerlib.common.database.dao.HeaderDao.Companion.findPositionInDefaultHeaders
import ru.maplyb.unitmanagerlib.common.database.data_store.PreferencesDataSource
import ru.maplyb.unitmanagerlib.common.database.domain.DatabaseRepository
import ru.maplyb.unitmanagerlib.core.util.types.RowIndex
import ru.maplyb.unitmanagerlib.gui.api.model.Position
import ru.maplyb.unitmanagerlib.gui.impl.domain.mapper.toDTO
import ru.maplyb.unitmanagerlib.gui.impl.domain.mapper.toUI
import ru.maplyb.unitmanagerlib.parser.impl.FileParsingResult

internal sealed interface MainScreenAction {
    data class SelectItem(val item: Pair<String, RowIndex>) : MainScreenAction
    class DeleteItems : MainScreenAction
    data class MoveItems(val type: String) : MainScreenAction
    data class AddItem(val type: String) : MainScreenAction
    data class UpdateState(val state: MainScreenState) : MainScreenAction
    data class SetTableName(val context: Context, val tableName: String) : MainScreenAction
    data class UpdatePosition(val positionUI: Position) : MainScreenAction
    data class UpdateValues(
        val type: String,
        val rowIndex: Int,
        val columnIndex: Int,
        val newValue: String
    ) : MainScreenAction

    data class ShowOnMapClick(
        val type: String,
        val index: Int
    ) : MainScreenAction

    data class SetPositions(val positions: List<Position>): MainScreenAction
}

internal data class MainScreenUIState(
    val state: MainScreenState,
    val selectedMap: Map<String, List<RowIndex>>,
    val fileInfo: FileParsingResult?,
    val positions: List<Position>,
    val tableName: String
) {
    companion object {
        val default = MainScreenUIState(
            state = MainScreenState.Initial,
            selectedMap = emptyMap(),
            fileInfo = null,
            positions = emptyList(),
            tableName = ""
        )
    }
}

internal sealed interface MainScreenEffect {
    data class ShowMessage(val message: String) : MainScreenEffect
    data class ShowOnMap(val position: Position) : MainScreenEffect
}

internal sealed interface MainScreenState {
    sealed interface Select : MainScreenState {
        class Initial : Select
        class DeleteDialog : Select
        class MoveDialog : Select
        class SelectPosition : Select
    }

    data object Initial : MainScreenState
}

internal class MainScreenViewModel private constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenUIState.default)
    val state = _state.asStateFlow()

    private val _effect = Channel<MainScreenEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEffect(effect: MainScreenEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    fun onAction(action: MainScreenAction) {
        when (action) {
            is MainScreenAction.SelectItem -> {
                val mutableSelectedMap = _state.value.selectedMap.toMutableMap()
                val destination = action.item.first
                val index = action.item.second
                if (mutableSelectedMap[destination]?.contains(action.item.second) == true) {
                    // Если есть в списке - удаляем
                    mutableSelectedMap[destination] =
                        _state.value.selectedMap[destination]?.minus(index) ?: emptyList()
                } else {
                    //Если нет - добавляем
                    mutableSelectedMap[destination] =
                        _state.value.selectedMap[destination]?.plus(index) ?: listOf(index)
                }
                _state.update {
                    it.copy(
                        selectedMap = mutableSelectedMap
                    )
                }
            }

            is MainScreenAction.DeleteItems -> {
                deleteSelectedItems().invokeOnCompletion {
                    _state.update {
                        it.copy(
                            state = MainScreenState.Initial,
                            selectedMap = emptyMap()
                        )
                    }
                }

            }

            is MainScreenAction.MoveItems -> {
                moveSelectedItems(action.type).invokeOnCompletion {
                    _state.update {
                        it.copy(
                            state = MainScreenState.Initial,
                            selectedMap = emptyMap()
                        )
                    }
                }
            }

            is MainScreenAction.AddItem -> {
                addItem(action.type)
            }

            is MainScreenAction.UpdateState -> {
                when (action.state) {
                    is MainScreenState.Select.DeleteDialog,
                    is MainScreenState.Select.MoveDialog,
                    is MainScreenState.Select.SelectPosition -> {
                        if (_state.value.selectedMap.isEmpty()) {
                            onEffect(
                                MainScreenEffect.ShowMessage(
                                    message = "Сначала выберите записи"
                                )
                            )
                            return
                        }
                    }

                    MainScreenState.Initial -> Unit
                    is MainScreenState.Select.Initial -> Unit
                }
                _state.update {
                    it.copy(
                        state = action.state,
                        selectedMap = if (action.state is MainScreenState.Initial) emptyMap()
                        else _state.value.selectedMap
                    )
                }
            }

            is MainScreenAction.SetTableName -> {
                setLastTableName(action.context, action.tableName)
                _state.update {
                    it.copy(
                        tableName = action.tableName
                    )
                }
                startCollectDatabase()
            }

            is MainScreenAction.UpdateValues -> {
                updateValues(action.type, action.rowIndex, action.columnIndex, action.newValue)
            }

            is MainScreenAction.UpdatePosition -> {
                _state.value.selectedMap.forEach {
                    it.value.forEach { value ->
                        setPosition(
                            rowIndex = value,
                            type = it.key,
                            position = action.positionUI
                        )
                    }
                }
                _state.update {
                    it.copy(
                        state = MainScreenState.Initial,
                        selectedMap = emptyMap()
                    )
                }
            }

            is MainScreenAction.ShowOnMapClick -> {
                val item =
                    _state.value.fileInfo?.values?.get(action.type)?.get(action.index)
                if (item == null) {
                    onEffect(MainScreenEffect.ShowMessage("Элемент с типом ${action.type} и индексом ${action.index} не найден"))
                } else {
                    onEffect(
                        MainScreenEffect.ShowOnMap(getPosition(item))
                    )
                }

            }

            is MainScreenAction.SetPositions -> {
                _state.update {
                    it.copy(
                        positions = action.positions
                    )
                }
            }
        }
    }

    private fun setLastTableName(context: Context, tableName: String) = viewModelScope.launch {
        repository.setLastTable(context, tableName)
    }

    private fun getPosition(
        item: List<String>
    ): Position {
        val x = findPositionInDefaultHeaders("X")
            .firstOrNull()
            ?.let {
                item[it]
            }
            ?: error("header \"X\" not found")
        val y = findPositionInDefaultHeaders("Y")
            .firstOrNull()
            ?.let {
                item[it]
            }
            ?: error("header \"Y\" not found")
        val name = findPositionInDefaultHeaders("Название")
            .firstOrNull()
            ?.let {
                item[it]
            }
            ?: error("header \"Название\" not found")
        return Position(x.toDoubleOrNull() ?: 0.0, y.toDoubleOrNull() ?: 0.0, name)
    }

    private fun setPosition(
        rowIndex: Int,
        type: String,
        position: Position
    ) = viewModelScope.launch {
        repository.setPosition(_state.value.tableName, position.toDTO(), type, rowIndex)
    }

    private fun updateValues(
        type: String,
        rowIndex: Int,
        columnIndex: Int,
        newValue: String
    ) = viewModelScope.launch {
        repository.updateValues(_state.value.tableName, type, rowIndex, columnIndex, newValue)
    }

    private var getTableInfoJob: Job? = null

    private fun startCollectDatabase() {
        getTableInfoJob?.cancel()
        getTableInfoJob = viewModelScope.launch {
            repository
                .getTableInfoFlow(_state.value.tableName)
                .onEach { info ->
                    _state.update {
                        it.copy(
                            fileInfo = info?.toUI()
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun addItem(type: String) {
        viewModelScope.launch {
            repository.addNewItem(
                type = type,
                tableName = _state.value.tableName
            )
        }
    }

    private fun moveSelectedItems(type: String) = viewModelScope.launch {
        require(_state.value.fileInfo != null) {
            "file info must not be null"
        }
        val selectedItems =
            getSelectedItems(_state.value.selectedMap, _state.value.fileInfo!!.values)
        repository.moveItems(
            type = type,
            tableName = _state.value.tableName,
            items = selectedItems
        )
    }

    private fun deleteSelectedItems() = viewModelScope.launch {
        require(_state.value.fileInfo != null) {
            "file info must not be null"
        }
        val selectedItems =
            getSelectedItems(_state.value.selectedMap, _state.value.fileInfo!!.values)
        repository.deleteItems(
            tableName = _state.value.tableName,
            items = selectedItems
        )
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

    companion object {
        fun create(repository: DatabaseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainScreenViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}