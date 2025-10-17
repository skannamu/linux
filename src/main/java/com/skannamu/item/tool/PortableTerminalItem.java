package com.skannamu.item.tool;

import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.client.ClientHackingState; // 💡 Hacked 상태 체크를 위한 클라이언트 상태 임포트
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.text.Text;

public class PortableTerminalItem extends Item {

    public PortableTerminalItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {

            if (ClientHackingState.isTerminalHacked()) {
                player.sendMessage(Text.literal("Terminal locked. System overloaded (EMP effect)."), true);
                return ActionResult.FAIL;
            }

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