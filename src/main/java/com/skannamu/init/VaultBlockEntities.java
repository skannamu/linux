package com.skannamu.init;

import com.skannamu.item.block.VaultBlockEntity;
import com.skannamu.skannamuMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
                FabricBlockEntityTypeBuilder.create( // FabricBlockEntityTypeBuilder 사용
                        VaultBlockEntity::new,      // Block Entity 생성자 참조
                        BlockInitialization.VAULT_BLOCK
                ).build()
        );

        skannamuMod.LOGGER.info("Registered Vault Block Entity.");
    }
}
