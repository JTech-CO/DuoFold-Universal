package com.example.duofold.model

enum class DeviceOpticalProfile(
    val defaultEyeDistanceMillimeters: Float,
    val fallbackPixelsPerMillimeter: Float
) {
    PHONE(420f, 15.5f),
    TABLET(550f, 10.5f),
    LAPTOP(650f, 5.0f),
    CUSTOM(450f, 6.0f)
}

data class DisplayCalibration(
    val profile: DeviceOpticalProfile = DeviceOpticalProfile.PHONE,
    val manualPixelsPerMillimeter: Float? = null,
    val manualEyeDistanceMillimeters: Float? = null
) {
    fun resolvePixelsPerMillimeter(autoValue: Float?): Float {
        val manual = manualPixelsPerMillimeter
        if (manual != null && manual.isFinite() && manual > 0f) {
            return manual
        }

        if (autoValue != null && autoValue.isFinite() && autoValue in 2f..40f) {
            return autoValue
        }

        return profile.fallbackPixelsPerMillimeter
    }

    fun resolveEyeDistanceMillimeters(): Float {
        val manual = manualEyeDistanceMillimeters
        return if (manual != null && manual.isFinite() && manual > 100f) {
            manual
        } else {
            profile.defaultEyeDistanceMillimeters
        }
    }
}

data class FoldParameters(
    val calibration: DisplayCalibration = DisplayCalibration(),
    val blurSpread: Float = 0.12f,
    val darkening: Float = 0.015f,
    val maxTiltDegrees: Float = 60f
) {
    init {
        require(blurSpread.isFinite() && blurSpread in 0f..1f)
        require(darkening.isFinite() && darkening >= 0f)
        require(maxTiltDegrees.isFinite() && maxTiltDegrees in 0f..89f)
    }
}
