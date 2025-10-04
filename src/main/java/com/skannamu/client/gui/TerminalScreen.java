// src/main/java/com/skannamu/client/gui/TerminalScreen.java (오류 수정)

package com.skannamu.client.gui;

import com.skannamu.network.TerminalCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TerminalScreen extends Screen {

    private TextFieldWidget inputField;
    private final List<String> outputLines = new ArrayList<>();

    public TerminalScreen() {
        super(Text.literal("Portable Terminal"));
    }

    @Override
    protected void init() {
        int inputHeight = 20;
        int padding = 4;

        // 입력 필드 초기화
        this.inputField = new TextFieldWidget(
                this.textRenderer,
                padding,
                this.height - inputHeight - padding,
                this.width - (padding * 2),
                inputHeight,
                Text.literal("")
        );
        this.inputField.setMaxLength(256);
        this.inputField.setDrawsBackground(true);
        this.inputField.setEditable(true);
        this.inputField.setFocusUnlocked(false);

        // ⚡️ 오류 수정: setTextFieldFocused -> setFocused
        this.inputField.setFocused(true);

        this.addSelectableChild(this.inputField);
    }

    private void handleCommand(String command) {
        outputLines.add(Text.literal("> ").formatted(Formatting.GREEN).getString() + command);

        if (command.isEmpty()) {
            return;
        }

        TerminalCommandPayload payload = new TerminalCommandPayload(command);
        ClientPlayNetworking.send(payload);

        this.inputField.setText("");

        if (outputLines.size() > 100) {
            outputLines.remove(0);
        }
    }

    public void appendOutput(String output) {
        outputLines.add(output);

        if (outputLines.size() > 100) {
            outputLines.remove(0);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter or Numpad Enter
            String command = this.inputField.getText();
            this.handleCommand(command);
            return true;
        }

        if (keyCode == 256) { // Esc key
            this.client.setScreen((Screen)null);
            return true;
        }

        // 입력 필드에 포커스가 있다면, 입력 필드가 키 이벤트를 먼저 처리하도록 합니다.
        if (this.inputField.isActive() && this.inputField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ⚡️ 오류 수정: renderBackground 시그니처 수정 (4개 매개변수)
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 배경 렌더링을 renderBackground로 분리했으므로, 여기서 다시 호출할 필요는 없습니다.
        // super.render(context, mouseX, mouseY, delta);

        this.inputField.render(context, mouseX, mouseY, delta);

        // 출력 영역 렌더링
        int x = 4;
        // 텍스트 출력 시작 위치는 아래에서 위로 쌓이도록 조정 (선택적)
        int startY = this.height - 24 - (textRenderer.fontHeight * outputLines.size());

        for (int i = 0; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            String clippedLine = textRenderer.trimToWidth(line, this.width - 8);
            context.drawTextWithShadow(textRenderer, clippedLine, x, startY + (i * textRenderer.fontHeight), 0xFFFFFF);
        }

        // super.render 호출은 최종적으로 호출하는 것이 일반적입니다.
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}