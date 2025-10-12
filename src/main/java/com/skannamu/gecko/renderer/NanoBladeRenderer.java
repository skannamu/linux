package com.skannamu.gecko.renderer;

import com.skannamu.gecko.NanoBladeModel;
import com.skannamu.item.weapon.NanoBladeItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NanoBladeRenderer extends GeoItemRenderer<NanoBladeItem> {

    public NanoBladeRenderer() {
        super(new NanoBladeModel());
    }
}
