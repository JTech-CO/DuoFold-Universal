package com.example.duofold.render

import com.example.duofold.model.*
import kotlin.math.abs

/** Shared renderer contract. Legacy angle is a per-panel tilt magnitude.
 * Supply panels for asymmetric folds or physical hinge opening angles.
 */
data class FoldRenderState(
    val angleDegrees: Float,
    val line: FoldLine,
    val parameters: FoldParameters = FoldParameters(),
    val quality: FoldQuality = FoldQuality.AUTO,
    val panels: FoldPanels? = null
) {
    val clampedAngleDegrees: Float
        get() = if (angleDegrees.isFinite()) abs(angleDegrees).coerceIn(0f, parameters.maxTiltDegrees) else 0f

    val resolvedPanels: FoldPanels
        get() = panels ?: FoldPanels(clampedAngleDegrees, -clampedAngleDegrees)

    /** Edge-on geometry is singular in screen space; rendering stops at 89 degrees. */
    val renderPanels: FoldPanels
        get() = resolvedPanels.let {
            FoldPanels(it.positiveDegrees.coerceIn(-89f, 89f), it.negativeDegrees.coerceIn(-89f, 89f))
        }

    val maximumPanelTilt: Float
        get() = renderPanels.let { maxOf(abs(it.positiveDegrees), abs(it.negativeDegrees)) }
}
