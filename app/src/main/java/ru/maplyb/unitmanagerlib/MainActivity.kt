package ru.maplyb.unitmanagerlib

import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.maplyb.unitmanagerlib.parser.impl.parseFile
import java.io.File
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyTable(this@MainActivity.assets.open("штат.csv"))
        }
    }
}

@Composable
fun MyTable(file: InputStream) {
    val headersData = parseFile(file)
    Column {
        TableHeader(headersData)
    }
}

@Composable
fun TableHeader(headersData: Map<String, List<String>>) {
    val scrollState = rememberScrollState()
    var rowWidth by remember { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth()
    ) {
        headersData.forEach { (mainHeader, subHeaders) ->
            Column(
                modifier = Modifier.wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mainHeader,
                    modifier = Modifier
                        .width(with(LocalDensity.current) { rowWidth.toDp() })
                        .border(1.dp, Color.Black)
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                if (subHeaders.isNotEmpty()) {
                    Row(
                        Modifier.onGloballyPositioned {
                            rowWidth = it.size.width
                        }
                    ) {
                        subHeaders.forEach { sub ->
                            Text(
                                text = sub,
                                modifier = Modifier
                                    .border(1.dp, Color.Black)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
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

