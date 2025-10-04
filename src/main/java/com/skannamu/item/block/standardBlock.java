// src/main/java/com/skannamu/item/block/standardBlock.java (최종 수정 전문)

package com.skannamu.item.block;

import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.network.UrlScreenOpenPayload; // ⚡️ 패킷 Import 추가
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking; // ⚡️ 패킷 전송을 위한 Import
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity; // 캐스팅을 위한 Import
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
        if (!world.isClient) {
            // ⚡️ 서버 측 로직: 권한 확인 (OK)
            boolean isActive = ServerCommandProcessor.isPlayerActive(player.getUuid());

            if (isActive) {
                // ⚡️ 활성화된 경우: 클라이언트에게 UI를 열라는 패킷을 보냅니다.
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new UrlScreenOpenPayload());
                }
                return ActionResult.SUCCESS;
            } else {
                // 비활성화된 경우: 접근 거부 메시지 전송 (OK)
                player.sendMessage(
                        Text.literal("§c[ACCESS DENIED]: System requires activation key. Use the portable terminal (key <code>)."),
                        true
                );
                return ActionResult.CONSUME;
            }
        }

        // ⚡️ 클라이언트 측 로직: 아무것도 하지 않고 서버의 명령(패킷)을 기다립니다.
        // 기존의 오류를 일으키는 isActive 확인 로직을 제거했습니다.
        return ActionResult.PASS;
    }
}