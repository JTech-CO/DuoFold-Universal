package com.example.duofold.render

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun Modifier.foldSurfaceEffect(state: FoldRenderState): Modifier = composed {
    val runtimeEffect = remember {
        RuntimeEffect.makeForShader(FOLD_SHADER)
    }

    DisposableEffect(runtimeEffect) { onDispose { runtimeEffect.close() } }

    this.graphicsLayer {
        if (size.width <= 1f || size.height <= 1f || state.maximumPanelTilt < 0.00001f) {
            renderEffect = null
            return@graphicsLayer
        }

        val line = state.line.normalized()
        val pxPerMm =
            state.parameters.calibration.resolvePixelsPerMillimeter(autoValue = null)
        val eyeDistancePx =
            state.parameters.calibration.resolveEyeDistanceMillimeters() * pxPerMm
        val sampleBudget =
            state.quality.sampleBudget(size.width, size.height, state.maximumPanelTilt)

        RuntimeShaderBuilder(runtimeEffect).use { builder ->
            builder.uniform("resolution", size.width, size.height)
            builder.uniform("panelDegrees", state.renderPanels.positiveDegrees, state.renderPanels.negativeDegrees)
            builder.uniform("eyeDistancePx", eyeDistancePx)
            builder.uniform(
                "hingePointPx",
                line.anchorX * size.width,
                line.anchorY * size.height
            )
            builder.uniform(
                "hingeDirection",
                line.directionX,
                line.directionY
            )
            builder.uniform("activeSide", line.activeSide.shaderValue)
            builder.uniform("blurSpread", state.parameters.blurSpread)
            builder.uniform(
                "darkening",
                state.parameters.darkening * 6f / pxPerMm
            )
            builder.uniform("maxSamples", sampleBudget.toFloat())

            renderEffect = ImageFilter.makeRuntimeShader(
                runtimeShaderBuilder = builder,
                shaderNames = arrayOf("content"),
                inputs = arrayOf(null)
            ).asComposeRenderEffect()

        }

        clip = true
    }
}
