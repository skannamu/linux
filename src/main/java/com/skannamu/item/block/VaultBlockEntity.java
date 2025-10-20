package com.skannamu.item.block;

import com.mojang.serialization.MapCodec;
import com.skannamu.init.VaultBlockEntities;
import com.skannamu.server.DataLoader;
import com.skannamu.server.MissionData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VaultBlockEntity extends BlockEntity implements SidedInventory {

    private static final int INVENTORY_SIZE = 9;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int[] sliderValues = new int[]{0, 0, 0};
    private boolean isVaultCorrect = false;

    public VaultBlockEntity(BlockPos pos, BlockState state) {
        super(VaultBlockEntities.VAULT_BLOCK_ENTITY_TYPE, pos, state);
    }

    public void updateSliderValue(int index, int newValue) {
        if (this.world == null || this.world.isClient || isVaultCorrect || index < 0 || index >= 3) {
            return;
        }

        if (sliderValues[index] != newValue) {
            sliderValues[index] = newValue;
            markDirty();
        }
    }

    public boolean checkAndOpenVault(PlayerEntity player) {
        if (this.world == null || this.world.isClient || isVaultCorrect) {
            return false;
        }

        MissionData.VaultSettings settings = DataLoader.INSTANCE.getVaultSettings();
        if (settings == null || settings.correct_values == null || settings.correct_values.size() != 3) {
            return false;
        }

        List<Integer> correctValues = new ArrayList<>(settings.correct_values);
        List<Integer> currentValues = new ArrayList<>();
        currentValues.add(sliderValues[0]);
        currentValues.add(sliderValues[1]);
        currentValues.add(sliderValues[2]);

        Collections.sort(correctValues);
        Collections.sort(currentValues);

        if (correctValues.equals(currentValues)) {
            isVaultCorrect = true;

            // 보상을 플레이어 인벤토리에 지급
            if (!player.getInventory().insertStack(new ItemStack(Items.DIAMOND_SWORD))) {
                // 공간이 없으면 월드에 드롭
                Block.dropStack(this.world, pos, new ItemStack(Items.DIAMOND_SWORD));
            }

            if (this.world != null) {
                this.world.setBlockState(pos, this.world.getBlockState(pos).with(VaultBlock.OPEN, true), Block.NOTIFY_ALL);
                this.world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.9F);
                markDirty();
                world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
                player.sendMessage(net.minecraft.text.Text.literal("§a금고가 열렸습니다! 보상이 지급되었습니다."), false);
            }
            return true;
        } else {
            // 실패 시 슬라이더 초기화
            for (int i = 0; i < 3; i++) sliderValues[i] = 0;
            markDirty();
            if (world != null) {
                world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
                world.playSound(
                        null,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                        net.minecraft.sound.SoundCategory.BLOCKS,
                        1.0F,
                        0.8F
                );
            }
            return false;
        }
    }

    public DefaultedList<ItemStack> getInventory() { return inventory; }
    public int[] getSliderValues() { return sliderValues; }
    public boolean isVaultCorrect() { return isVaultCorrect; }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        NbtCompound nbt = new NbtCompound();
        nbt.putIntArray("SliderValues", this.sliderValues);
        nbt.putBoolean("IsVaultCorrect", this.isVaultCorrect);
        Inventories.writeData(view, this.inventory);
        NbtComponent nbtComponent = NbtComponent.of(nbt);
        MapCodec<NbtComponent> nbtMapCodec = DataComponentTypes.BLOCK_ENTITY_DATA.getCodec().fieldOf("nbt_data");
        view.put(nbtMapCodec, nbtComponent);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        Inventories.readData(view, this.inventory);
        MapCodec<NbtComponent> nbtMapCodec = DataComponentTypes.BLOCK_ENTITY_DATA.getCodec().fieldOf("nbt_data");
        Optional<NbtComponent> optionalComponent = view.read(nbtMapCodec);
        if (optionalComponent.isPresent()) {
            NbtCompound nbt = optionalComponent.get().copyNbt();
            this.sliderValues = nbt.getIntArray("SliderValues").orElseGet(() -> {
                return new int[]{0, 0, 0};
            });
            this.isVaultCorrect = nbt.getBoolean("IsVaultCorrect").orElse(false);
        } else {
            this.sliderValues = new int[]{0, 0, 0};
            this.isVaultCorrect = false;
        }
    }

    @Override public int size() { return INVENTORY_SIZE; }
    @Override public boolean isEmpty() {
        for (ItemStack stack : inventory) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { return Inventories.splitStack(this.inventory, slot, amount); }
    @Override public ItemStack removeStack(int slot) { return Inventories.removeStack(this.inventory, slot); }
    @Override public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) stack.setCount(this.getMaxCountPerStack());
    }
    @Override public boolean canPlayerUse(PlayerEntity player) { return true; }
    @Override public boolean isValid(int slot, ItemStack stack) { return true; }
    @Override public void clear() { this.inventory.clear(); }
    @Override public void onOpen(PlayerEntity player) {}
    @Override public void onClose(PlayerEntity player) {}

    @Override public int[] getAvailableSlots(Direction side) { return new int[]{0,1,2,3,4,5,6,7,8}; }
    @Override public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) { return true; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return true; }
}