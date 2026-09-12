package com.example.duofold.input

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.pointerFoldInput(
    source: PointerFoldPoseSource
): Modifier = pointerInput(source) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val position = event.changes.firstOrNull()?.position ?: continue

            if (size.width > 0 && size.height > 0) {
                source.updateFromNormalized(
                    x = position.x / size.width.toFloat(),
                    y = position.y / size.height.toFloat()
                )
            }
        }
    }
}
