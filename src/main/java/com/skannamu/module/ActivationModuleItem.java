package com.skannamu.module;

import com.skannamu.network.ModuleActivationPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public abstract class ActivationModuleItem extends Item {

    private final String commandToActivate;
    public ActivationModuleItem(Settings settings, String commandToActivate) {
        super(settings);
        this.commandToActivate = commandToActivate;
    }
    protected String getCommandToActivate() {
        return commandToActivate;
    }
    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {
            return activateModuleOnClient(player, hand);
        }
        return ActionResult.PASS;
    }
    @Environment(EnvType.CLIENT)
    private ActionResult activateModuleOnClient(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }

        ModuleActivationPayload payload = new ModuleActivationPayload(this.commandToActivate);
        ClientPlayNetworking.send(payload);

        player.getItemCooldownManager().set(stack, 20); // 1초 쿨타임 부여

        return ActionResult.SUCCESS;
    }
}