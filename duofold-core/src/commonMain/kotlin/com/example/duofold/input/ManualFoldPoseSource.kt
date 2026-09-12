package com.example.duofold.input

import com.example.duofold.model.FoldInputKind
import com.example.duofold.model.FoldPose
import com.example.duofold.model.FoldPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManualFoldPoseSource : FoldPoseSource {
    private val mutablePose = MutableStateFlow(FoldPose())
    private val mutableAvailable = MutableStateFlow(true)

    override val pose: StateFlow<FoldPose> = mutablePose.asStateFlow()
    override val available: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    fun setAngle(degrees: Float, preset: FoldPreset = FoldPreset.AUTO_EDGE) {
        mutablePose.value = FoldPose(
            angleDegrees = if (degrees.isFinite()) degrees.coerceIn(-60f, 60f) else 0f,
            inputKind = FoldInputKind.MANUAL,
            suggestedPreset = preset
        )
    }

    override fun recalibrate() {
        setAngle(0f, mutablePose.value.suggestedPreset)
    }
}
