// PortableTerminalItem.java (수정버전)
package com.skannamu.item.tool;

import com.skannamu.client.gui.TerminalScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PortableTerminalItem extends Item {

    public PortableTerminalItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {
            openTerminalScreen();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
    @Environment(EnvType.CLIENT)
    private void openTerminalScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new TerminalScreen());
    }
}
