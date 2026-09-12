package com.example.duofold

import com.example.duofold.model.FoldPreset
import com.example.duofold.model.FoldQuality
import com.example.duofold.model.resolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FoldGeometryTest {
    @Test
    fun autoEdgeUsesRightForPositiveAngle() {
        val line = FoldPreset.AUTO_EDGE.resolve(20f)
        assertEquals(1f, line.anchorX)
        assertEquals(0f, line.directionX)
        assertEquals(1f, line.directionY)
    }

    @Test
    fun autoEdgeUsesLeftForNegativeAngle() {
        val line = FoldPreset.AUTO_EDGE.resolve(-20f)
        assertEquals(0f, line.anchorX)
    }

    @Test
    fun centerHorizontalUsesHorizontalAxis() {
        val line = FoldPreset.CENTER_HORIZONTAL.resolve(30f)
        assertEquals(0.5f, line.anchorY)
        assertEquals(1f, line.directionX)
        assertEquals(0f, line.directionY)
    }

    @Test
    fun adaptiveQualityReducesSamplesForLargeSurface() {
        val samples = FoldQuality.AUTO.sampleBudget(
            widthPx = 2560f,
            heightPx = 1600f,
            angleDegrees = 35f
        )
        assertTrue(samples <= 12)
    }
}
