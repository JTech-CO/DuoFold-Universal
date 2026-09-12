# 1.0.0-dev.1 - SDK development candidate

- Split reusable core and Compose libraries from shared UI and application hosts.
- Add independent rigid-panel rotations, absolute hinge opening, inverse projection,
  overlap resolution and shared alpha-preserving AGSL/SkSL source.
- Add geometry and native raster pixel tests, Android shader smoke test, and ABI snapshots.
- Update toolchain, API 36 runtime target, Wrapper, local Maven staging and validation CI.
- Correct sensor collector lifecycle and make demo controls scrollable on small screens.
- Physical device/performance certification and licensing remain incomplete. Not Stable.

---
# Changelog

All notable changes in DuoFold Universal are documented here.

## 2.0.0 - 2026-09-12

### Architecture

1. Separated motion input from rendering.

   The old direct relationship between a motion model and a Compose shader modifier is replaced by platform independent render state.

2. Added `FoldPoseSource`.

   Motion sensors, hinge sensors, pointer input and manual input now share one interface.

3. Replaced fixed left and right hinges with `FoldLine`.

   A fold line is represented by an anchor point, a direction vector and an optional active side.

4. Added center and arbitrary fold geometry.

   New presets include center vertical, center horizontal, top edge, bottom edge and laptop bottom. `CUSTOM` accepts an arbitrary normalized line.

5. Split AGSL and SkSL rendering.

   Android uses `RuntimeShader` and AGSL. Desktop uses Skia `RuntimeEffect`, `RuntimeShaderBuilder` and SkSL.

### Performance

6. Added quality presets and adaptive sampling.

   LOW uses up to 8 samples.
   MEDIUM uses up to 16 samples.
   HIGH uses up to 24 samples.
   ULTRA uses up to 32 samples.
   AUTO selects a budget according to pixel area and fold angle.

   The shader keeps a fixed upper loop bound for portability while skipping texture evaluation outside the active sample budget.

### Calibration

7. Added device optical profiles and manual calibration.

   Android reads `xdpi` when the reported value is valid.
   Manual px/mm can override automatic density.
   PHONE, TABLET and LAPTOP profiles define fallback density and default viewing distance.
   Eye distance can also be overridden manually.

### Multiplatform

8. Moved shared UI, geometry, render state and input contracts to Kotlin Multiplatform common code.

   The project now has Android and Desktop targets in one `composeApp` module.

### Foldable support

9. Added optional Android hardware hinge angle input.

   `AndroidHingePoseSource` reads `Sensor.TYPE_HINGE_ANGLE`.
   `AdaptiveAndroidPoseSource` prefers the hinge sensor and falls back to the normal rotation vector source.
   The default hardware hinge geometry suggestion is center vertical.

### Desktop input

10. Added pointer based Desktop input.

    `PointerFoldPoseSource` converts normalized pointer X position into fold angle.
    Recalibration moves the pointer zero point to the current cursor position.
    This is the default live control path on Windows, macOS and Linux.

### Device coverage

The new architecture targets:

- Android phones
- Android tablets
- Android foldables
- Android emulators with manual control
- Chromebook style Android environments
- Samsung DeX with manual input recommended for external displays
- Windows Desktop
- macOS Desktop
- Linux Desktop

### Compatibility notes

Android still requires API 33 or newer because the AGSL renderer uses Android `RuntimeShader`.

Desktop rendering requires a Compose Desktop and Skia runtime that supports `RuntimeEffect` and runtime shader image filters.

Physical laptop lid angle is not used by default because there is no portable cross platform API for it. Vendor specific implementations can be added as new `FoldPoseSource` adapters.
