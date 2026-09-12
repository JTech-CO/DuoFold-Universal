package com.example.duofold.input

import com.example.duofold.model.FoldInputKind
import com.example.duofold.model.FoldPose
import com.example.duofold.model.FoldPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PointerFoldPoseSource(
    private val degreesAcrossSurface: Float = 90f
) : FoldPoseSource {
    init { require(degreesAcrossSurface.isFinite()) }
    private val mutablePose = MutableStateFlow(
        FoldPose(
            inputKind = FoldInputKind.POINTER,
            suggestedPreset = FoldPreset.AUTO_EDGE
        )
    )
    private val mutableAvailable = MutableStateFlow(true)

    override val pose: StateFlow<FoldPose> = mutablePose.asStateFlow()
    override val available: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    private var zeroX = 0.5f
    private var latestX = 0.5f

    fun updateFromNormalized(x: Float, y: Float) {
        if (!x.isFinite() || !y.isFinite()) return
        latestX = x.coerceIn(0f, 1f)
        val angle = ((latestX - zeroX) * degreesAcrossSurface)
            .coerceIn(-60f, 60f)

        mutablePose.value = FoldPose(
            angleDegrees = angle,
            inputKind = FoldInputKind.POINTER,
            suggestedPreset = FoldPreset.AUTO_EDGE,
            confidence = 1f
        )
    }

    override fun recalibrate() {
        zeroX = latestX
        mutablePose.value = mutablePose.value.copy(angleDegrees = 0f)
    }
}
