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
            // 2. 명령어 기록을 '프롬프트 + 입력'으로 변경하여 렌더링 로직과 일치시킵니다.
            String commandLine = prompt + currentInput; // currentInput을 사용해야 공백도 포함됨
            outputLines.set(outputLines.size() - 1, commandLine); // 마지막 줄 (프롬프트만 있던 줄)을 명령어 줄로 교체
            ClientPlayNetworking.send(new TerminalCommandPayload(command));
        }
    }

    public void appendOutput(String output) {
        // 1. \n을 기준으로 문자열을 분리하여 각 줄을 outputLines에 추가
        String[] lines = output.split("\\n");
        for (String line : lines) {
            outputLines.add(line);
        }

        // 1. 응답이 끝난 후, 자동으로 다음 프롬프트를 추가합니다.
        if (!outputLines.isEmpty() && !outputLines.get(outputLines.size() - 1).equals(prompt)) {
            outputLines.add(prompt);
        }

        // 출력 제한
        while (outputLines.size() > 100) {
            outputLines.remove(0);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { // Enter 또는 Numpad Enter
            String command = currentInput; // trim()을 제거하여 공백만 입력 시에도 처리되도록 함 (handleCommand에서 !command.isEmpty()로 체크)
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
        // 문자 입력 로직은 charTyped로 이동합니다.
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // 3. charTyped를 사용하여 Shift를 포함한 문자 입력을 정확하게 처리하고, 오류가 나던 isPrintable 부분을 대체
    @Override
    public boolean charTyped(char chr, int modifiers) {
        // chr이 인쇄 가능한 ASCII 문자 범위 (32~126)에 있는지 확인합니다.
        if (chr >= 32 && chr <= 126 && chr != '\n' && chr != '\r') {
            if (textRenderer.getWidth(prompt + currentInput + chr) < width - 20) { // 화면 너비 제한
                currentInput += chr;
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
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
        for (int i = 0; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            int startX = 10;

            // 현재 입력 중인 마지막 줄
            if (i == outputLines.size() - 1 && line.equals(prompt)) {
                // 현재 프롬프트 (녹색)
                context.drawTextWithShadow(textRenderer, prompt, startX, y, 0xFF00FF00);
                startX += textRenderer.getWidth(prompt);

                // 입력 텍스트 (흰색)
                context.drawTextWithShadow(textRenderer, currentInput, startX, y, 0xFFFFFFFF);

                // 커서 표시
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    startX += textRenderer.getWidth(currentInput);
                    context.drawTextWithShadow(textRenderer, "_", startX, y, 0xFFFFFFFF);
                }
            }
            // 2. 명령어 입력이 완료된 이전 줄 (프롬프트로 시작하는 경우)
            else if (line.startsWith(prompt)) {
                // 프롬프트 부분 (녹색)
                context.drawTextWithShadow(textRenderer, prompt, startX, y, 0xFF00FF00);
                startX += textRenderer.getWidth(prompt);

                // 명령어 부분 (흰색)
                String commandText = line.substring(prompt.length());
                context.drawTextWithShadow(textRenderer, commandText, startX, y, 0xFFFFFFFF);
            }
            // 일반 출력 (흰색)
            else {
                context.drawTextWithShadow(textRenderer, line, startX, y, 0xFFFFFFFF);
            }

            y += textRenderer.fontHeight + 2;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}