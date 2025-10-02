package com.skannamu.tooltip;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class standardBlockTooltip extends BlockItem {

    public standardBlockTooltip(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, java.util.List<Text> tooltip, Object context) {
        // 기본 설명 툴팁
        tooltip.add(Text.translatable("tooltip.skannamu.standard_block.line1")
                .formatted(Formatting.GRAY));

        // Shift 키를 눌렀을 때 상세 정보 표시 (Screen.hasShiftDown() 사용)
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("tooltip.skannamu.standard_block.detail_on")
                    .formatted(Formatting.AQUA));
            tooltip.add(Text.translatable("tooltip.skannamu.standard_block.line_extra")
                    .formatted(Formatting.AQUA));
        } else {
            // Shift 키 안내 메시지
            tooltip.add(Text.translatable("tooltip.skannamu.standard_block.shift_prompt")
                    .formatted(Formatting.DARK_GRAY));
        }
    }
}