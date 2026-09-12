package com.example.duofold.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import com.example.duofold.model.FoldInputKind
import com.example.duofold.model.FoldPose
import com.example.duofold.model.FoldPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2

class AndroidMotionPoseSource(
    context: Context
) : FoldPoseSource, SensorEventListener {
    private val appContext = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val displayManager =
        appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    private val rotationSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val mutablePose = MutableStateFlow(
        FoldPose(
            inputKind = FoldInputKind.MOTION_SENSOR,
            suggestedPreset = FoldPreset.AUTO_EDGE
        )
    )
    private val mutableAvailable = MutableStateFlow(false)

    override val pose: StateFlow<FoldPose> = mutablePose.asStateFlow()
    override val available: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    private val raw = FloatArray(9)
    private val screen = FloatArray(9)
    private var reference: FloatArray? = null
    private var running = false
    private var filteredAngle = 0f

    override fun start() {
        if (running) return
        running = rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: false
    }

    override fun stop() {
        if (!running) return
        running = false
        sensorManager.unregisterListener(this)
        mutableAvailable.value = false
    }

    override fun recalibrate() {
        reference = null
        filteredAngle = 0f
        mutablePose.value = mutablePose.value.copy(angleDegrees = 0f)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!running) return
        if (event.sensor.type != Sensor.TYPE_GAME_ROTATION_VECTOR &&
            event.sensor.type != Sensor.TYPE_ROTATION_VECTOR
        ) {
            return
        }

        SensorManager.getRotationMatrixFromVector(raw, event.values)
        remapToScreen(raw, screen)

        val zero = reference
        if (zero == null) {
            reference = screen.copyOf()
            return
        }

        val relative = multiplyTransposeLeft(zero, screen)
        val measuredRadians = atan2(relative[2].toDouble(), relative[8].toDouble())
        val measuredDegrees = Math.toDegrees(measuredRadians).toFloat().coerceIn(-60f, 60f)

        filteredAngle += (measuredDegrees - filteredAngle) * 0.62f
        mutableAvailable.value = true
        mutablePose.value = FoldPose(
            angleDegrees = filteredAngle,
            inputKind = FoldInputKind.MOTION_SENSOR,
            suggestedPreset = FoldPreset.AUTO_EDGE,
            confidence = 1f
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun remapToScreen(input: FloatArray, output: FloatArray) {
        val rotation = displayManager
            .getDisplay(Display.DEFAULT_DISPLAY)
            ?.rotation
            ?: Surface.ROTATION_0

        val axes = when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }

        SensorManager.remapCoordinateSystem(input, axes.first, axes.second, output)
    }

    private fun multiplyTransposeLeft(a: FloatArray, b: FloatArray): FloatArray {
        val out = FloatArray(9)
        for (row in 0..2) {
            for (column in 0..2) {
                out[row * 3 + column] =
                    a[row] * b[column] +
                        a[3 + row] * b[3 + column] +
                        a[6 + row] * b[6 + column]
            }
        }
        return out
    }
}
