package ru.maplyb.unitmanagerlib.gui.impl.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
internal fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Принять",
    declineText: String = "Отклонить",
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        modifier = Modifier.clickable {
                            onDismissRequest()
                        },
                        text = declineText,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        modifier = Modifier.clickable {
                            onConfirm()
                        },
                        text = confirmText,
                        color = Color(0xff1277F3)
                    )
                }

            }
        }
    )
}


@Composable
@Preview
internal fun PreviewConfirmDialog() {
    ConfirmDialog(
        title = "title",
        message = "message",
        onConfirm = {},
        onDismissRequest = {}
    )
}