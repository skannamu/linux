package com.skannamu.item.block;

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
    protected void appendProperties(net.minecraft.state.StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VaultBlockEntity(pos, state);
    }

    @Override
    public net.minecraft.block.BlockRenderType getRenderType(BlockState state) {
        return net.minecraft.block.BlockRenderType.MODEL;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // [서버 로직] - 안내 메시지 및 상태 확인
        if (!world.isClient) {
            if (state.get(OPEN)) {
                player.sendMessage(Text.literal("§aThe Vault is open. Accessing loot..."), true);
            } else {
                player.sendMessage(Text.literal("§7Opening Vault Terminal..."), true);
            }
            return ActionResult.SUCCESS;
        }
        // [클라이언트 로직] - GUI 표시
        else {
            if (world.getBlockEntity(pos) instanceof VaultBlockEntity entity) {
                // 금고가 열려있든 닫혀있든 GUI를 띄웁니다. (열려있으면 인벤토리 모드가 뜸)
                MinecraftClient.getInstance().setScreen(new VaultScreen(entity));
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }
    }
}