package com.skannamu.item.tool;

import com.skannamu.client.gui.TerminalScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult; // ⚡️ 이 클래스를 Enum이라고 가정하고 수정
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PortableTerminalItem extends Item {

    public PortableTerminalItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {

        if (world.isClient()) {
            MinecraftClient client = MinecraftClient.getInstance();

            client.setScreen(new TerminalScreen());

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}