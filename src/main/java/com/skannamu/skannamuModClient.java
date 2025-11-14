package com.skannamu;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;


public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;
    private static final Identifier SHADER_RELOADER_ID = Identifier.of(skannamuMod.MOD_ID, "exploit_shader_reloader");

    @Override
    public void onInitializeClient() {
        skannamuMod.LOGGER.info("skannamuMod Client initialized!");


    }
}
