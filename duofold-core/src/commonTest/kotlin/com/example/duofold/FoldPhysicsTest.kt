package com.example.duofold

import com.example.duofold.model.*
import com.example.duofold.render.FoldRenderState
import kotlin.math.*
import kotlin.test.*

class FoldPhysicsTest {
    private val hinge = FoldPreset.CENTER_VERTICAL.resolve(0f)
    private fun near(a: Float, b: Float) = assertEquals(a, b, 0.001f)

    @Test fun flatIsIdentity() {
        val p = FoldPhysics.rotate(80f, 40f, 50f, 50f, hinge, FoldPanels.fromOpening(180f))
        near(80f, p.x); near(40f, p.y); near(0f, p.z)
    }
    @Test fun centralPanelsHaveOppositeRotationsAndEqualDepth() {
        val panels = FoldPanels.fromOpening(90f)
        near(45f, panels.positiveDegrees); near(-45f, panels.negativeDegrees)
        val left = FoldPhysics.rotate(20f, 30f, 50f, 50f, hinge, panels)
        val right = FoldPhysics.rotate(80f, 30f, 50f, 50f, hinge, panels)
        near(left.z, right.z); assertTrue(left.z > 0)
        near(100f, left.x + right.x)
        near(30f, sqrt((left.x - 50f).pow(2) + left.z.pow(2)))
    }
    @Test fun asymmetricFoldKeepsStationaryPanelFlat() {
        val panels = FoldPanels.fromOpening(120f, 1f)
        val right = FoldPhysics.rotate(80f, 30f, 50f, 50f, hinge, panels)
        near(80f, right.x); near(0f, right.z)
    }
    @Test fun allPointsOnArbitraryHingeRemainFixed() {
        val line = FoldLine(0.5f, 0.5f, 3f, 4f)
        val p = FoldPhysics.rotate(56f, 58f, 50f, 50f, line, FoldPanels(50f, -20f))
        near(56f, p.x); near(58f, p.y); near(0f, p.z)
    }
    @Test fun inactiveSideRemainsUnchanged() {
        val p = FoldPhysics.rotate(80f, 30f, 50f, 50f, hinge.copy(activeSide = FoldActiveSide.POSITIVE), FoldPanels(45f, -45f))
        near(80f, p.x); near(0f, p.z)
    }
    @Test fun invalidOpeningAndParametersAreRejected() {
        for (value in listOf(Float.NaN, Float.POSITIVE_INFINITY, -1f, 181f)) {
            assertFailsWith<IllegalArgumentException> { FoldPanels.fromOpening(value) }
        }
        assertFailsWith<IllegalArgumentException> { FoldParameters(maxTiltDegrees = -1f) }
        assertFailsWith<IllegalArgumentException> { FoldParameters(blurSpread = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { FoldPanels.fromOpening(0f, 1f) }
    }
    @Test fun invalidInputCannotPoisonShaderAndClosedPoseIsBounded() {
        near(0f, FoldRenderState(Float.NaN, hinge).maximumPanelTilt)
        near(89f, FoldRenderState(0f, hinge, panels = FoldPanels.fromOpening(0f)).maximumPanelTilt)
        near(1f, FoldLine(0f, 0f, Float.NaN, 0f).normalized().directionY)
    }
    @Test fun perspectiveRejectsPointsBehindEye() {
        assertNull(FoldPhysics.project(FoldPoint3(1f, 2f, 100f), 0f, 0f, 100f))
        val projected = FoldPhysics.project(FoldPoint3(10f, 5f, 50f), 0f, 0f, 100f)!!
        near(20f, projected.x); near(10f, projected.y)
    }
}
