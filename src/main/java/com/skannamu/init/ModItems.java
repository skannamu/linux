package com.skannamu.init;

import com.skannamu.module.BinaryModuleItem;
import com.skannamu.skannamuMod;
import com.skannamu.item.tool.PortableTerminalItem;
import net.minecraft.item.Item;
import com.skannamu.item.weapon.NanoBladeItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey; // 필수 임포트
import net.minecraft.registry.RegistryKeys; // 필수 임포트
import net.minecraft.util.Identifier;

public class ModItems {

    public static Item CAT_MODULE;
    public static Item DECRYPT_MODULE;
    public static Item KEY_MODULE;
    public static Item PORTABLE_TERMINAL;
    public static Item NANO_BLADE;
    public static Item EXPLOIT_MODULE;

    private static Item registerBinaryModule(String name, String commandName) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new BinaryModuleItem(commandName,
                new Item.Settings().maxCount(1).registryKey(itemKey)
        );

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
    private static Item registerNanoBlade(String name){
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new NanoBladeItem(new Item.Settings().maxCount(1).registryKey(itemKey));
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    public static void initializeItems() {
        skannamuMod.LOGGER.info("Registering Mod Items for " + skannamuMod.MOD_ID);
        CAT_MODULE = registerBinaryModule("cat_module", "cat");
        DECRYPT_MODULE = registerBinaryModule("decrypt_module", "decrypt");
        KEY_MODULE = registerBinaryModule("key_module", "key");
        PORTABLE_TERMINAL = registerPortableTerminal("portable_terminal");
        NANO_BLADE = registerNanoBlade("nano_blade");
        EXPLOIT_MODULE = registerBinaryModule("exploit_module", "exploit");
    }
}