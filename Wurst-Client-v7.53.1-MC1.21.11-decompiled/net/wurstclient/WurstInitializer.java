package net.wurstclient;

import net.fabricmc.api.ModInitializer;
import net.wurstclient.WurstClient;

public final class WurstInitializer
implements ModInitializer {
    private static boolean initialized;

    public void onInitialize() {
        if (initialized) {
            throw new RuntimeException("WurstInitializer.onInitialize() ran twice!");
        }
        WurstClient.INSTANCE.initialize();
        initialized = true;
    }
}
