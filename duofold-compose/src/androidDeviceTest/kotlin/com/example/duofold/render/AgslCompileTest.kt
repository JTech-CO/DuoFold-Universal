package com.example.duofold.render

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgslCompileTest {
    @Test fun compilesAndBindsOnAndroidRuntime() {
        val shader = RuntimeShader(FOLD_SHADER)
        shader.setFloatUniform("resolution", 800f, 600f)
        shader.setFloatUniform("panelDegrees", 45f, -30f)
        shader.setFloatUniform("eyeDistancePx", 3000f)
        shader.setFloatUniform("hingePointPx", 400f, 300f)
        shader.setFloatUniform("hingeDirection", 0f, 1f)
        shader.setFloatUniform("activeSide", 0f)
        shader.setFloatUniform("blurSpread", 0.12f)
        shader.setFloatUniform("darkening", 0.015f)
        shader.setFloatUniform("maxSamples", 16f)
        RenderEffect.createRuntimeShaderEffect(shader, "content")
    }
}
