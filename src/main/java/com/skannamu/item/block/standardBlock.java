package com.skannamu.item.block;

import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;

public class standardBlock extends Block{
    public standardBlock(){
        super(AbstractBlock.Settings.create().strength(2.0f, 3.0f));

    }
}
