package com.skannamu.client.gui;

import com.skannamu.network.TerminalCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
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

        // **수정:** 터미널을 열 때, 버퍼를 초기화하고 초기 프롬프트만 추가합니다.
        // appendOutput("prompt") 호출을 제거하고, 대신 outputLines.add(prompt);를 사용합니다.
        // 이렇게 하면 appendOutput의 복잡한 로직을 우회하여 초기 프롬프트만 하나만 확실하게 추가합니다.
        outputLines.clear();
        outputLines.add(prompt);
    }

    public String getPrompt() {
        return prompt; // prompt getter
    }

    private void handleCommand(String command) {
        if (!command.isEmpty()) {
            // 명령어 기록을 '프롬프트 + 입력'으로 버퍼의 마지막 줄을 교체합니다.
            // 기존 로직을 유지하면서, 마지막 줄이 프롬프트인지 확인 후 안전하게 교체합니다.
            if (!outputLines.isEmpty() && outputLines.get(outputLines.size() - 1).equals(prompt)) {
                String commandLine = prompt + currentInput;
                outputLines.set(outputLines.size() - 1, commandLine);
            }

            // 명령 전송
            ClientPlayNetworking.send(new TerminalCommandPayload(command));

            // 다음 프롬프트는 서버 응답을 기다리지 않고 미리 추가합니다.
            outputLines.add(prompt);
        }
    }

    public void appendOutput(String output) {
        // 서버 응답이 올 경우, 미리 추가되어 있던 다음 프롬프트를 제거합니다.
        if (!outputLines.isEmpty() && outputLines.get(outputLines.size() - 1).equals(prompt)) {
            outputLines.remove(outputLines.size() - 1);
        }

        // 1. \n을 기준으로 문자열을 분리하여 각 줄을 outputLines에 추가
        String[] lines = output.split("\\n");
        for (String line : lines) {
            outputLines.add(line);
        }

        // 서버 응답이 추가된 후, 마지막에 새 프롬프트를 추가하여 입력 대기 상태를 만듭니다.
        outputLines.add(prompt);

        // 출력 제한 (100줄 오버플로우 로직 유지)
        while (outputLines.size() > 100) {
            outputLines.remove(0);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String command = currentInput;
            handleCommand(command);
            currentInput = "";
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(null);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            currentInput = "";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr <= 126 && chr != '\n' && chr != '\r') {
            // 화면 너비 제한 (10px 여백 유지)
            int availableWidth = width - 20;
            String currentLineText = prompt + currentInput + chr;
            if (textRenderer.getWidth(currentLineText) < availableWidth) {
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

        final int PADDING_X = 10;
        final int PADDING_Y_TOP = 10;
        final int PADDING_Y_BOTTOM = 10;
        final int LINE_HEIGHT = textRenderer.fontHeight + 2;

        // 화면에 최대로 표시할 수 있는 줄 수를 계산합니다.
        int maxVisibleLines = (height - PADDING_Y_TOP - PADDING_Y_BOTTOM) / LINE_HEIGHT;

        // 렌더링할 시작 인덱스를 계산합니다.
        int startIndex = Math.max(0, outputLines.size() - maxVisibleLines);

        // 렌더링할 Y 좌표를 화면 상단에서부터 시작하도록 설정합니다.
        int currentY = PADDING_Y_TOP;

        // 계산된 시작 인덱스부터 현재 버퍼의 끝까지 순회하며 렌더링합니다.
        for (int i = startIndex; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            int startX = PADDING_X;

            // **1. 현재 입력 중인 마지막 줄**
            if (i == outputLines.size() - 1) {
                // 현재 프롬프트 (녹색)
                context.drawTextWithShadow(textRenderer, prompt, startX, currentY, 0xFF00FF00);
                startX += textRenderer.getWidth(prompt);

                // 입력 텍스트 (흰색)
                context.drawTextWithShadow(textRenderer, currentInput, startX, currentY, 0xFFFFFFFF);

                // 커서 표시
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    startX += textRenderer.getWidth(currentInput);
                    context.drawTextWithShadow(textRenderer, "_", startX, currentY, 0xFFFFFFFF);
                }
            }

            else if (line.startsWith(prompt)) {

                context.drawTextWithShadow(textRenderer, prompt, startX, currentY, 0xFF00FF00);
                startX += textRenderer.getWidth(prompt);

                String commandText = line.substring(prompt.length());
                context.drawTextWithShadow(textRenderer, commandText, startX, currentY, 0xFFFFFFFF);
            }

            else {
                context.drawTextWithShadow(textRenderer, line, startX, currentY, 0xFFFFFFFF);
            }

            currentY += LINE_HEIGHT;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}