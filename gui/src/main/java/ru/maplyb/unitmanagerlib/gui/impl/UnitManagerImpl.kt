package ru.maplyb.unitmanagerlib.gui.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import ru.maplyb.unitmanagerlib.gui.api.UnitManager
import ru.maplyb.unitmanagerlib.parser.impl.FileParsingResult
import ru.maplyb.unitmanagerlib.parser.impl.convertToCsv
import ru.maplyb.unitmanagerlib.parser.impl.parseFile
import ru.maplyb.unitmanagerlib.parser.impl.parseLines
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

internal class UnitManagerImpl : UnitManager {

    private var activity: Activity? = null
//    private var database: UnitManagerDatabase? = null

    override fun init(activity: Activity) {
        this.activity = activity
//        database = Database.provideDatabase(activity)
    }

    @Composable
    override fun Show(uri: Uri?) {
        var lines by remember {
            mutableStateOf(listOf<String>())
        }
        LaunchedEffect(Unit) {
            val inputStream = activity?.contentResolver?.openInputStream(uri!!)
            val reader = BufferedReader(InputStreamReader(inputStream))
            buildList {
                reader.useLines { sequence ->
                    sequence.forEach { line ->
                        add(line)
                    }
                }
            }.also {
                lines = it
            }
        }
        val parsedFile by remember(lines) {
            mutableStateOf(parseLines(lines))
        }
        if (!parsedFile.isEmpty()) {
            ShowImpl(parsedFile)
        }
    }

    @Composable
    private fun ShowImpl(
        parsedFile: FileParsingResult
    ) {
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
            headersData = parsedFile.headers,
            values = parsedFile.values,
            share = {
                sharedData = it
                launcher.launch(null)
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