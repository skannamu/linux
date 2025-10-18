package com.skannamu.item.block;

import com.skannamu.init.VaultBlockEntities;
import com.skannamu.server.DataLoader;
import com.skannamu.server.MissionData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// SidedInventory를 상속받아 인벤토리 기능을 추가합니다.
public class VaultBlockEntity extends BlockEntity implements SidedInventory {

    private static final int INVENTORY_SIZE = 9; // 3x3 인벤토리 크기
    // 💡 모든 상태는 서버 RAM에만 저장됩니다.
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int[] sliderValues = new int[]{0, 0, 0};
    private boolean isVaultCorrect = false;

    public VaultBlockEntity(BlockPos pos, BlockState state) {
        super(VaultBlockEntities.VAULT_BLOCK_ENTITY_TYPE, pos, state);
    }

    // --- NBT 저장 및 로드 (RAM 전용이므로 비워둠) ---
    @Override
    protected void writeNbt(NbtCompound nbt) {}

    @Override
    public void readNbt(NbtCompound nbt) {}

    // 💡 슬라이더 값만 업데이트 (정답 대조는 하지 않음)
    public void updateSliderValue(int index, int newValue) {
        if (this.world == null || this.world.isClient || isVaultCorrect || index < 0 || index >= 3) {
            return;
        }

        if (sliderValues[index] != newValue) {
            sliderValues[index] = newValue;
        }
    }

    /**
     * 정답 입력 버튼을 눌렀을 때 서버에서 호출되는 메서드
     */
    public boolean checkAndOpenVault() {
        if (this.world == null || this.world.isClient || isVaultCorrect) {
            return false;
        }

        MissionData.VaultSettings settings = DataLoader.INSTANCE.getVaultSettings();
        if (settings == null || settings.correct_values == null || settings.correct_values.size() != 3) {
            return false;
        }

        List<Integer> correctValues = new ArrayList<>(settings.correct_values);
        List<Integer> currentValues = List.of(sliderValues[0], sliderValues[1], sliderValues[2]);

        Collections.sort(correctValues);
        Collections.sort(currentValues);

        if (correctValues.equals(currentValues)) {
            // 1. 상태 변경 (RAM 저장)
            isVaultCorrect = true;

            // 2. 인벤토리 초기 아이템 설정 (다이아몬드 검) (RAM 저장)
            this.inventory.set(4, new ItemStack(Items.DIAMOND_SWORD)); // 중앙 슬롯 (5번째 슬롯)

            // 3. 블록 상태 변경 및 사운드
            if (this.world != null) {
                this.world.setBlockState(pos, this.world.getBlockState(pos).with(VaultBlock.OPEN, true), Block.NOTIFY_ALL);
                this.world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.9F);
            }
            return true;
        } else {
            return false;
        }
    }

    // --- Inventory Interface Methods (RAM 전용) ---
    @Override
    public int size() { return INVENTORY_SIZE; }
    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) { if (!stack.isEmpty()) { return false; } } return true;
    }

    public DefaultedList<ItemStack> getInventory() { return inventory; }
    public int[] getSliderValues() { return sliderValues; }
    public boolean isVaultCorrect() { return isVaultCorrect; }

    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = Inventories.splitStack(this.inventory, slot, amount); return result; }
    @Override public ItemStack removeItem(int slot) { return Inventories.removeStack(this.inventory, slot); }
    @Override public void setStack(int slot, ItemStack stack) { this.inventory.set(slot, stack); if (stack.getCount() > this.getMaxCountPerStack()) { stack.setCount(this.getMaxCountPerStack()); } }
    @Override public boolean isValid(int slot, ItemStack stack) { return true; }
    @Override public void clear() { this.inventory.clear(); }
    @Override public void onOpen(net.minecraft.entity.player.PlayerEntity player) {}
    @Override public void onClose(net.minecraft.entity.player.PlayerEntity player) {}

    // SidedInventory 구현
    @Override public int[] getAvailableSlots(Direction side) { return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}; }
    @Override public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) { return true; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return true; }
}