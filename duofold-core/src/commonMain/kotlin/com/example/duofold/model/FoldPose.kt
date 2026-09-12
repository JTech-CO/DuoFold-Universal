package com.example.duofold.model

enum class FoldInputKind {
    MOTION_SENSOR,
    HINGE_SENSOR,
    POINTER,
    MANUAL
}

data class FoldPose(
    val angleDegrees: Float = 0f,
    val inputKind: FoldInputKind = FoldInputKind.MANUAL,
    val suggestedPreset: FoldPreset = FoldPreset.AUTO_EDGE,
    val confidence: Float = 1f,
    val hingeOpeningDegrees: Float? = null
)
