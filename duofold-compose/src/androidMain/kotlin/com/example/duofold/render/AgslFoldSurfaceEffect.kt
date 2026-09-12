package com.example.duofold.render

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.util.DisplayMetrics
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

actual fun Modifier.foldSurfaceEffect(state: FoldRenderState): Modifier = composed {
    val context = LocalContext.current
    val metrics: DisplayMetrics = context.resources.displayMetrics

    val runtimeShader = remember {
        RuntimeShader(FOLD_SHADER)
    }
    val effect = remember(runtimeShader) {
        RenderEffect
            .createRuntimeShaderEffect(runtimeShader, "content")
            .asComposeRenderEffect()
    }

    this.graphicsLayer {
        if (size.width <= 1f || size.height <= 1f || state.maximumPanelTilt < 0.00001f) {
            renderEffect = null
            return@graphicsLayer
        }

        val line = state.line.normalized()
        val autoPxPerMm = metrics.xdpi
            .takeIf { it.isFinite() && it > 0f }
            ?.div(25.4f)

        val pxPerMm =
            state.parameters.calibration.resolvePixelsPerMillimeter(autoPxPerMm)
        val eyeDistancePx =
            state.parameters.calibration.resolveEyeDistanceMillimeters() * pxPerMm
        val sampleBudget =
            state.quality.sampleBudget(size.width, size.height, state.maximumPanelTilt)

        runtimeShader.setFloatUniform("resolution", size.width, size.height)
        runtimeShader.setFloatUniform("panelDegrees", state.renderPanels.positiveDegrees, state.renderPanels.negativeDegrees)
        runtimeShader.setFloatUniform("eyeDistancePx", eyeDistancePx)
        runtimeShader.setFloatUniform(
            "hingePointPx",
            line.anchorX * size.width,
            line.anchorY * size.height
        )
        runtimeShader.setFloatUniform(
            "hingeDirection",
            line.directionX,
            line.directionY
        )
        runtimeShader.setFloatUniform("activeSide", line.activeSide.shaderValue)
        runtimeShader.setFloatUniform("blurSpread", state.parameters.blurSpread)
        runtimeShader.setFloatUniform(
            "darkening",
            state.parameters.darkening * 6f / pxPerMm
        )
        runtimeShader.setFloatUniform("maxSamples", sampleBudget.toFloat())

        renderEffect = effect
        clip = true
    }
}
