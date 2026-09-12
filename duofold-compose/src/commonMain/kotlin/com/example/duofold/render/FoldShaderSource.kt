package com.example.duofold.render

/** Shared AGSL/SkSL source. The nearest valid panel intersection wins at overlaps. */
internal val FOLD_SHADER = """
uniform shader content;
uniform float2 resolution;
uniform float2 panelDegrees;
uniform float eyeDistancePx;
uniform float2 hingePointPx;
uniform float2 hingeDirection;
uniform float activeSide;
uniform float blurSpread;
uniform float darkening;
uniform float maxSamples;

// xy = unfolded sample, z = ray parameter, w = signed panel height.
float4 intersectPanel(float2 pixel, float degrees, float side) {
    float2 h = normalize(hingeDirection);
    float2 n = float2(-h.y, h.x);
    float2 eye = resolution * 0.5;
    float angle = radians(degrees);
    float c = cos(angle);
    float s = sin(angle);
    float a = dot(eye - hingePointPx, n);
    float b = dot(pixel - eye, n);
    float denominator = eyeDistancePx * c + b * s;
    if (abs(denominator) < 0.0001) return float4(0.0, 0.0, -1.0, 0.0);
    float ray = (eyeDistancePx * c - a * s) / denominator;
    if (ray <= 0.0001) return float4(0.0, 0.0, -1.0, 0.0);
    float2 planeXY = eye + (pixel - eye) * ray;
    float z = eyeDistancePx * (1.0 - ray);
    float across = dot(planeXY - hingePointPx, n) * c + z * s;
    if (across * side < -0.001) return float4(0.0, 0.0, -1.0, 0.0);
    float along = dot(planeXY - hingePointPx, h);
    float2 source = hingePointPx + h * along + n * across;
    if (source.x < 0.0 || source.y < 0.0 || source.x > resolution.x || source.y > resolution.y)
        return float4(0.0, 0.0, -1.0, 0.0);
    return float4(source, ray, z);
}

half4 sampleInside(float2 point) {
    if (point.x < 0.0 || point.y < 0.0 || point.x > resolution.x || point.y > resolution.y)
        return half4(0.0);
    return content.eval(point);
}

half4 main(float2 fragCoord) {
    if (resolution.x <= 1.0 || resolution.y <= 1.0) return content.eval(fragCoord);
    float positive = activeSide < -0.5 ? 0.0 : panelDegrees.x;
    float negative = activeSide > 0.5 ? 0.0 : panelDegrees.y;
    if (abs(positive) + abs(negative) < 0.00001) return content.eval(fragCoord);
    float4 p = intersectPanel(fragCoord, positive, 1.0);
    float4 n = intersectPanel(fragCoord, negative, -1.0);
    float4 hit = p;
    if (p.z <= 0.0 || (n.z > 0.0 && n.z < p.z)) hit = n;
    if (hit.z <= 0.0) return half4(0.0);
    float radius = blurSpread * abs(hit.w);
    float attenuation = max(1.0 - darkening * radius, 0.0);
    half4 color;
    if (radius < 0.5) {
        color = sampleInside(hit.xy);
    } else {
        float requestedSamples = clamp(radius * 2.0, 6.0, maxSamples);
        half4 sum = half4(0.0);
        float count = 0.0;
        for (int i = 0; i < 32; i++) {
            float fi = float(i);
            if (fi < requestedSamples) {
                float r = radius * sqrt((fi + 0.5) / requestedSamples);
                float theta = fi * 2.39996323;
                sum += sampleInside(hit.xy + r * float2(cos(theta), sin(theta)));
                count += 1.0;
            }
        }
        color = sum / half(max(count, 1.0));
    }
    // Preserve premultiplied alpha and darken RGB only.
    return half4(color.rgb * half(attenuation), color.a);
}
""".trimIndent()
