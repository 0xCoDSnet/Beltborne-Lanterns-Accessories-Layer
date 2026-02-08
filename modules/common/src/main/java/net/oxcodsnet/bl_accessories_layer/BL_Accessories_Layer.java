package net.oxcodsnet.bl_accessories_layer;

import net.oxcodsnet.bl_accessories_layer.common.config.SlotConfig;

public final class BL_Accessories_Layer {
    public static final String MOD_ID = "bl_accessories_layer";

    public static void init() {
        SlotConfig.load();
    }
}
