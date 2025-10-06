package com.skannamu.client.gui;

import com.skannamu.network.TerminalCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TerminalScreen extends Screen {

    private final List<String> outputLines = new ArrayList<>();
    private String currentInput = ""; // 현재 입력 중인 텍스트
    private String prompt = ""; // 초기화는 init에서

    public TerminalScreen() {
        super(Text.literal("Portable Terminal"));
    }

    @Override
    protected void init() {
        prompt = "SKN@" + client.getSession().getUsername() + ":~# ";
        appendOutput(prompt); // 초기 프롬프트 표시
    }

    public String getPrompt() {
        return prompt; // prompt getter
    }

    private void handleCommand(String command) {
        if (!command.isEmpty()) {
            appendOutput(">>> " + command + '\n'); // 명령어 기록
            ClientPlayNetworking.send(new TerminalCommandPayload(command));
        }
    }

    public void appendOutput(String output) {
        outputLines.add(output);
        if (outputLines.size() > 100) { // 출력 제한
            outputLines.remove(0);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { // Enter 또는 Numpad Enter
            String command = currentInput.trim();
            handleCommand(command);
            currentInput = ""; // 입력 초기화
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { // Esc 키
            this.client.setScreen(null);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !currentInput.isEmpty()) { // Backspace
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) { // Delete
            currentInput = "";
            return true;
        }
        char character = getCharacterFromKeyCode(keyCode, scanCode, modifiers);
        if (character != 0 && isPrintable(character)) {
            if (textRenderer.getWidth(prompt + currentInput + character) < width - 20) { // 화면 너비 제한
                currentInput += character;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private char getCharacterFromKeyCode(int keyCode, int scanCode, int modifiers) {
        String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
        if (keyName != null && keyName.length() == 1) {
            return keyName.charAt(0);
        }
        if (modifiers == GLFW.GLFW_MOD_SHIFT && keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return (char) (keyCode - GLFW.GLFW_KEY_A + 'A');
        }
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return (char) (keyCode - GLFW.GLFW_KEY_A + 'a');
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            return (char) (keyCode - GLFW.GLFW_KEY_0 + '0');
        }
        // 기본 특수문자 (Shift 상태 고려)
        if (modifiers == GLFW.GLFW_MOD_SHIFT) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_1: return '!';
                case GLFW.GLFW_KEY_2: return '@';
                case GLFW.GLFW_KEY_3: return '#';
                case GLFW.GLFW_KEY_4: return '$';
                case GLFW.GLFW_KEY_5: return '%';
                case GLFW.GLFW_KEY_6: return '^';
                case GLFW.GLFW_KEY_7: return '&';
                case GLFW.GLFW_KEY_8: return '*';
                case GLFW.GLFW_KEY_9: return '(';
                case GLFW.GLFW_KEY_0: return ')';
                case GLFW.GLFW_KEY_MINUS: return '_';
                case GLFW.GLFW_KEY_EQUAL: return '+';
                case GLFW.GLFW_KEY_LEFT_BRACKET: return '{';
                case GLFW.GLFW_KEY_RIGHT_BRACKET: return '}';
                case GLFW.GLFW_KEY_SEMICOLON: return ':';
                case GLFW.GLFW_KEY_APOSTROPHE: return '"';
                case GLFW.GLFW_KEY_BACKSLASH: return '|';
                case GLFW.GLFW_KEY_COMMA: return '<';
                case GLFW.GLFW_KEY_PERIOD: return '>';
                case GLFW.GLFW_KEY_SLASH: return '?';
            }
        } else {
            switch (keyCode) {
                case GLFW.GLFW_KEY_MINUS: return '-';
                case GLFW.GLFW_KEY_EQUAL: return '=';
                case GLFW.GLFW_KEY_LEFT_BRACKET: return '[';
                case GLFW.GLFW_KEY_RIGHT_BRACKET: return ']';
                case GLFW.GLFW_KEY_SEMICOLON: return ';';
                case GLFW.GLFW_KEY_APOSTROPHE: return '\'';
                case GLFW.GLFW_KEY_BACKSLASH: return '\\';
                case GLFW.GLFW_KEY_COMMA: return ',';
                case GLFW.GLFW_KEY_PERIOD: return '.';
                case GLFW.GLFW_KEY_SLASH: return '/';
                case GLFW.GLFW_KEY_SPACE: return ' ';
            }
        }
        return 0; // 처리되지 않은 키
    }

    private boolean isPrintable(char c) {
        return c >= 32 && c < 127; // 기본 인쇄 가능 ASCII 문자
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF000000); // 검은 배경
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // 출력 및 입력 렌더링
        int y = 10;
        for (int i = 0; i < outputLines.size() - 1; i++) { // 마지막 줄 제외
            context.drawTextWithShadow(textRenderer, outputLines.get(i), 10, y, 0xFFFFFFFF);
            y += textRenderer.fontHeight + 2;
        }
        // 마지막 줄 (프롬프트 + 입력)
        if (!outputLines.isEmpty()) {
            String lastLine = outputLines.get(outputLines.size() - 1);
            if (lastLine.equals(prompt)) {
                context.drawTextWithShadow(textRenderer, prompt, 10, y, 0xFF00FF00); // 녹색 프롬프트
                context.drawTextWithShadow(textRenderer, currentInput, 10 + textRenderer.getWidth(prompt), y, 0xFFFFFFFF); // 입력 텍스트
                // 커서 표시
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    context.drawTextWithShadow(textRenderer, "_", 10 + textRenderer.getWidth(prompt + currentInput), y, 0xFFFFFFFF);
                }
            } else {
                // 서버 응답 출력
                context.drawTextWithShadow(textRenderer, lastLine, 10, y, 0xFFFFFFFF);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}