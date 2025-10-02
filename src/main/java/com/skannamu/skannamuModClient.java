package com.skannamu;

import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class skannamuModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {

                lines.add(Text.translatable("tooltip.skannamu.standard_block.line1").formatted(Formatting.GRAY));

                if (Screen.hasShiftDown()) {
                    lines.add(Text.translatable("tooltip.skannamu.standard_block.detail_on").formatted(Formatting.AQUA));
                    lines.add(Text.translatable("tooltip.skannamu.standard_block.line_extra").formatted(Formatting.AQUA));
                } else {

                    lines.add(Text.translatable("tooltip.skannamu.standard_block.shift_prompt")
                            .formatted(Formatting.DARK_GRAY)
                            .formatted(Formatting.ITALIC));
                }
            }
        });
    }
}