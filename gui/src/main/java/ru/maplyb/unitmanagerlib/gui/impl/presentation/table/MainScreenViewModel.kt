package ru.maplyb.unitmanagerlib.gui.impl.presentation.table

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.maplyb.unitmanagerlib.common.database.Database
import ru.maplyb.unitmanagerlib.common.database.domain.DatabaseRepository
import ru.maplyb.unitmanagerlib.core.util.types.RowIndex
import ru.maplyb.unitmanagerlib.gui.impl.domain.mapper.toUI
import ru.maplyb.unitmanagerlib.parser.impl.FileParsingResult

internal sealed interface MainScreenAction {
    data class SelectItem(val item: Pair<String, RowIndex>) : MainScreenAction
    class DeleteItems : MainScreenAction
    data class MoveItems(val type: String) : MainScreenAction
    data class AddItem(val type: String) : MainScreenAction
    data class UpdateState(val state: MainScreenState) : MainScreenAction
    data class SetTableName(val tableName: String) : MainScreenAction
    data class UpdateValues(val type: String, val rowIndex: Int, val columnIndex: Int, val newValue: String):
        MainScreenAction
}

internal data class MainScreenUIState(
    val state: MainScreenState = MainScreenState.Initial,
    val selectedMap: Map<String, List<RowIndex>>,
    val fileInfo: FileParsingResult? = null,
    val tableName: String
) {
    companion object {
        val default = MainScreenUIState(
            selectedMap = emptyMap(),
            tableName = ""
        )
    }
}

internal sealed interface MainScreenState {
    sealed interface Select : MainScreenState {
        class Initial: Select
        class DeleteDialog : Select
        class MoveDialog : Select
    }

    data object Initial : MainScreenState
}

internal class MainScreenViewModel private constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenUIState.default)
    val state = _state.asStateFlow()

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
                _state.update {
                    it.copy(
                        state = action.state,
                        selectedMap = if (action.state is MainScreenState.Initial) emptyMap()
                        else _state.value.selectedMap
                    )
                }
            }

            is MainScreenAction.SetTableName -> {
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
        }
    }

    private fun updateValues(
        type: String,
        rowIndex: Int,
        columnIndex: Int,
        newValue: String
    ) = viewModelScope.launch {
        repository.updateValues(type, rowIndex, columnIndex, newValue)
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
        val selectedItems = getSelectedItems(_state.value.selectedMap, _state.value.fileInfo!!.values)
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
        val selectedItems = getSelectedItems(_state.value.selectedMap, _state.value.fileInfo!!.values)
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
        fun create(activity: Activity): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
                        val repository =
                            DatabaseRepository.create(Database.provideDatabase(activity))
                        @Suppress("UNCHECKED_CAST")
                        return MainScreenViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}