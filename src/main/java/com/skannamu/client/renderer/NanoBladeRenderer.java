package com.skannamu.client.renderer;

import com.skannamu.client.model.NanoBladeModel;
import com.skannamu.item.weapon.NanoBladeItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NanoBladeRenderer extends GeoItemRenderer<NanoBladeItem> {

    public NanoBladeRenderer() {
        super(new NanoBladeModel());
    }
}