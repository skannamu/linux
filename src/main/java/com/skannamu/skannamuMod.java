package com.skannamu;

import com.skannamu.init.BlockInitialization;
import net.fabricmc.api.ModInitializer;

public class skannamuMod implements ModInitializer{
    @Override
    public void onInitialize() {
        BlockInitialization.register();
    }
}