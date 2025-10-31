package com.skannamu.init;

import com.skannamu.module.AuxiliaryModuleItem;
import com.skannamu.module.ExploitModuleItem;
import com.skannamu.item.tool.PortableTerminalItem;
import com.skannamu.skannamuMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import com.skannamu.item.weapon.NanoBladeItem;

public class ModItems {

    public static Item CAT_MODULE;
    public static Item DECRYPT_MODULE;
    public static Item KEY_MODULE;
    // 💡 ECHO_MODULE 필드 추가
    public static Item ECHO_MODULE;
    public static Item PORTABLE_TERMINAL;
    // Exploit 계열

    private static Item registerBinaryModule(String name, String commandName) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new com.skannamu.module.BinaryModuleItem(commandName,
                new Item.Settings().maxCount(1).registryKey(itemKey)
        );

        return Registry.register(Registries.ITEM, itemKey, item);
    }

    private static Item registerSimpleItem(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new Item(new Item.Settings().maxCount(1).registryKey(itemKey));

        return Registry.register(Registries.ITEM, itemKey, item);
    }

    private static Item registerPortableTerminal(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new PortableTerminalItem(
                new Item.Settings().maxCount(1).registryKey(itemKey)
        );

        return Registry.register(Registries.ITEM, itemKey, item);
    }

    public static void initializeItems() {
        skannamuMod.LOGGER.info("Registering Mod Items for " + skannamuMod.MOD_ID);

        CAT_MODULE = registerBinaryModule("cat_module", "cat");
        DECRYPT_MODULE = registerBinaryModule("decrypt_module", "decrypt");
        KEY_MODULE = registerBinaryModule("key_module", "key");

        ECHO_MODULE = registerBinaryModule("echo_module", "echo");

        PORTABLE_TERMINAL = registerPortableTerminal("portable_terminal");

}