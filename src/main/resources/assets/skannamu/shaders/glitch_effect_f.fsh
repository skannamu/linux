#version 150

uniform sampler2D Sampler0;
uniform vec2 ScreenSize;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;

    float scanline = floor(uv.y * ScreenSize.y);

    float t = mod(Time, 200.0) / 200.0;

    float distortion = (rand(vec2(scanline, t * 20.0)) - 0.5) * 0.04;

    float glitchIntensity = abs(sin(Time * 40.0)) * 0.7 + 0.3;

    uv.x += distortion * glitchIntensity;

    vec2 offset = vec2(distortion * 0.6, 0.0);

    vec4 colorR = texture(Sampler0, uv + offset);
    vec4 colorG = texture(Sampler0, uv);
    vec4 colorB = texture(Sampler0, uv - offset);

    vec4 combined = vec4(colorR.r, colorG.g, colorB.b, colorG.a);

    float staticNoise = rand(vec2(gl_FragCoord.xy + Time * 100.0)) * 0.1;

    fragColor = combined + vec4(vec3(staticNoise), 0.0);
}