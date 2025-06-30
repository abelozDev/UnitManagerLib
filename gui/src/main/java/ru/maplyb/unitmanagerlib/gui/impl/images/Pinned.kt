package ru.maplyb.unitmanagerlib.gui.impl.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.Unit

@Composable
fun pinned(
    color: Color
): ImageVector {
        return Builder(name = "Pinned", defaultWidth = 800.0.dp, defaultHeight = 800.0.dp,
                viewportWidth = 52.0f, viewportHeight = 52.0f).apply {
            path(fill = SolidColor(color), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(36.9f, 23.7f)
                horizontalLineToRelative(-0.5f)
                lineTo(33.0f, 7.9f)
                horizontalLineToRelative(0.9f)
                curveToRelative(1.6f, 0.0f, 2.9f, -1.3f, 2.9f, -2.9f)
                reflectiveCurveToRelative(-1.3f, -2.9f, -2.9f, -2.9f)
                horizontalLineTo(18.1f)
                curveToRelative(-1.6f, 0.0f, -2.9f, 1.3f, -2.9f, 2.9f)
                reflectiveCurveToRelative(1.3f, 2.9f, 2.9f, 2.9f)
                horizontalLineTo(19.0f)
                lineToRelative(-3.3f, 15.8f)
                horizontalLineToRelative(-0.5f)
                curveToRelative(-1.6f, 0.0f, -2.9f, 1.3f, -2.9f, 2.9f)
                reflectiveCurveToRelative(1.3f, 2.9f, 2.9f, 2.9f)
                horizontalLineToRelative(8.4f)
                verticalLineToRelative(17.4f)
                curveToRelative(0.0f, 1.6f, 1.3f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.3f, 3.0f, -3.0f)
                verticalLineTo(29.6f)
                horizontalLineToRelative(7.4f)
                curveToRelative(1.6f, 0.0f, 2.9f, -1.3f, 2.9f, -2.9f)
                reflectiveCurveTo(38.5f, 23.7f, 36.9f, 23.7f)
                close()
            }
        }
        .build()
    }


@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = pinned(Color(0xff212121)), contentDescription = "")
    }
}
