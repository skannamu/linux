package com.skannamu.init;

import com.skannamu.skannamuMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import com.skannamu.item.weapon.NanoBladeItem;

public class ModItems {

    public static Item NANO_BLADE;


    private static Item registerNanoBlade(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = new NanoBladeItem(new Item.Settings().maxCount(1).registryKey(itemKey));

        return Registry.register(Registries.ITEM, itemKey, item);
    }

    public static void initializeItems() {
        skannamuMod.LOGGER.info("Registering Mod Items for " + skannamuMod.MOD_ID);

        NANO_BLADE = registerNanoBlade("nano_blade");
    }
}