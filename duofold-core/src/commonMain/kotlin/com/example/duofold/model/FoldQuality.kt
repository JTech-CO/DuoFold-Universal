package com.example.duofold.model

import kotlin.math.abs

enum class FoldQuality {
    AUTO,
    LOW,
    MEDIUM,
    HIGH,
    ULTRA;

    fun sampleBudget(widthPx: Float, heightPx: Float, angleDegrees: Float): Int {
        if (this != AUTO) {
            return when (this) {
                LOW -> 8
                MEDIUM -> 16
                HIGH -> 24
                ULTRA -> 32
                AUTO -> 16
            }
        }

        val pixels = widthPx * heightPx
        var samples = when {
            pixels >= 4_000_000f -> 8
            pixels >= 2_000_000f -> 12
            pixels >= 1_000_000f -> 16
            else -> 24
        }

        val angle = abs(angleDegrees)
        if (angle < 8f) {
            samples = minOf(samples, 8)
        } else if (angle < 18f) {
            samples = minOf(samples, 12)
        }

        return samples.coerceIn(6, 32)
    }
}
