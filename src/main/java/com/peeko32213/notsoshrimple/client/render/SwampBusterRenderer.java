package com.peeko32213.notsoshrimple.client.render;

import com.peeko32213.notsoshrimple.client.model.SwampBusterModel;
import com.peeko32213.notsoshrimple.common.item.ItemClawblade;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SwampBusterRenderer extends GeoItemRenderer<ItemClawblade> {
    public SwampBusterRenderer() {
        super(new SwampBusterModel());
    }
}