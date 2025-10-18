package com.skannamu.client.gui;

import com.skannamu.item.block.VaultBlockEntity;
import com.skannamu.network.VaultSliderPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
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

        // 💡 정답 입력 버튼
        submitButton = ButtonWidget.builder(
                        Text.literal("정답 입력"),
                        button -> handleSubmit())
                .dimensions(this.width / 2 - 100, this.height / 2 + 50, 200, 20)
                .build();
        this.addDrawableChild(submitButton);

        // 닫기 버튼
        closeButton = ButtonWidget.builder(
                        Text.literal("닫기"),
                        button -> this.client.setScreen(null))
                .dimensions(this.width / 2 - 100, this.height / 2 + 80, 200, 20)
                .build();
        this.addDrawableChild(closeButton);
    }

    private void initInventoryMode() {
        // 닫기 버튼만 재배치
        closeButton = ButtonWidget.builder(
                        Text.literal("닫기"),
                        button -> this.client.setScreen(null))
                .dimensions(this.width / 2 - 50, inventoryY + 60, 100, 20)
                .build();
        this.addDrawableChild(closeButton);
    }

    // 💡 정답 입력 버튼 클릭 시 서버로 정답 대조 요청 패킷 전송
    private void handleSubmit() {
        BlockPos pos = blockEntity.getPos();

        // 서버로 SLIDER_SUBMIT_ID 패킷 전송
        PacketByteBuf buf = ClientPlayNetworking.createPacket(VaultSliderPayload.SLIDER_SUBMIT_ID, buffer -> {
            buffer.writeBlockPos(pos);
        });
        ClientPlayNetworking.send(buf);

        // UI를 다시 초기화하여 서버의 상태 변화(isVaultCorrect)를 확인하고 화면을 전환합니다.
        this.init();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

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

        // 3x3 인벤토리 칸 배경 렌더링
        int size = 9;
        int slotSize = 18;
        int gridWidth = 3 * slotSize;
        int gridHeight = 3 * slotSize;

        // 인벤토리 배경 박스
        context.fill(inventoryX, inventoryY, inventoryX + gridWidth, inventoryY + gridHeight, 0xFF444444);

        // 아이템 슬롯 및 아이템 렌더링
        for (int i = 0; i < size; i++) {
            int row = i / 3;
            int col = i % 3;

            int x = inventoryX + col * slotSize;
            int y = inventoryY + row * slotSize;

            context.drawBorder(x, y, slotSize, slotSize, 0xFFFFFFFF);

            ItemStack stack = blockEntity.getInventory().get(i);
            context.drawItem(stack, x + 1, y + 1);
            context.drawItemInSlot(this.textRenderer, stack, x + 1, y + 1);
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    // --- 커스텀 슬라이더 클래스 (유지) ---
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

            PacketByteBuf buf = ClientPlayNetworking.createPacket(VaultSliderPayload.SLIDER_UPDATE_ID, buffer -> {
                buffer.writeBlockPos(this.blockPos);
                buffer.writeInt(this.sliderIndex);
                buffer.writeInt(value);
            });
            ClientPlayNetworking.send(buf);
        }
    }
}