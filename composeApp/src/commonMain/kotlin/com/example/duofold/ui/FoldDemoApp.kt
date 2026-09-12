package com.example.duofold.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.example.duofold.input.FoldPoseSource
import com.example.duofold.model.DeviceOpticalProfile
import com.example.duofold.model.DisplayCalibration
import com.example.duofold.model.FoldPanels
import com.example.duofold.model.FoldParameters
import com.example.duofold.model.FoldPreset
import com.example.duofold.model.FoldQuality
import com.example.duofold.model.resolve
import com.example.duofold.render.FoldRenderState
import com.example.duofold.render.foldSurfaceEffect

@Composable
fun FoldDemoApp(
    poseSource: FoldPoseSource,
    interactionModifier: Modifier = Modifier,
    platformLabel: String,
    initialProfile: DeviceOpticalProfile = DeviceOpticalProfile.PHONE
) {
    val livePose by poseSource.pose.collectAsState()
    val inputAvailable by poseSource.available.collectAsState()

    var useLiveInput by remember(inputAvailable) { mutableStateOf(inputAvailable) }
    var followInputGeometry by remember { mutableStateOf(true) }
    var manualAngle by remember { mutableFloatStateOf(0f) }
    var selectedPreset by remember { mutableStateOf(FoldPreset.CENTER_VERTICAL) }
    var quality by remember { mutableStateOf(FoldQuality.AUTO) }
    var profile by remember(initialProfile) { mutableStateOf(initialProfile) }
    var useManualDensity by remember { mutableStateOf(false) }
    var manualPxPerMm by remember { mutableFloatStateOf(10f) }
    var useManualEyeDistance by remember { mutableStateOf(false) }
    var manualEyeDistance by remember { mutableFloatStateOf(500f) }

    val live = useLiveInput && inputAvailable
    val angle = if (live) livePose.angleDegrees else manualAngle
    val effectivePreset = if (live && followInputGeometry) {
        livePose.suggestedPreset
    } else {
        selectedPreset
    }

    val calibration = DisplayCalibration(
        profile = profile,
        manualPixelsPerMillimeter = manualPxPerMm.takeIf { useManualDensity },
        manualEyeDistanceMillimeters = manualEyeDistance.takeIf { useManualEyeDistance }
    )
    val parameters = FoldParameters(calibration = calibration)
    val line = effectivePreset.resolve(angle)
    val renderState = FoldRenderState(
        angleDegrees = angle,
        line = line,
        parameters = parameters,
        quality = quality,
        panels = if (live) livePose.hingeOpeningDegrees?.let { FoldPanels.fromOpening(it) } else null
    )

    MaterialTheme {
        Column(
            modifier = interactionModifier
                .fillMaxSize()
                .safeDrawingPadding()
                .background(Color(0xFF101114))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "DuoFold Universal",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$platformLabel | ${livePose.inputKind} | ${oneDecimal(angle)} deg",
                        color = Color(0xFFB9BDC7),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(onClick = { poseSource.recalibrate() }) {
                    Text("Calibrate")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .foldSurfaceEffect(renderState),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                DemoSurface()
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C21))
            ) {
                Column(
                    modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Live input", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = live,
                            onCheckedChange = { useLiveInput = it },
                            enabled = inputAvailable
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Follow input geometry", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = followInputGeometry,
                            onCheckedChange = { followInputGeometry = it }
                        )
                    }

                    Text("Manual angle", color = Color(0xFFD8DAE0))
                    Slider(
                        value = if (live) livePose.angleDegrees else manualAngle,
                        onValueChange = {
                            useLiveInput = false
                            manualAngle = it
                        },
                        valueRange = -60f..60f
                    )

                    Text("Fold geometry", color = Color(0xFFD8DAE0))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GeometryChip(
                            label = "Center V",
                            selected = selectedPreset == FoldPreset.CENTER_VERTICAL
                        ) {
                            followInputGeometry = false
                            selectedPreset = FoldPreset.CENTER_VERTICAL
                        }
                        GeometryChip(
                            label = "Center H",
                            selected = selectedPreset == FoldPreset.CENTER_HORIZONTAL
                        ) {
                            followInputGeometry = false
                            selectedPreset = FoldPreset.CENTER_HORIZONTAL
                        }
                        GeometryChip(
                            label = "Laptop",
                            selected = selectedPreset == FoldPreset.LAPTOP_BOTTOM
                        ) {
                            followInputGeometry = false
                            selectedPreset = FoldPreset.LAPTOP_BOTTOM
                        }
                        GeometryChip(
                            label = "Auto edge",
                            selected = selectedPreset == FoldPreset.AUTO_EDGE
                        ) {
                            followInputGeometry = false
                            selectedPreset = FoldPreset.AUTO_EDGE
                        }
                    }

                    Text("Quality", color = Color(0xFFD8DAE0))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            FoldQuality.AUTO,
                            FoldQuality.LOW,
                            FoldQuality.MEDIUM,
                            FoldQuality.HIGH,
                            FoldQuality.ULTRA
                        ).forEach { option ->
                            FilterChip(
                                selected = quality == option,
                                onClick = { quality = option },
                                label = { Text(option.name) }
                            )
                        }
                    }

                    Text("Optical profile", color = Color(0xFFD8DAE0))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            DeviceOpticalProfile.PHONE,
                            DeviceOpticalProfile.TABLET,
                            DeviceOpticalProfile.LAPTOP
                        ).forEach { option ->
                            FilterChip(
                                selected = profile == option,
                                onClick = { profile = option },
                                label = { Text(option.name) }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Manual px/mm", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = useManualDensity,
                            onCheckedChange = { useManualDensity = it }
                        )
                    }
                    if (useManualDensity) {
                        Slider(
                            value = manualPxPerMm,
                            onValueChange = { manualPxPerMm = it },
                            valueRange = 2f..30f
                        )
                        Text(
                            text = "${oneDecimal(manualPxPerMm)} px/mm",
                            color = Color(0xFFB9BDC7),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Manual eye distance", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = useManualEyeDistance,
                            onCheckedChange = { useManualEyeDistance = it }
                        )
                    }
                    if (useManualEyeDistance) {
                        Slider(
                            value = manualEyeDistance,
                            onValueChange = { manualEyeDistance = it },
                            valueRange = 250f..900f
                        )
                        Text(
                            text = "${manualEyeDistance.toInt()} mm",
                            color = Color(0xFFB9BDC7),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeometryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun DemoSurface() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFF7F8FC),
                        Color(0xFFE9EEFF),
                        Color(0xFFFFECEC)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Adaptive Fold Surface",
                color = Color(0xFF171920),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "One fold model, multiple input sources and render backends.",
                color = Color(0xFF606571)
            )

            Spacer(Modifier.height(4.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C5BFF)),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("LIVE SURFACE", color = Color(0xFFCBD7FF))
                    Text(
                        "Perspective reprojection",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Gap driven blur, adaptive sample budget and generalized hinge geometry.",
                        color = Color(0xFFE8EDFF)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DemoMetric("Geometry", "Arbitrary line", Modifier.weight(1f))
                DemoMetric("Renderer", "AGSL / SkSL", Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DemoMetric("Input", "Sensor / pointer", Modifier.weight(1f))
                DemoMetric("Quality", "8 to 32 taps", Modifier.weight(1f))
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Target devices",
                        color = Color(0xFF171920),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Android phone, Android tablet, Android foldable, Chromebook style Android environment, Windows, macOS and Linux desktop.",
                        color = Color(0xFF606571)
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF7A7F8B))
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color(0xFF171920),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun oneDecimal(value: Float): String = ((value * 10f).roundToInt() / 10f).toString()
