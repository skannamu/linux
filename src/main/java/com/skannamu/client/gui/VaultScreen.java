package com.skannamu.client.gui;

import com.skannamu.item.block.VaultBlockEntity;
import com.skannamu.network.VaultSliderPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class VaultScreen extends Screen {

    private final VaultBlockEntity blockEntity;
    private VaultSlider[] sliders = new VaultSlider[3];
    private ButtonWidget submitButton;
    private ButtonWidget closeButton;

    private int inventoryX;
    private int inventoryY;

    public VaultScreen(VaultBlockEntity blockEntity) {
        super(Text.literal("금고 다이얼 시스템"));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        this.clearChildren();

        inventoryX = this.width / 2 - 81;
        inventoryY = this.height / 2 - 18;

        if (!blockEntity.isVaultCorrect()) {
            initSliderMode();
        } else {
            initInventoryMode();
        }
    }

    private void initSliderMode() {
        int startY = this.height / 2 - 50;
        int[] initialValues = blockEntity.getSliderValues();

        for (int i = 0; i < 3; i++) {
            double initialProgress = (double) initialValues[i] / 99.0;

            VaultSlider slider = new VaultSlider(
                    this.width / 2 - 100,
                    startY + i * 30,
                    200, 20,
                    Text.literal("Dial " + (i + 1)),
                    initialProgress,
                    i, blockEntity.getPos()
            );
            sliders[i] = slider;
            this.addDrawableChild(slider);
        }

        submitButton = ButtonWidget.builder(
                        Text.literal("정답 입력"),
                        button -> handleSubmit())
                .dimensions(this.width / 2 - 100, this.height / 2 + 50, 200, 20)
                .build();
        this.addDrawableChild(submitButton);

        closeButton = ButtonWidget.builder(
                        Text.literal("닫기"),
                        button -> this.client.setScreen(null))
                .dimensions(this.width / 2 - 100, this.height / 2 + 80, 200, 20)
                .build();
        this.addDrawableChild(closeButton);
    }

    private void initInventoryMode() {
        closeButton = ButtonWidget.builder(
                        Text.literal("닫기"),
                        button -> this.client.setScreen(null))
                .dimensions(this.width / 2 - 50, inventoryY + 60, 100, 20)
                .build();
        this.addDrawableChild(closeButton);
    }

    private void handleSubmit() {
        BlockPos pos = blockEntity.getPos();
        // ✅ 서버로 제출 패킷 전송 (sliderIndex == -1 → submit 모드)
        ClientPlayNetworking.send(new VaultSliderPayload(pos, -1, 0));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hasInventoryButton = this.children().stream()
                .filter(e -> e instanceof ClickableWidget)
                .map(e -> (ClickableWidget) e)
                .anyMatch(w -> w.getMessage().getString().equals("닫기") && w.getHeight() == 20);

        boolean hasSliderButton = this.children().stream()
                .filter(e -> e instanceof ClickableWidget)
                .map(e -> (ClickableWidget) e)
                .anyMatch(w -> w.getMessage().getString().equals("정답 입력"));

        if (blockEntity.isVaultCorrect() && !hasInventoryButton) {
            this.init(); // 금고 열림 상태면 인벤토리 모드로 전환
        } else if (!blockEntity.isVaultCorrect() && !hasSliderButton) {
            this.init(); // 닫힌 상태면 다이얼 모드로 전환
        }

        if (blockEntity.isVaultCorrect()) {
            renderInventoryMode(context);
        } else {
            renderSliderMode(context);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSliderMode(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    private void renderInventoryMode(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§a[ACCESS GRANTED] Vault Unlocked!"), this.width / 2, 8, 0x00FF00);

        int size = 9;
        int slotSize = 18;
        int gridWidth = 3 * slotSize;
        int gridHeight = 3 * slotSize;

        context.fill(inventoryX, inventoryY, inventoryX + gridWidth, inventoryY + gridHeight, 0xFF444444);

        for (int i = 0; i < size; i++) {
            int row = i / 3;
            int col = i % 3;

            int x = inventoryX + col * slotSize;
            int y = inventoryY + row * slotSize;

            context.drawBorder(x, y, slotSize, slotSize, 0xFFFFFFFF);

            ItemStack stack = blockEntity.getInventory().get(i);
            context.drawItem(stack, x + 1, y + 1);

            if (!stack.isEmpty() && stack.getCount() > 1) {
                String count = String.valueOf(stack.getCount());
                context.drawText(this.textRenderer, count, x + 17 - this.textRenderer.getWidth(count), y + 9, 0xFFFFFF, true);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private class VaultSlider extends SliderWidget {
        private final int sliderIndex;
        private final BlockPos blockPos;

        public VaultSlider(int x, int y, int width, int height, Text text, double progress, int index, BlockPos pos) {
            super(x, y, width, height, text, progress);
            this.sliderIndex = index;
            this.blockPos = pos;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            int value = (int) Math.round(this.value * 99);
            this.setMessage(Text.literal("Dial " + (sliderIndex + 1) + ": " + value));
        }

        @Override
        protected void applyValue() {
            int value = (int) Math.round(this.value * 99);
            ClientPlayNetworking.send(new VaultSliderPayload(blockPos, sliderIndex, value));
        }
    }
}
