package ru.maplyb.unitmanagerlib.gui.impl.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.maplyb.unitmanagerlib.core.ui_kit.DefaultVerticalSpacer
import ru.maplyb.unitmanagerlib.core.ui_kit.PrintMapColorSchema
import ru.maplyb.unitmanagerlib.core.ui_kit.UnitManagerFab

@Composable
internal fun HomeScreen(
    allHeaderNames: List<String> = emptyList(),
    selectFile: (name: String) -> Unit = {},
    openNew: () -> Unit = {},
    createNew: () -> Unit = {},
    deleteTable: (name: String) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrintMapColorSchema.colors.backgroundColor)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Недавние файлы:",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                color = PrintMapColorSchema.colors.textColor,
                fontWeight = FontWeight.Bold
            )
            DefaultVerticalSpacer(8)
            allHeaderNames.forEach {
                Row(
                    modifier = Modifier.clickable {
                        selectFile(it)

                    }
                ) {
                    Text(
                        text = it,
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        fontSize = 16.sp,
                        color = PrintMapColorSchema.colors.textColor,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        content = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = PrintMapColorSchema.colors.textColor
                            )
                        },
                        onClick = {
                            deleteTable(it)
                        }
                    )
                }
            }
        }
        var visibility by remember {
            mutableStateOf(false)
        }
        if (visibility) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        visibility = false
                    }
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
        ) {

            AnimatedVisibility(
                visible = visibility,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(
                    targetAlpha = 1f
                )
            ) {
                Column(
                    modifier = Modifier
                        .clip(FloatingActionButtonDefaults.shape)
                        .background(PrintMapColorSchema.colors.buttonBackgroundColor)
                        .padding(8.dp)
                ) {
                    FabActions.entries.forEach {
                        Text(
                            modifier = Modifier
                                .clickable {
                                    when(it) {
                                        FabActions.OPEN -> {
                                            openNew()
                                        }
                                        FabActions.CREATE -> {
                                            createNew()
                                        }
                                    }
                                    visibility = false
                                }
                                .padding(12.dp),
                            text = it.description,
                            color = PrintMapColorSchema.colors.textColor
                        )
                    }
                }
            }
            DefaultVerticalSpacer(height = 8)
            UnitManagerFab(
                onClick = {
                    visibility = !visibility
                },
                content = {
                    Icon(
                        imageVector = if (visibility) Icons.Default.Close else Icons.Default.MoreVert,
                        contentDescription = null
                    )
                }
            )
        }


        /*Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                openNew()
            },
            colors = unitManagerButtonColors(),
            content = {
                Text("Открыть новый файл")
            }
        )*/
    }
}

private enum class FabActions(val description: String) {
    OPEN("Открыть файл"), CREATE("Создать файл")
}

@Preview
@Composable
private fun PreviewHomeScreen() {
    HomeScreen(listOf("asdasd", "asdasdasd", "asdasdasdasd", "asdasdasdasdasd", "asdasdasdasdasd"))
}