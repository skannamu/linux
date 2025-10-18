package com.skannamu.init;

import com.skannamu.item.block.VaultBlockEntity;
import com.skannamu.skannamuMod;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class VaultBlockEntities {

    public static BlockEntityType<VaultBlockEntity> VAULT_BLOCK_ENTITY_TYPE;

    public static void registerBlockEntities() {

        VAULT_BLOCK_ENTITY_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(skannamuMod.MOD_ID, "vault_block_entity"), // 고유 ID
                BlockEntityType.Builder.create(
                        VaultBlockEntity::new, // Block Entity 생성자 참조
                        BlockInitialization.VAULT_BLOCK // VaultBlock 클래스 참조
                ).build()
        );

        skannamuMod.LOGGER.info("Registered Vault Block Entity.");
    }
}