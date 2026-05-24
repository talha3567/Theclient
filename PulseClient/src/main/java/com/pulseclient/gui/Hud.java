package com.pulseclient.gui;

import com.pulseclient.PulseClient;
import com.pulseclient.Module;
import java.util.List;
import java.util.stream.Collectors;

public class Hud {
    public void render() {
        List<Module> activeModules = PulseClient.INSTANCE.moduleManager.getModules().stream()
            .filter(Module::isEnabled)
            .collect(Collectors.toList());

        System.out.println("--- PulseClient HUD ---");
        for (Module m : activeModules) {
            System.out.println(m.getName());
        }
    }
}
