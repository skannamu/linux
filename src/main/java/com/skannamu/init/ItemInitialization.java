package com.skannamu.init;

import com.skannamu.skannamuMod;
import com.skannamu.item.tool.PortableTerminalItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey; // 👈 import 추가
import net.minecraft.registry.RegistryKeys; // 👈 import 추가
import net.minecraft.util.Identifier;

public class ItemInitialization {

    public static Item PORTABLE_TERMINAL;

    public static void initializeItems() {

        String terminalName = "portable_terminal";
        Identifier terminalId = Identifier.of(skannamuMod.MOD_ID, terminalName);
        RegistryKey<Item> terminalKey = RegistryKey.of(RegistryKeys.ITEM, terminalId);

        Item.Settings terminalSettings = new Item.Settings()
                .registryKey(terminalKey)
                .maxCount(1);

        PORTABLE_TERMINAL = Registry.register(
                Registries.ITEM,
                terminalId,
                new PortableTerminalItem(terminalSettings)
        );

        skannamuMod.LOGGER.info("Initializing Mod Items for " + skannamuMod.MOD_ID);
    }
}