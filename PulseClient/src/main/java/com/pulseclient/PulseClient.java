package com.pulseclient;

import com.pulseclient.events.EventManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PulseClient implements ClientModInitializer {
    public static final PulseClient INSTANCE = new PulseClient();
    public static final Logger LOGGER = LoggerFactory.getLogger("PulseClient");
    public static final String VERSION = "1.0.0";
    public final ModuleManager moduleManager = new ModuleManager();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing PulseClient " + VERSION);
        moduleManager.init();
    }

    public void onTick() {
        moduleManager.getModules().forEach(module -> {
            if (module.isEnabled()) {
                if (module instanceof com.pulseclient.modules.combat.PulseKillAura) {
                    ((com.pulseclient.modules.combat.PulseKillAura) module).onTick();
                } else if (module instanceof com.pulseclient.modules.movement.PulseFlight) {
                    ((com.pulseclient.modules.movement.PulseFlight) module).onTick();
                }
            }
        });
    }

    // For testing and direct access
    public void init() {
        onInitializeClient();
    }
}
