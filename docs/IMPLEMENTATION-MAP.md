# Implementation map

- `duofold-core/src/commonMain`: models, FoldPanels/FoldPhysics, input contracts, FoldRenderState.
- `duofold-core/src/commonTest`: preset/quality and rigid-panel regression tests.
- `duofold-compose/src/commonMain`: modifier contract and the single AGSL/SkSL shader.
- `duofold-compose/src/androidMain`: RuntimeShader binding, motion/hinge/adaptive adapters.
- `duofold-compose/src/desktopMain`: Skia binding and pointer input.
- `duofold-compose/src/desktopTest`: native shader compilation and raster pixel tests.
- `duofold-compose/src/androidDeviceTest`: on-device AGSL compilation/uniform binding test.
- `duofold-core/api`, `duofold-compose/api`: generated Android and desktop ABI snapshots.
- `composeApp`: shared demo; `androidApp` and `desktopApp`: launchers.
- `.github/workflows/validate.yml`: build, staging, ABI, lint and emulator matrix.
- `scripts/test-windows.ps1`: direct JUnit fallback for Unicode Gradle worker paths.
- `scripts/check-release.ps1`: evidence completeness gate, not certification.
- `validation`: actual local results and pending physical-device evidence.

Historical prototype descriptions are superseded by README.md and docs/ARCHITECTURE.md.
