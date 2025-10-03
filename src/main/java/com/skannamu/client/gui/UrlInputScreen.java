package com.skannamu.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class UrlInputScreen extends Screen {

    private TextFieldWidget urlField;

    // 화면 제목을 설정
    public UrlInputScreen() {
        super(Text.literal("웹사이트 주소 입력"));
    }

    @Override
    protected void init() {

        this.urlField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 100, // 화면 중앙 - 100
                this.height / 2 - 20, // 화면 중앙 - 20
                200, // 너비 200
                20,  // 높이 20
                Text.literal("URL 입력 필드")
        );
        this.urlField.setMaxLength(1024); // 최대 길이 설정
        this.urlField.setText("http://"); // 기본값 설정
        this.addSelectableChild(this.urlField); // 화면에 추가


        ButtonWidget openButton = ButtonWidget.builder(
                        Text.literal("웹사이트로 이동"),
                        button -> {
                            String url = this.urlField.getText();

                            openUrl(url);

                            MinecraftClient.getInstance().setScreen(null);
                        })
                .dimensions(this.width / 2 - 100, this.height / 2 + 10, 200, 20)
                .build();
        this.addDrawableChild(openButton);


        ButtonWidget closeButton = ButtonWidget.builder(
                        Text.literal("닫기"),
                        button -> MinecraftClient.getInstance().setScreen(null))
                .dimensions(this.width / 2 - 100, this.height / 2 + 40, 200, 20)
                .build();
        this.addDrawableChild(closeButton);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        this.urlField.render(context, mouseX, mouseY, delta); // 입력 필드는 직접 렌더링
        context.drawTextWithShadow(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, 8, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        return this.urlField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean shouldPause() {
        return false;
    }


    private void openUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);

            Util.getOperatingSystem().open(uri);

        } catch (Exception e) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("§chttps://block.xyz/ 올바르지 않은 주소입니다: " + url));
            e.printStackTrace();
        }
    }
}