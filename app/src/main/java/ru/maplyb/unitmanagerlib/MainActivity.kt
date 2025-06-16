package ru.maplyb.unitmanagerlib

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.net.toFile
import ru.maplyb.unitmanagerlib.gui.api.UnitManager
import ru.maplyb.unitmanagerlib.parser.impl.parseFile
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unitManager = UnitManager.create()
        unitManager.init(this)
        setContent {
            unitManager.TableHandler()
        }
    }

}


@Composable
fun MyTable(file: InputStream) {
    val parsingResult = parseFile(file)
    Column {
        TableHeader(parsingResult.headers, parsingResult.values)
    }
}

@Composable
fun TableHeader(headersData: Map<String, List<String>>, values: Map<String, List<List<String>>>) {
    /*MainScreen(
        headersData = headersData,
        values = values
    )*/

}

@Composable
private fun TableSegment(text: String) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .border(1.dp, Color.Gray)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

