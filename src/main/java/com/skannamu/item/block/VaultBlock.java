package com.skannamu.item.block;

import com.skannamu.init.VaultBlockEntities;
import com.mojang.serialization.MapCodec;
import com.skannamu.client.gui.VaultScreen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VaultBlock extends BlockWithEntity {

    public static final net.minecraft.state.property.BooleanProperty OPEN = net.minecraft.state.property.BooleanProperty.of("open");

    public VaultBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(OPEN, false));
    }
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return Block.createCodec(VaultBlock::new);
    }
    @Override
    protected void appendProperties(net.minecraft.state.StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VaultBlockEntity(pos, state);
    }
    public net.minecraft.block.entity.BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return VaultBlockEntities.VAULT_BLOCK_ENTITY_TYPE;
    }

    @Override
    public net.minecraft.block.BlockRenderType getRenderType(BlockState state) {
        return net.minecraft.block.BlockRenderType.MODEL;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof VaultBlockEntity entity) {
            // 💡 금고가 이미 정답 상태라면 (비활성화)
            if (entity.isVaultCorrect()) {
                if (!world.isClient) {
                    player.sendMessage(Text.literal("§c이 금고는 이미 해제되었습니다."), true);
                }
                return ActionResult.FAIL; // 서버와 클라이언트 모두 FAIL/CONSUME로 처리하여 UI를 열지 않음
            }

            if (!world.isClient) {
                if (state.get(OPEN)) {
                    player.sendMessage(Text.literal("§aThe Vault is open. Accessing loot..."), true);
                } else {
                    player.sendMessage(Text.literal("§7Opening Vault Terminal..."), true);
                }
                return ActionResult.SUCCESS;
            }
            else {
                MinecraftClient.getInstance().setScreen(new VaultScreen(entity));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.CONSUME;
    }
}