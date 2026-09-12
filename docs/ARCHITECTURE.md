# Architecture

`FoldPoseSource -> FoldPose -> FoldRenderState -> Modifier.foldSurfaceEffect`

Core owns calibration, an oriented FoldLine, explicit FoldPanels, a CPU geometry reference,
and input contracts. It has no Compose or platform imports. Compose owns the modifier API,
one shared shader string, and platform sensor/pointer implementations. Demo modules depend
on SDK modules; SDK modules never depend on demo UI or applications.

Each panel is a rigid rotation about a shared oriented hinge. For local normal distance s,
rotation theta gives horizontal displacement s*cos(theta) and depth s*sin(theta). Opposite
signed theta values produce a symmetric V with both outer edges approaching the viewer.
Along-hinge coordinates are unchanged. Perspective uses an eye at viewport center and a
calibrated distance in pixels. The shader solves the inverse ray-plane intersection, checks
half-plane ownership and content bounds, then resolves overlaps by nearest positive ray parameter.

Normalized anchors are multiplied by the viewport size. Directions describe axes in pixel
space; callers constructing diagonals from normalized screen endpoints must scale them by the
viewport before normalization. Android xdpi is an estimate; calibration overrides are available.

FoldRenderState's optional panels overrides the legacy scalar tilt. Hardware opening 180 means
flat and 0 means closed; angles are absolute and calibration does not reset the hinge. Motion
and pointer sources retain the legacy signed scalar input with edge selection. Near edge-on
rendering is bounded at 89 degrees to avoid degenerate projections. Invalid scalar tilt falls
back to zero; invalid explicit panel geometry and optical effect parameters fail fast.

Android consumes the shader with RuntimeShader; desktop uses Skia RuntimeEffect. The shared
source avoids renderer divergence. Blur averages premultiplied RGBA, darkens only RGB, and returns
transparent pixels outside panel projections. Blur remains an artistic approximation.

Start/stop input sources on the main thread with the host lifecycle. AdaptiveAndroidPoseSource
owns cancellable collectors only while started, prefers a hinge after a sample arrives, and
falls back to motion. Missing/stale sensor samples, display identity, crease mapping and actual
foldable pose tracking need further device validation. The demo exposes manual geometry selection.

Stable API, package ownership, automatic crease mapping, adaptive measured performance, full
resource-lifetime/performance audits, and physical device certification remain release gates.
