package com.skannamu.module;

import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.TerminalCommands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult; // TypedActionResult 대신 ActionResult 사용
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BinaryModuleItem extends Item {

    private final String commandToUnlock;

    public BinaryModuleItem(String commandName, Settings settings) {
        super(settings);
        this.commandToUnlock = commandName.toLowerCase();
    }
    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient()) {
            ServerCommandProcessor.PlayerState state = TerminalCommands.getPlayerState(player.getUuid());

            if (state.isCommandAvailable(commandToUnlock)) {
                player.sendMessage(Text.literal(
                        "[" + commandToUnlock.toUpperCase() + " Module] Error: This module is already installed."), false);
                return ActionResult.FAIL; // 실패 시 ActionResult.FAIL 반환
            }
            state.addCommand(commandToUnlock);
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            player.sendMessage(Text.literal(
                    "[" + commandToUnlock.toUpperCase() + " Module] Installation complete. Command '" + commandToUnlock + "' is now available."), false);

            return ActionResult.SUCCESS; // 성공 시 ActionResult.SUCCESS 반환
        }
        return ActionResult.SUCCESS;
    }
    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.skannamu.module_" + commandToUnlock);
    }
}