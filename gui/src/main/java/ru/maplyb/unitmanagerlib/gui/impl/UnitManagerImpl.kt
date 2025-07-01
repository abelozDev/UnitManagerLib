package ru.maplyb.unitmanagerlib.gui.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.buildLongList
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.maplyb.unitmanagerlib.common.database.Database
import ru.maplyb.unitmanagerlib.common.database.data_store.PreferencesDataSource
import ru.maplyb.unitmanagerlib.common.database.domain.DatabaseRepository
import ru.maplyb.unitmanagerlib.core.ui_kit.LocalColorScheme
import ru.maplyb.unitmanagerlib.core.ui_kit.darkColorSchema
import ru.maplyb.unitmanagerlib.core.ui_kit.lightColorSchema
import ru.maplyb.unitmanagerlib.gui.api.ShowOnMapClickListener
import ru.maplyb.unitmanagerlib.gui.api.UnitManager
import ru.maplyb.unitmanagerlib.gui.api.model.Position
import ru.maplyb.unitmanagerlib.gui.impl.domain.mapper.toDTO
import ru.maplyb.unitmanagerlib.gui.impl.domain.mapper.toUI
import ru.maplyb.unitmanagerlib.gui.impl.presentation.home.HomeScreen
import ru.maplyb.unitmanagerlib.gui.impl.presentation.home.components.CreateTableDialog
import ru.maplyb.unitmanagerlib.gui.impl.presentation.table.MainScreen
import ru.maplyb.unitmanagerlib.gui.impl.presentation.table.MainScreenAction
import ru.maplyb.unitmanagerlib.gui.impl.presentation.table.MainScreenEffect
import ru.maplyb.unitmanagerlib.gui.impl.presentation.table.MainScreenUIState
import ru.maplyb.unitmanagerlib.gui.impl.presentation.table.MainScreenViewModel
import ru.maplyb.unitmanagerlib.parser.impl.parseLines
import java.io.BufferedReader
import java.io.InputStreamReader

internal class UnitManagerImpl : UnitManager {

    private var showOnMapClickListener: ShowOnMapClickListener? = null

    private var activity: Context? = null
    private lateinit var repository: DatabaseRepository
    private var _tableName = MutableStateFlow<String?>(null)
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val positions = MutableStateFlow<List<Position>>(emptyList())

    override fun setShowOnMapClickListener(listener: ShowOnMapClickListener) {
        showOnMapClickListener = listener
    }

    override fun init(activity: Activity) {
        this.activity = activity.applicationContext
        repository = DatabaseRepository.create(
            Database.provideDatabase(activity),
            PreferencesDataSource.create()
        )
        scope.launch {
            _tableName.value = repository.getLastTable(activity)
        }
    }


    @Composable
    override fun TableHandler() {
        var tableName by rememberSaveable {
            mutableStateOf<String?>(_tableName.value)
        }
        val scope = rememberCoroutineScope()
        var launchSelector by remember {
            mutableStateOf(false)
        }
        var showCreateDialog by rememberSaveable {
            mutableStateOf(false)
        }
        val allTablesNames by repository.getAllTablesNames().collectAsState(emptyList())
        BackHandler {
            if (tableName != null) {
                scope.launch {
                    tableName = null
                    repository.removeLastTable(activity!!)
                }
            }
        }
        if (launchSelector) {
            SelectFile { uri ->
                val inputStream = activity?.contentResolver?.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                buildList {
                    reader.useLines { sequence ->
                        sequence.forEach { line ->
                            add(line)
                        }
                    }
                }.also {
                    val parsedTable = parseLines(it)
                    scope.launch {
                        val table = repository.insertHeadersAndValues(
                            tableName = getFileName(activity!!, uri)!!,
                            headers = parsedTable.headers,
                            values = parsedTable.values
                        ).toUI()
                        tableName = table.tableName
                    }
                }
                launchSelector = false
            }
        }
        val colors = if (!isSystemInDarkTheme()) lightColorSchema() else darkColorSchema()
        CompositionLocalProvider(LocalColorScheme provides colors) {
            if (tableName == null) {
                HomeScreen(
                    allHeaderNames = allTablesNames,
                    selectFile = {
                        tableName = it
                    },
                    openNew = {
                        launchSelector = true
                    },
                    createNew = {
                        showCreateDialog = !showCreateDialog
                    },
                    deleteTable = {
                        scope.launch {
                            repository.deleteTable(it)
                        }
                    }
                )
            } else {
                tableName.let {
                    Show(it!!) {
                        tableName = null
                    }
                }
            }
            if (showCreateDialog) {
                CreateTableDialog(
                    onDismissRequest = {
                        showCreateDialog = !showCreateDialog
                    },
                    confirm = {
                        scope.launch {
                            repository.createNew(it)
                            showCreateDialog = !showCreateDialog
                        }
                    }
                )
            }
        }
    }


    override fun updatePositions(positions: List<Position>) {
        this.positions.value = positions
    }

    @Composable
    private fun SelectFile(
        onUriTaken: (Uri) -> Unit
    ) {
        val activity = requireNotNull(activity) { "Необходима инициализация" }
        var uri: Uri? by remember {
            mutableStateOf(null)
        }
        val contract = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), {
            if (it != null) {
                activity.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                uri = it
            }
        })
        LaunchedEffect(Unit) {
            contract.launch(arrayOf("*/*"))
        }
        if (uri != null) {
            val inputStream = activity.contentResolver.openInputStream(uri!!)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val lines = mutableListOf<String>()
            reader.useLines { sequence ->
                sequence.forEach { line ->
                    lines.add(line)
                }
            }
            onUriTaken(uri!!)
        }
    }

    @Composable
    private fun Show(
        tableName: String,
        back: () -> Unit
    ) {
        val mainViewModelStore = remember { ViewModelStore() }
        val mainViewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = mainViewModelStore
            }
        }
        val viewModel = ViewModelProvider(
            owner = mainViewModelStoreOwner,
            factory = MainScreenViewModel.create(repository)
        )[MainScreenViewModel::class.java]
        val state by viewModel.state.collectAsState()
        val context = LocalContext.current
        DisposableEffect(Unit) {
            onDispose {
                mainViewModelStore.clear()
            }
        }
        LaunchedEffect(Unit) {
            positions
                .onEach {
                    viewModel.onAction(MainScreenAction.SetPositions(it))
                }
                .launchIn(this)
        }
        LaunchedEffect(tableName) {
            viewModel.onAction(MainScreenAction.SetTableName(context, tableName))
        }
        LaunchedEffect(Unit) {
            viewModel
                .effect
                .onEach {
                    when (it) {
                        is MainScreenEffect.ShowMessage -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }

                        is MainScreenEffect.ShowOnMap -> {
                            showOnMapClickListener?.onClick(it.position)
                        }

                        is MainScreenEffect.Back -> {
                            back()
                        }
                    }

                }
                .launchIn(this)
        }
        if (state.fileInfo != null) {
            ShowImpl(
                uiState = state,
                onAction = { action ->
                    viewModel.onAction(action)
                },
                onEffect = {
                    viewModel.onEffect(it)
                }
            )
        }
    }

    fun getFileName(activity: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    @Composable
    private fun ShowImpl(
        uiState: MainScreenUIState,
        onAction: (MainScreenAction) -> Unit,
        onEffect: (MainScreenEffect) -> Unit
    ) {
        require(uiState.fileInfo != null) {
            "file info must not be null"
        }
        val context = LocalContext.current
        var sharedData by remember {
            mutableStateOf<List<String>>(emptyList())
        }
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri != null) {
                    // Даем доступ на долгое время
                    activity?.contentResolver?.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    activity?.saveCsvToFolder(uri, "my_file.csv", sharedData)
                }
            }
        MainScreen(
            uiState = uiState,
            onAction = onAction,
            share = {
                sharedData = it
                launcher.launch(null)
            },
            addItem = { type ->
                onAction(MainScreenAction.AddItem(type))
            },
            moveItem = { type, items ->
                onAction(MainScreenAction.MoveItems(type))
            },
            deleteItems = { items ->
                onAction(MainScreenAction.DeleteItems())
            },
            showOnMap = { type, index ->
                onAction(MainScreenAction.ShowOnMapClick(type, index))
            },
            back = {
                onAction(MainScreenAction.Back(context))
            }
        )
    }

    private fun Context.saveCsvToFolder(folderUri: Uri, name: String, lines: List<String>) {
        val folderDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
            folderUri,
            DocumentsContract.getTreeDocumentId(folderUri)
        )
        val docUri = DocumentsContract.createDocument(
            contentResolver,
            folderDocumentUri,
            "text/csv",
            name
        )

        docUri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    lines.forEach { line ->
                        writer.write(line)
                        writer.newLine()
                    }
                }
            }
        }
    }
}