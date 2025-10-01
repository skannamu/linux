package com.skannamu.item.block;

import net.minecraft.block.Block;

public class standardBlock extends Block {

    public standardBlock() {
        super(Block.Settings.create().strength(1.5f).requiresTool());
    }
}
