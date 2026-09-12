package com.example.duofold.render

import org.jetbrains.skia.*
import kotlin.test.*

/** Native Skia raster execution. These are pixel tests, not a GPU performance claim. */
class ShaderPixelTest {
    private fun render(positive: Float, negative: Float, blur: Float = 0f, check: (Bitmap) -> Unit) {
        Surface.makeRasterN32Premul(128, 128).use { source ->
            source.canvas.clear(0x80ff0000.toInt())
            Paint().use { paint ->
                paint.blendMode = BlendMode.SRC
                paint.color = 0x800000ff.toInt()
                source.canvas.drawRect(Rect.makeXYWH(64f, 0f, 64f, 128f), paint)
            }
            source.makeImageSnapshot().use { image ->
                image.makeShader().use { child ->
                    RuntimeEffect.makeForShader(FOLD_SHADER).use { effect ->
                        RuntimeShaderBuilder(effect).use { builder ->
                            builder.child("content", child)
                            builder.uniform("resolution", 128f, 128f)
                            builder.uniform("panelDegrees", positive, negative)
                            builder.uniform("eyeDistancePx", 500f)
                            builder.uniform("hingePointPx", 64f, 64f)
                            builder.uniform("hingeDirection", 0f, 1f)
                            builder.uniform("activeSide", 0f)
                            builder.uniform("blurSpread", blur)
                            builder.uniform("darkening", 0f)
                            builder.uniform("maxSamples", 16f)
                            builder.makeShader().use { shader ->
                                Surface.makeRasterN32Premul(128, 128).use { output ->
                                    output.canvas.clear(0)
                                    Paint().use { paint ->
                                        paint.shader = shader
                                        output.canvas.drawRect(Rect.makeWH(128f, 128f), paint)
                                    }
                                    Bitmap().use { bitmap ->
                                        bitmap.allocN32Pixels(128, 128)
                                        assertTrue(output.readPixels(bitmap, 0, 0))
                                        check(bitmap)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    @Test fun flatPreservesContentAndAlpha() = render(0f, 0f) {
        assertEquals(0x80ff0000.toInt(), it.getColor(20, 64))
        assertEquals(0x800000ff.toInt(), it.getColor(100, 64))
    }
    @Test fun centralFoldShrinksBothPanelsAndLeavesTransparentEdges() = render(60f, -60f) {
        assertEquals(0, it.getColor(5, 64))
        assertEquals(0, it.getColor(122, 64))
        assertEquals(0x80ff0000.toInt(), it.getColor(45, 64))
        assertEquals(0x800000ff.toInt(), it.getColor(83, 64))
    }
    @Test fun asymmetricFoldOnlyShrinksMovingPanel() = render(60f, 0f) {
        assertEquals(0, it.getColor(5, 64))
        assertEquals(0x800000ff.toInt(), it.getColor(122, 64))
    }
    @Test fun blurredInteriorRetainsSemiTransparency() = render(45f, -45f, 0.12f) {
        assertEquals(128, it.getColor(45, 64).ushr(24))
    }
}
