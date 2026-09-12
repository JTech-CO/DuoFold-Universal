package com.example.duofold.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.duofold.model.FoldInputKind
import com.example.duofold.model.FoldPose
import com.example.duofold.model.FoldPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidHingePoseSource(
    context: Context,
    private val flatAngleDegrees: Float = 180f,
    private val defaultPreset: FoldPreset = FoldPreset.CENTER_VERTICAL
) : FoldPoseSource, SensorEventListener {
    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val hingeSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)

    private val mutablePose = MutableStateFlow(
        FoldPose(
            inputKind = FoldInputKind.HINGE_SENSOR,
            suggestedPreset = defaultPreset
        )
    )
    private val mutableAvailable = MutableStateFlow(false)

    override val pose: StateFlow<FoldPose> = mutablePose.asStateFlow()
    override val available: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    private var running = false


    override fun start() {
        if (running) return
        running = hingeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: false
    }

    override fun stop() {
        if (!running) return
        running = false
        sensorManager.unregisterListener(this)
        mutableAvailable.value = false
    }

    // A hardware opening is absolute and must not be zeroed like a motion sensor.
    override fun recalibrate() = Unit

    override fun onSensorChanged(event: SensorEvent) {
        if (!running) return
        if (event.sensor.type != Sensor.TYPE_HINGE_ANGLE) return
        val rawHingeAngle = event.values.firstOrNull() ?: return

        if (!rawHingeAngle.isFinite()) return
        val opening = (rawHingeAngle + 180f - flatAngleDegrees).coerceIn(0f, 180f)
        val visualFold = (180f - opening) * 0.5f

        mutableAvailable.value = true
        mutablePose.value = FoldPose(
            angleDegrees = visualFold,
            inputKind = FoldInputKind.HINGE_SENSOR,
            suggestedPreset = defaultPreset,
            confidence = 1f,
            hingeOpeningDegrees = opening
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
