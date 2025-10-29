package com.skannamu.client.renderer;

import com.skannamu.client.model.NanoBladeModel; // 모델 클래스 임포트
import com.skannamu.item.weapon.NanoBladeItem; // 아이템 클래스 임포트
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NanoBladeRenderer extends GeoItemRenderer<NanoBladeItem> {

    public NanoBladeRenderer() {
        // 생성자에서 모델 클래스의 인스턴스를 GeoItemRenderer의 상위 생성자에 전달합니다.
        super(new NanoBladeModel());
    }
}