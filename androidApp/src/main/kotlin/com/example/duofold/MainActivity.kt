package com.example.duofold

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.compose.runtime.remember
import com.example.duofold.input.AdaptiveAndroidPoseSource
import com.example.duofold.model.DeviceOpticalProfile
import com.example.duofold.ui.FoldDemoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val poseSource = remember {
                AdaptiveAndroidPoseSource(applicationContext)
            }

            LifecycleStartEffect(poseSource) {
                poseSource.start()
                onStopOrDispose {
                    poseSource.stop()
                }
            }

            val initialProfile =
                if (resources.configuration.smallestScreenWidthDp >= 600) {
                    DeviceOpticalProfile.TABLET
                } else {
                    DeviceOpticalProfile.PHONE
                }

            FoldDemoApp(
                poseSource = poseSource,
                platformLabel = "Android",
                initialProfile = initialProfile
            )
        }
    }
}
