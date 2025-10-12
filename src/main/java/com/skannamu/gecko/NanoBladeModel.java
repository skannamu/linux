package com.skannamu.gecko;

import com.skannamu.item.weapon.NanoBladeItem;
import com.skannamu.skannamuMod;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

// 3D 모델 파일, 텍스처, 애니메이션 경로를 정의하는 클래스입니다.
public class NanoBladeModel extends GeoModel<NanoBladeItem> {

    @Override
    public Identifier getModelResource(NanoBladeItem object) {
        return Identifier.of(skannamuMod.MOD_ID, "geo/item/nanoblade.geo.json");
    }

    @Override
    public Identifier getTextureResource(NanoBladeItem object) {
        return Identifier.of(skannamuMod.MOD_ID, "textures/item/nanoblade_texture.png");
    }

    @Override
    public Identifier getAnimationResource(NanoBladeItem animatable) {
        return Identifier.of(skannamuMod.MOD_ID, "animations/item/nanoblade.animation.json");
    }
}
