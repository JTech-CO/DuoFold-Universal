# DuoFold 1.0 validation protocol

Status: development candidate. A passing CI build is not physical-device certification.

## Required evidence

Use the exact candidate commit and artifact SHA-256 on Galaxy phone, Galaxy Tab,
Galaxy Z Fold, Galaxy Z Flip, Windows and macOS. Save model, OS/build, GPU/backend,
display resolution/density, refresh rate, power mode, toolchain, test date and tester.
Store reports and traces outside source control if large; link them in release-readiness.json.
A maintainer reviews evidence before changing a row to passed. Never use emulator results
as physical Galaxy evidence. This directory is not a certification claim.

## Geometry and visual checks

- Identity at zero tilt, premultiplied alpha, transparent edges, content orientation.
- Central vertical/horizontal openings 180, 150, 120, 90, 30, 0 degrees.
- Asymmetric rotations and each active-side mask; diagonal and off-center hinge.
- No duplicate images, inverted panels, cracks, NaN flashes or opaque black borders.
- Compare CPU reference projections with captured GPU pixel landmarks.
- 0-degree opening is geometrically closed; rendering regularizes edge-on panels at 89 degrees.
- Hardware hinge opening is absolute. Fold uses vertical, Flip horizontal. The sensor does
  not supply crease position, orientation or window occlusion. Validate manual selection
  in portrait/landscape, multi-window, cover display, DeX and external displays.
- Virtual glass blur is an artistic gap-based approximation, not measured refraction.

## Interaction and lifecycle

Rotate, resize, split screen, fold/unfold, background/resume 50 times. Verify sensor
registration stops in background and resumes without duplicate collectors. Confirm fallback
when sensor registration fails and when no hinge sample arrives. Test pointer/manual sources.
Check all controls are reachable at phone widths and enlarged system font settings.

## Performance acceptance proposal (requires product sign-off)

After 30 seconds warmup, record three 120-second runs for LOW/AUTO/HIGH/ULTRA at native
resolution with a repeatable 0-to-60-to-0 tilt sweep. Use Perfetto/FrameTimeline on Android,
and backend-appropriate frame tracing on Windows/macOS. Record CPU/GPU frame p50/p95/p99,
dropped frames, process RSS, thermal state, and allocations per frame.
Proposed AUTO gate: p95 within one refresh interval, p99 within two, missed frames under 1%,
no sustained thermal regression or unbounded memory growth after 10 minutes. Repeat at 60 Hz
and high refresh rates. Keep raw traces, not only averages. If a tier fails, lower the quality
policy based on evidence and repeat validation. No performance numbers have been measured yet.

## Release gates

1. All CI jobs pass on the candidate; install Android APK and native desktop packages.
2. API and binary compatibility baseline reviewed; external consumer uses staged artifacts.
3. All six physical-device reports pass, including visual and performance evidence.
4. Confirm ownership/provenance, choose a license and complete third-party notices.
5. Confirm publishing namespace, POM, signing, package identities, and release notes.
6. Run `pwsh scripts/check-release.ps1`; review its evidence links. Only then change the
   version to 1.0.0 and sign/publish. This script checks completeness, not authenticity.
