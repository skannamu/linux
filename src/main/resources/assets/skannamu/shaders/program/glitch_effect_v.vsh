#version 150

in vec3 Position;
in vec2 TexCoord;

out vec2 texCoord;

void main() {
    gl_Position = vec4(Position, 1.0);
    texCoord = TexCoord;
}