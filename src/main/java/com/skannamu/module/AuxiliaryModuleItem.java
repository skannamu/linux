package com.skannamu.module;

import com.skannamu.network.ModuleActivationPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.text.Text;

public class AuxiliaryModuleItem extends Item {
    public AuxiliaryModuleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {
            // 클라이언트: 서버로 모듈 활성화 페이로드 전송
            ClientPlayNetworking.send(new ModuleActivationPayload("auxiliary"));
            player.sendMessage(Text.literal("§a[Terminal] Auxiliary module activated! Use 'auxiliary -h' in terminal."), true);
            return new ActionResult.Success(ActionResult.SwingSource.CLIENT, new ActionResult.ItemContext(true, null));
        }
        return ActionResult.PASS;
    }
}