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
    public static Item NANO_BLADE;
    public static Item BACKATTACK_MODULE;    // Auxiliary 계열
    public static Item EMP_MODULE;
    public static Item EMP_IFF_MODULE;

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

    // 💡 ExploitModuleItem 등록 (exploit 명령어 활성화)
    private static Item registerExploitModule(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new ExploitModuleItem(new Item.Settings().maxCount(1).registryKey(itemKey));
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    // 💡 AuxiliaryModuleItem 등록 (auxiliary 명령어 활성화)
    private static Item registerAuxiliaryModule(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new AuxiliaryModuleItem(new Item.Settings().maxCount(1).registryKey(itemKey));
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    // 💡 NanoBladeItem 전용 등록 메서드 추가
    private static Item registerNanoBlade(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        // NanoBladeItem을 사용하여 등록
        Item item = new NanoBladeItem(new Item.Settings().maxCount(1).registryKey(itemKey));

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

        // 💡 registerSimpleItem 대신 registerNanoBlade를 사용합니다.
        NANO_BLADE = registerNanoBlade("nano_blade");
        BACKATTACK_MODULE = registerExploitModule("backattack_module");

        EMP_MODULE = registerAuxiliaryModule("emp_module");
        EMP_IFF_MODULE = registerSimpleItem("emp_iff_module");
    }
}