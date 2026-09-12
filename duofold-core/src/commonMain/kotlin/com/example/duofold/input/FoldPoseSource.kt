package com.example.duofold.input

import com.example.duofold.model.FoldPose
import kotlinx.coroutines.flow.StateFlow

interface FoldPoseSource {
    val pose: StateFlow<FoldPose>
    val available: StateFlow<Boolean>

    fun start() = Unit
    fun stop() = Unit
    fun recalibrate() = Unit
}
