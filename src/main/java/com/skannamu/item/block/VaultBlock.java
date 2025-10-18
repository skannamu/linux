package com.skannamu.item.block;

import com.skannamu.server.DataLoader;
import com.skannamu.server.MissionData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VaultBlock extends HorizontalFacingBlock {

    public static final BooleanProperty OPEN = BooleanProperty.of("open");
    public static final IntProperty DIAL_VALUE = IntProperty.of("dial_value", 0, 99);
    public static final IntProperty PROGRESS_STEP = IntProperty.of("progress_step", 0, 10);

    public VaultBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(OPEN, false)
                .with(DIAL_VALUE, 0)
                .with(PROGRESS_STEP, 0)
                .with(FACING, net.minecraft.util.math.Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN, DIAL_VALUE, PROGRESS_STEP, FACING);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) { return ActionResult.SUCCESS; }

        boolean isOpen = state.get(OPEN);
        if (isOpen) {
            player.sendMessage(Text.literal("§aThe Vault is already open."), true);
            return ActionResult.CONSUME;
        }

        MissionData.VaultSettings settings = DataLoader.INSTANCE.getVaultSettings();
        if (settings == null || settings.correct_sequence == null || settings.correct_sequence.isEmpty()) {
            player.sendMessage(Text.literal("§c[ERROR] Vault configuration is missing on the server."), true);
            return ActionResult.CONSUME;
        }

        List<Integer> correctSequence = settings.correct_sequence;
        int maxDialValue = settings.max_dial_value > 0 ? settings.max_dial_value : 99;

        int currentValue = state.get(DIAL_VALUE);
        int currentStep = state.get(PROGRESS_STEP);
        // 다음 다이얼 값은 현재 값에서 1 증가하고 maxDialValue + 1로 나눈 나머지입니다.
        int nextValue = (currentValue + 1) % (maxDialValue + 1);

        // 1. 다이얼 사운드 재생
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK, SoundCategory.BLOCKS, 0.5F, 1.2F + (nextValue * 0.01F));

        // 2. 현재 단계 정답 확인
        if (currentStep < correctSequence.size()) {
            int requiredCode = correctSequence.get(currentStep);

            if (nextValue == requiredCode) {
                // 💡 [정답 처리] 다음 단계로 진행 (혹은 최종 성공)
                int nextStep = currentStep + 1;

                if (nextStep == correctSequence.size()) {
                    // 최종 정답!
                    world.setBlockState(pos, state.with(OPEN, true).with(DIAL_VALUE, nextValue).with(PROGRESS_STEP, nextStep), Block.NOTIFY_ALL);
                    world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1.0F, 0.9F);
                    player.sendMessage(Text.literal("§e[VAULT OPENED] All codes accepted!"), true);
                    return ActionResult.CONSUME;
                } else {
                    // 중간 단계 정답
                    world.setBlockState(pos, state.with(DIAL_VALUE, nextValue).with(PROGRESS_STEP, nextStep), Block.NOTIFY_LISTENERS);
                    world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 0.8F, 1.5F);
                    player.sendMessage(Text.literal("§aCode accepted. Set the next dial (" + (nextStep + 1) + "/" + correctSequence.size() + ")"), true);
                    return ActionResult.CONSUME;
                }
            } else if (currentValue == requiredCode) {
                // 💡 [오답 초기화] 현재 값이 requiredCode였는데, nextValue로 넘어가면서 정답을 지나쳤을 경우

                // 진행 단계 초기화
                world.setBlockState(pos, state.with(DIAL_VALUE, nextValue).with(PROGRESS_STEP, 0), Block.NOTIFY_ALL);
                world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 1.0F, 0.5F);
                player.sendMessage(Text.literal("§cSequence broken! Starting over..."), true);

                // 플레이어에게 현재 다이얼 값을 보여주면서 초기화 메시지 포함
                player.sendMessage(Text.literal("§7Dial set to: " + nextValue + " (Required: " + (correctSequence.get(0)) + ")"), false);
                return ActionResult.CONSUME;
            }
        }

        // 3. 정답이 아니며, 초기화 조건에도 해당하지 않으면 다이얼 값만 업데이트 (PROGRESS_STEP 유지)
        world.setBlockState(pos, state.with(DIAL_VALUE, nextValue), Block.NOTIFY_LISTENERS);

        // 4. 현재 다이얼 값을 플레이어에게 보여줍니다. (초기화되지 않은 경우)
        // 오답으로 초기화되면 위에서 메시지를 보냈으므로, 여기서는 현재 단계의 요구 코드를 표시합니다.
        player.sendMessage(Text.literal("§7Dial set to: " + nextValue + " (Required: " + (correctSequence.get(currentStep)) + ")"), true);

        return ActionResult.CONSUME;
    }
}