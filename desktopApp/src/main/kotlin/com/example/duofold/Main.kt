package com.example.duofold

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.duofold.input.PointerFoldPoseSource
import com.example.duofold.input.pointerFoldInput
import com.example.duofold.model.DeviceOpticalProfile
import com.example.duofold.ui.FoldDemoApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DuoFold Universal"
    ) {
        val poseSource = remember {
            PointerFoldPoseSource()
        }

        FoldDemoApp(
            poseSource = poseSource,
            interactionModifier = Modifier.pointerFoldInput(poseSource),
            platformLabel = "Desktop",
            initialProfile = DeviceOpticalProfile.LAPTOP
        )
    }
}
