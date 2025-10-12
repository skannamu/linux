#version 150

uniform sampler2D Sampler0;
uniform vec2      ScreenSize;
uniform float     Time;

in vec2 texCoord;
out vec4 fragColor;

float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 offsetUV = texCoord;
    float scanline = floor(offsetUV.y * ScreenSize.y);
    float timeMod = mod(Time, 100.0) / 100.0; // 시간 범위를 0~1로 제한 (글리치 속도)

    float distortion = (rand(vec2(scanline, timeMod * 10.0)) - 0.5) * 0.03;

    float glitchIntensity = sin(Time * 50.0) * 0.5 + 0.5; // 빠르게 깜빡이는 강도

    offsetUV.x += distortion * glitchIntensity;

    vec2 channelOffset = vec2(distortion * 0.5, 0.0);

    vec4 colorR = texture(Sampler0, offsetUV + channelOffset);
    vec4 colorG = texture(Sampler0, offsetUV);
    vec4 colorB = texture(Sampler0, offsetUV - channelOffset);

    fragColor = vec4(colorR.r, colorG.g, colorB.b, colorR.a);
}