package com.example.duofold.model



enum class FoldActiveSide(val shaderValue: Float) {
    BOTH(0f),
    POSITIVE(1f),
    NEGATIVE(-1f)
}

data class FoldLine(
    val anchorX: Float,
    val anchorY: Float,
    val directionX: Float,
    val directionY: Float,
    val activeSide: FoldActiveSide = FoldActiveSide.BOTH
) {
    init { require(anchorX.isFinite() && anchorY.isFinite()) }

    fun normalized(): FoldLine {
        val length = kotlin.math.hypot(directionX.toDouble(), directionY.toDouble()).toFloat()
        if (!length.isFinite() || length <= 0.0001f) {
            return copy(directionX = 0f, directionY = 1f)
        }
        return copy(
            directionX = directionX / length,
            directionY = directionY / length
        )
    }
}

enum class FoldPreset {
    AUTO_EDGE,
    LEFT_EDGE,
    RIGHT_EDGE,
    TOP_EDGE,
    BOTTOM_EDGE,
    CENTER_VERTICAL,
    CENTER_HORIZONTAL,
    LAPTOP_BOTTOM,
    CUSTOM
}

fun FoldPreset.resolve(
    angleDegrees: Float,
    customLine: FoldLine = FoldLine(
        anchorX = 0.5f,
        anchorY = 0.5f,
        directionX = 0f,
        directionY = 1f
    )
): FoldLine = when (this) {
    FoldPreset.AUTO_EDGE -> {
        if (angleDegrees >= 0f) {
            FoldLine(1f, 0.5f, 0f, 1f)
        } else {
            FoldLine(0f, 0.5f, 0f, 1f)
        }
    }

    FoldPreset.LEFT_EDGE -> FoldLine(0f, 0.5f, 0f, 1f)
    FoldPreset.RIGHT_EDGE -> FoldLine(1f, 0.5f, 0f, 1f)
    FoldPreset.TOP_EDGE -> FoldLine(0.5f, 0f, 1f, 0f)
    FoldPreset.BOTTOM_EDGE -> FoldLine(0.5f, 1f, 1f, 0f)
    FoldPreset.CENTER_VERTICAL -> FoldLine(0.5f, 0.5f, 0f, 1f)
    FoldPreset.CENTER_HORIZONTAL -> FoldLine(0.5f, 0.5f, 1f, 0f)
    FoldPreset.LAPTOP_BOTTOM -> FoldLine(0.5f, 1f, 1f, 0f)
    FoldPreset.CUSTOM -> customLine
}.normalized()
