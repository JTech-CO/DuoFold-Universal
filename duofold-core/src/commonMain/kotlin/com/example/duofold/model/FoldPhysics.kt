package com.example.duofold.model

import kotlin.math.*

/** Signed rotations around the oriented hinge; opposite signs form a central V.
 * Opening 180 = flat, 0 = closed. Positive-side rotation is toward the viewer.
 * Angles describe rigid geometry, not spring dynamics or the device's absolute pose.
 */
data class FoldPanels(val positiveDegrees: Float, val negativeDegrees: Float) {
    init {
        require(positiveDegrees.isFinite() && positiveDegrees in -90f..90f)
        require(negativeDegrees.isFinite() && negativeDegrees in -90f..90f)
    }
    companion object {
        fun fromOpening(openingDegrees: Float, positiveShare: Float = 0.5f): FoldPanels {
            require(openingDegrees.isFinite() && openingDegrees in 0f..180f)
            require(positiveShare.isFinite() && positiveShare in 0f..1f)
            val closure = 180f - openingDegrees
            return FoldPanels(closure * positiveShare, -closure * (1f - positiveShare))
        }
    }
}

data class FoldPoint3(val x: Float, val y: Float, val z: Float)

/** CPU reference for the renderer. Coordinates and direction are in physical pixels. */
object FoldPhysics {
    fun rotate(x: Float, y: Float, hingeX: Float, hingeY: Float, line: FoldLine, panels: FoldPanels): FoldPoint3 {
        val h = line.normalized()
        val nx = -h.directionY
        val ny = h.directionX
        val dx = x - hingeX
        val dy = y - hingeY
        val along = dx * h.directionX + dy * h.directionY
        val across = dx * nx + dy * ny
        val active = line.activeSide == FoldActiveSide.BOTH ||
            (line.activeSide == FoldActiveSide.POSITIVE && across >= 0f) ||
            (line.activeSide == FoldActiveSide.NEGATIVE && across < 0f)
        val degrees = if (!active) 0f else if (across >= 0f) panels.positiveDegrees else panels.negativeDegrees
        val theta = degrees * (PI / 180.0).toFloat()
        return FoldPoint3(
            hingeX + h.directionX * along + nx * across * cos(theta),
            hingeY + h.directionY * along + ny * across * cos(theta),
            across * sin(theta)
        )
    }

    /** Perspective projection from an eye above the surface. Null means behind the eye. */
    fun project(point: FoldPoint3, eyeX: Float, eyeY: Float, eyeDistance: Float): FoldPoint3? {
        require(eyeDistance.isFinite() && eyeDistance > 0f)
        val depth = eyeDistance - point.z
        if (depth <= 0.001f) return null
        val scale = eyeDistance / depth
        return FoldPoint3(eyeX + (point.x - eyeX) * scale, eyeY + (point.y - eyeY) * scale, point.z)
    }
}
