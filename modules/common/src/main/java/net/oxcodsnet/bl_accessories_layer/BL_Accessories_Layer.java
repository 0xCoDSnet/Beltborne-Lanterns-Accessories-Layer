package net.oxcodsnet.bl_accessories_layer;

import net.oxcodsnet.bl_accessories_layer.common.config.SlotConfig;

public final class BL_Accessories_Layer {
    public static final String MOD_ID = "bl_accessories_layer";

    public static void init() {
        SlotConfig.load();
        // Common init code (shared between Fabric and NeoForge)
        // TODO:
        // 1) когда жмякаешь кружочек рядом с лампой, её рендер должен отключаться
        // 2) когда ставить другую лампу в декоративный слот, она должна заменять лампу из оригинального слота
    }
}

