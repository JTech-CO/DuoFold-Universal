# DuoFold Universal

Reusable KMP fold rendering SDK, currently **1.0.0-dev.1**, preparing for 1.0 Stable.
This is a development candidate, not a validated universal SDK release. Previous 2.0.0
labels described the prototype revision and do not represent a released SDK major version.

## Modules

| Module | Purpose |
| --- | --- |
| duofold-core | Fold geometry, rigid panel physics, calibration, render state, input contracts; no Compose dependency |
| duofold-compose | Shared shader, Compose modifier, Android sensor and Desktop pointer adapters |
| composeApp | Shared demo UI; not published |
| androidApp | Android application host; not published |
| desktopApp | Windows/macOS/Linux JVM demo host; not published |

Public packages currently retain `com.example.duofold` for source continuity. The Maven group
`dev.duofold` is provisional for local staging only. Review identities and freeze API before Stable.
Android/desktop ABI snapshots are in each SDK module's `api` directory.

## Use the SDK

Within this build, depend on `implementation(project(":duofold-compose"))`. For a local
Maven consumer, stage the libraries, add `build/staging-repository` as a Maven repository,
then depend on `dev.duofold:duofold-compose:1.0.0-dev.1`. KMP/Gradle selects Android or JVM
artifacts. Core-only consumers use `dev.duofold:duofold-core:1.0.0-dev.1`.

```kotlin
import com.example.duofold.model.*
import com.example.duofold.render.*

val state = FoldRenderState(
    angleDegrees = 0f,
    line = FoldPreset.CENTER_VERTICAL.resolve(0f),
    panels = FoldPanels.fromOpening(120f), // 180 flat, 0 closed
    parameters = FoldParameters(
        calibration = DisplayCalibration(profile = DeviceOpticalProfile.TABLET)
    )
)
// Apply to the content whose pixels should fold:
// Box(Modifier.foldSurfaceEffect(state)) { content() }
```

`FoldPanels` supports independent signed panel rotations within [-90, 90]. Positive and
negative refer to the oriented hinge normal `(-directionY, directionX)`, not left/right.
`fromOpening` distributes closure across panels; unsupported shares requiring rotation beyond
90 degrees are rejected. Closed/edge-on rendering is regularized to 89 degrees. The older
`angleDegrees` path retains per-panel tilt magnitude semantics. Hardware hinge inputs now
carry absolute opening separately and are never zeroed by calibration.

Shaders inverse-project each output pixel onto both rigid panels, clip against unfolded
content bounds, and choose the closest valid intersection. Blur is a stylized gap effect,
not calibrated optical refraction. Transparent content retains premultiplied alpha.
This is prescribed rigid geometry, not a spring/mass hinge dynamics simulation.

## Build and test

Use JDK 21 (JVM bytecode target 17), Android SDK platform `android-37.0`, Build Tools 36.0.0,
and the checked-in Gradle 9.5.0 Wrapper. Kotlin is 2.4.20, Compose Multiplatform 1.12.0,
AGP 9.3.1. The sample targets API 36; Compose's AAR metadata requires compile API 37.
Core minSdk is 23; renderer/demo minSdk is 33 because RuntimeShader requires it.

```sh
./gradlew :duofold-core:desktopTest :duofold-compose:desktopTest
./gradlew :androidApp:assembleDebug :androidApp:lintDebug
./gradlew :desktopApp:run
./gradlew :desktopApp:createDistributable
./gradlew :duofold-core:checkKotlinAbi :duofold-compose:checkKotlinAbi
./gradlew :duofold-core:publishAllPublicationsToStagingRepository :duofold-compose:publishAllPublicationsToStagingRepository
./gradlew :duofold-compose:connectedAndroidDeviceTest
```

On Windows use `gradlew.bat`. Some Windows configurations fail Gradle test worker class
loading under Unicode paths. `pwsh scripts/test-windows.ps1` compiles the same tests and
runs them directly with JUnit. It does not copy source outside the project. Set JAVA_HOME
and ANDROID_HOME for your installation. Local tools downloaded for this session are ignored.

CI defines Windows, macOS and Linux builds, SDK staging, ABI checks, Android lint and
API 33/36 emulator shader compilation. Workflow presence is not a successful CI run.
The local validation report predates Git setup. Development changes are tracked in
[JTech-CO/DuoFold-Universal](https://github.com/JTech-CO/DuoFold-Universal).

## Validation and release

See [validation protocol](validation/README.md), [local results](validation/LOCAL-RESULTS.md),
[architecture](docs/ARCHITECTURE.md) and [licensing gate](docs/LICENSING.md).

Galaxy phone, Tab, Z Fold, Z Flip, Windows and macOS must all pass on physical hardware,
with visual captures, frame traces and lifecycle checks. macOS uses Compose Desktop/JVM;
it is not an Apple-native/Kotlin-Native renderer. Latest Compose platform requirements must
be checked for the intended CPU/OS before promising support.

The hinge sensor does not identify crease orientation/location or display occlusion.
Select vertical/horizontal geometry explicitly as appropriate and validate rotation,
cover displays, multi-window and DeX. Automatic WindowManager crease mapping and external
monitor pose handling remain release work. AUTO quality is a heuristic, not measured adaptation.

No project-wide redistribution license has been granted by this change. The earlier README
recorded an unconfirmed licensing relationship to [Atomicx7/Duo-animation](https://github.com/Atomicx7/Duo-animation).
Resolve provenance and choose the license before public distribution. Staging publishes only
into this build directory. Signing and public publication are deliberately not configured yet.

Toolchain references: [KMP compatibility](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html),
[Android KMP plugin](https://developer.android.com/kotlin/multiplatform/plugin),
[Compose compatibility](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html).

Windows packaging under Unicode paths: run `pwsh scripts/package-windows.ps1`. This creates the jlink runtime using direct arguments and then runs Compose packaging. The result is an application directory, not a signed installer. When changing runtime module requirements or JDK, regenerate the build outputs.

The independent `examples/sdk-consumer` project consumes only staged Maven artifacts. After staging, run `./gradlew -p examples/sdk-consumer run` to verify SDK consumption.
