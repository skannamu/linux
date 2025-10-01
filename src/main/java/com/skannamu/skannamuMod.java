package com.skannamu;

import com.skannamu.init.BlockInitialization;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BlockInitialization.initializeBlocks(); LOGGER.info("skannamuMod initialized!");
    }
}
