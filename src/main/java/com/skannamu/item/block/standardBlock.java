package com.skannamu.item.block;

import com.skannamu.client.gui.UrlInputScreen;
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.skannamuModClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class standardBlock extends Block {

    public standardBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            // 클라이언트 측 로직: 로컬 상태 확인 후 UI 열기
            if (skannamuModClient.isPlayerActive) {
                MinecraftClient.getInstance().setScreen(new UrlInputScreen());
                return ActionResult.SUCCESS;
            } else {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                        Text.literal("§c[ACCESS DENIED]: System requires activation key. Use the portable terminal (key <code>).")
                );
                return ActionResult.CONSUME;
            }
        } else {
            boolean isActive = ServerCommandProcessor.isPlayerActive(player.getUuid());

            if (isActive) {
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(
                        Text.literal("§c[ACCESS DENIED]: System requires activation key. Use the portable terminal (key <code>)."),
                        true
                );
                return ActionResult.CONSUME;
            }
        }
    }
}