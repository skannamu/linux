package com.skannamu.client.model;

import com.skannamu.item.tool.PortableTerminalItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class PortableTerminalModel extends GeoModel<PortableTerminalItem> {

    private static final String MOD_ID = "skannamu";
    private static final String ITEM_ID = "portable_terminal"; // Portable Terminal 아이템 ID

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.of(MOD_ID, "geckolib/models/item/" + ITEM_ID + "_model.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.of(MOD_ID, "textures/item/" + ITEM_ID + ".png");
    }

    @Override
    public Identifier getAnimationResource(PortableTerminalItem animatable) {
        return Identifier.of(MOD_ID, "geckolib/animations/item/" + ITEM_ID + ".animation.json");
    }
}
