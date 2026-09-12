package com.example.duofold.render

import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.ImageFilter
import kotlin.test.Test

class ShaderCompileTest {
    @Test fun skiaCompilesShaderAndBindsEveryUniform() {
        RuntimeEffect.makeForShader(FOLD_SHADER).use { effect ->
            RuntimeShaderBuilder(effect).use { builder ->
                builder.uniform("resolution", 800f, 600f)
                builder.uniform("panelDegrees", 45f, -30f)
                builder.uniform("eyeDistancePx", 3000f)
                builder.uniform("hingePointPx", 400f, 300f)
                builder.uniform("hingeDirection", 0f, 1f)
                builder.uniform("activeSide", 0f)
                builder.uniform("blurSpread", 0.12f)
                builder.uniform("darkening", 0.015f)
                builder.uniform("maxSamples", 16f)
                ImageFilter.makeRuntimeShader(builder, arrayOf("content"), arrayOf(null)).close()
            }
        }
    }
}
