package com.skannamu.item.block;

import net.minecraft.block.Block;
import com.skannamu.client.gui.UrlInputScreen;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class standardBlock extends Block {

    public standardBlock(Settings settings) {
        super(settings);
    }
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){

        System.out.println("--- STANDARD BLOCK: onUse CALLED (NEW SIG) ---"); // 디버그용으로 만듦.. 이제 필요없을듯

        if (!world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (world.isClient) {
            net.minecraft.client.MinecraftClient.getInstance().setScreen(new UrlInputScreen());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}