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
    private String promptPath = "/"; // 💡 현재 경로만 저장하는 변수 추가 (초기값 '/')
    private String promptPrefix; // 💡 'SKN@<username>' 부분만 저장

    public TerminalScreen() {
        super(Text.literal("Portable Terminal"));
    }

    @Override
    protected void init() {
        // 1. 프롬프트 접두사 (SKN@username)를 초기화합니다.
        // 이 부분은 경로가 바뀌어도 변하지 않습니다.
        promptPrefix = "SKN@" + client.getSession().getUsername() + ":";

        // 2. 초기 경로 설정: 서버에서 최초 접속 시 경로를 받아와야 하지만,
        // 현재는 서버에 처음 접속할 때 'pwd'를 보내서 경로를 받거나,
        // 서버에서 초기 경로를 포함한 응답을 보내야 합니다.
        // 임시로 기본 경로인 '/'로 시작합니다.
        // **나중에 서버에서 경로를 받는 별도의 Payload를 구현해야 합니다.**

        outputLines.clear();
        outputLines.add(getFullPrompt()); // 💡 getFullPrompt()를 사용
    }

    // 💡 경로가 포함된 전체 프롬프트 문자열을 반환하는 헬퍼 메서드
    private String getFullPrompt() {
        // 프롬프트는 'SKN@user:/path# ' 형태를 유지합니다.
        return promptPrefix + promptPath + "# ";
    }

    // 💡 서버의 응답을 받아 현재 경로를 업데이트하는 핵심 메서드
    public void updatePrompt(String newPath) {
        if (newPath == null || newPath.isBlank()) {
            this.promptPath = "/";
        } else {
            this.promptPath = newPath;
        }
    }

    public String getPrompt() {
        return getFullPrompt(); // 💡 getFullPrompt()를 반환
    }

    private void handleCommand(String command) {
        if (!command.isEmpty()) {
            String fullPrompt = getFullPrompt(); // 💡 현재 프롬프트 상태를 가져옵니다.

            // 명령어 기록을 '프롬프트 + 입력'으로 버퍼의 마지막 줄을 교체합니다.
            if (!outputLines.isEmpty() && outputLines.get(outputLines.size() - 1).equals(fullPrompt)) {
                String commandLine = fullPrompt + currentInput;
                outputLines.set(outputLines.size() - 1, commandLine);
            }

            // 명령 전송
            ClientPlayNetworking.send(new TerminalCommandPayload(command));

            // 다음 프롬프트는 서버 응답을 기다리지 않고 미리 추가합니다.
            outputLines.add(fullPrompt); // 💡 getFullPrompt()를 사용
        }
    }

    public void appendOutput(String output) {
        String fullPrompt = getFullPrompt(); // 💡 현재 프롬프트 상태를 가져옵니다.

        // 서버 응답이 올 경우, 미리 추가되어 있던 다음 프롬프트를 제거합니다.
        if (!outputLines.isEmpty() && outputLines.get(outputLines.size() - 1).equals(fullPrompt)) {
            outputLines.remove(outputLines.size() - 1);
        }

        // 💡 1. '@@CWD:'를 포함하는 특수 명령 응답을 처리합니다.
        // 이는 'cd' 명령의 응답이거나, 'pwd' 명령의 응답을 특수하게 인코딩한 경우입니다.
        if (output.startsWith("@@CWD:")) {
            // 경로를 업데이트합니다.
            String newPath = output.substring("@@CWD:".length()).trim();
            updatePrompt(newPath);
            // 이 특수 응답은 화면에 출력하지 않습니다.
        } else {
            // 2. 일반 출력: \n을 기준으로 문자열을 분리하여 각 줄을 outputLines에 추가
            String[] lines = output.split("\\n");
            for (String line : lines) {
                outputLines.add(line);
            }
        }

        // 서버 응답이 추가된 후, 마지막에 새 프롬프트를 추가하여 입력 대기 상태를 만듭니다.
        outputLines.add(getFullPrompt()); // 💡 getFullPrompt()를 사용

        // 출력 제한 (100줄 오버플로우 로직 유지)
        while (outputLines.size() > 100) {
            outputLines.remove(0);
        }
    }

    // ... keyPressed, charTyped, renderBackground 메서드는 그대로 유지 ...

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
            String currentLineText = getFullPrompt() + currentInput + chr;
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
        String fullPrompt = getFullPrompt(); // 💡 렌더링 시에도 현재 프롬프트 상태를 가져옵니다.
        String currentPromptPrefix = this.promptPrefix + this.promptPath + "# "; // 현재 프롬프트 길이 계산용

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
                context.drawTextWithShadow(textRenderer, currentPromptPrefix, startX, currentY, 0xFF00FF00);
                startX += textRenderer.getWidth(currentPromptPrefix);

                // 입력 텍스트 (흰색)
                context.drawTextWithShadow(textRenderer, currentInput, startX, currentY, 0xFFFFFFFF);

                // 커서 표시
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    startX += textRenderer.getWidth(currentInput);
                    context.drawTextWithShadow(textRenderer, "_", startX, currentY, 0xFFFFFFFF);
                }
            }

            // **2. 이전에 입력했던 명령어 줄**
            else if (line.startsWith(fullPrompt)) {

                context.drawTextWithShadow(textRenderer, currentPromptPrefix, startX, currentY, 0xFF00FF00);
                startX += textRenderer.getWidth(currentPromptPrefix);

                String commandText = line.substring(currentPromptPrefix.length());
                context.drawTextWithShadow(textRenderer, commandText, startX, currentY, 0xFFFFFFFF);
            }

            // **3. 서버의 출력 메시지 줄**
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