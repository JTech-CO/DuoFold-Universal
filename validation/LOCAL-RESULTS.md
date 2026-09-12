# Local validation results - 2026-09-12

Candidate: 1.0.0-dev.1. Host: Windows x64. These local results were recorded before Git setup;
they do not certify a release commit or a remote CI run. The evidence below is local only.

| Check | Result |
| --- | --- |
| Core preset/quality/geometry tests | 12 passed, direct JUnit 4.13.2 |
| Native Skia shader compile/uniform test | 1 passed |
| Native Skia raster pixel tests | 4 passed: identity/alpha, symmetric fold, asymmetric fold, blurred alpha |
| Android demo debug APK | Built successfully, target API 36, minimum API 33 |
| Android lint | 0 errors, 4 warnings (target API choice and newer dependency notifications) |
| Android + desktop ABI checks, both SDK modules | Passed against generated development snapshots; public API approval still pending |
| Core + Compose Maven staging | Built Android AAR, JVM JAR, metadata and sources locally |
| Independent Maven consumer | Built and ran with staged SDK; model and modifier assertions passed |
| Windows application directory | Built, including bundled runtime; not signed or visually validated |
| Standard Gradle desktop test workers in this Unicode path | Failed class loading; direct JUnit fallback passes identical compiled test classes |
| Android AGSL instrumentation APK | Built successfully; not executed on a device |
| Android physical-device AGSL execution | Not run; adb device list empty |
| macOS/Linux execution and remote CI | Not run |
| Hardware rendering, frame timings and thermal/memory measurements | Not measured |

The native raster tests execute Skia's raster backend. They are not GPU backend certification,
Compose-window visual QA, optical accuracy measurements or performance benchmarks.
The independent consumer uses a JVM target; an independent Android consumer still needs validation.

## Evidence and artifacts

The log files listed below remain local and are excluded from Git. The result summary
and artifact hashes are tracked; CI uploads its own reports when executed.

- `logs/android-sdk.log`: final SDK, APK, instrumentation APK, lint and ABI build.
- `logs/build.log`: successful APK, lint, ABI, staging and Windows app-directory build.
- `logs/tests.log`: reproducible compilation and direct JUnit results (17 total tests).
- `logs/consumer.log`: independent staged-Maven consumer build/run.
- `artifact-sha256.txt`: hashes of selected locally generated artifacts.
- APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- Windows app directory: `desktopApp/build/compose/binaries/main/app/DuoFoldUniversal`.
  Keep the whole directory together when running its executable.
- SDK repository: `build/staging-repository`.

## Reproduction

Set JAVA_HOME to JDK 21 and ANDROID_HOME to an SDK containing platform android-37.0 and
Build Tools 36.0.0. The project uses Gradle 9.5.0, KMP 2.4.20, Compose 1.12.0 and AGP 9.3.1.
Compile SDK 37 is required by resolved Compose AAR metadata; the application still targets 36.

Use `scripts/test-windows.ps1` for direct JUnit and `scripts/package-windows.ps1` for Windows
packaging in Unicode paths. The packaging helper generates jlink output with direct arguments;
the Compose plugin's jlink argument file did not produce a runtime under this host's Unicode path.
Only the current known runtime modules are included. Rebuild runtime outputs when requirements change.

A temporary ASCII cache junction was used during diagnosis; source was never copied to the
public directory. Automatic approval rejected that proposed copy because it could expose source.
That path-copy approach was abandoned. Validation ran from the original project directory.

## Unfinished release work

Physical Galaxy phone/Tab/Fold/Flip and Windows/macOS verification, Android on-device shader
execution, realistic frame traces, automated crease/window mapping, backend resource-lifetime
and allocation audits, measured quality tuning, API/package ownership review, project license
and third-party provenance, signing, installers, remote CI and release publication remain open.
There is no claim of 1.0 Stable, Alpha certification, or measured universal performance.
