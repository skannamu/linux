package com.skannamu.client.renderer;

import com.skannamu.client.model.PortableTerminalModel; // 모델 클래스 임포트
import com.skannamu.item.tool.PortableTerminalItem; // 아이템 클래스 임포트
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PortableTerminalRenderer extends GeoItemRenderer<PortableTerminalItem> {

    public PortableTerminalRenderer() {
        // 생성자에서 PortableTerminalModel 인스턴스를 GeoItemRenderer의 상위 생성자에 전달합니다.
        super(new PortableTerminalModel());
    }
}
