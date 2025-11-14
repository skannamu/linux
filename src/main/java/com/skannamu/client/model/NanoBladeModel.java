package com.skannamu.client.model;

import com.skannamu.item.weapon.NanoBladeItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class NanoBladeModel extends GeoModel<NanoBladeItem> {

    private static final String MOD_ID = "skannamu";
    private static final String ITEM_ID = "nanoblade"; // 아이템 ID

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.of(MOD_ID, "geckolib/models/item/" + ITEM_ID + "_model.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.of(MOD_ID, "textures/item/" + ITEM_ID + "1.png");
    }

    @Override
    public Identifier getAnimationResource(NanoBladeItem animatable) {
        return Identifier.of(MOD_ID, "geckolib/animations/item/" + ITEM_ID + ".animation.json");
    }
}